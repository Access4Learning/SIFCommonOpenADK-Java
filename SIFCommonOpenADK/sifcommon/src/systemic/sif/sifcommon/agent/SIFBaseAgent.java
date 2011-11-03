/*
* Copyright 2010-2011 Systemic Pty Ltd
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software distributed under the License 
* is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
* or implied.
* See the License for the specific language governing permissions and limitations under the License.
*/
package systemic.sif.sifcommon.agent;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import openadk.library.ADK;
import openadk.library.ADKException;
import openadk.library.ADKFlags;
import openadk.library.Agent;
import openadk.library.AgentProperties;
import openadk.library.Zone;
import openadk.library.tools.cfg.AgentConfig;
import openadk.library.tools.mapping.ADKMappingException;
import openadk.library.tools.mapping.Mappings;

import org.apache.log4j.Logger;

import systemic.sif.sifcommon.BaseInfo;
import systemic.sif.sifcommon.customObject.CustomObjectInterface;
import systemic.sif.sifcommon.publisher.BasePublisher;
import systemic.sif.sifcommon.subscriber.BaseSubscriber;
import systemic.sif.sifcommon.utils.SIFCommonProperties;
import au.com.systemic.framework.utils.StringUtils;


/**
 * This class forms the base of all agents developed with the SIFCommon Framework. An agent implementation
 * should extend this class and implement the abstract classes. In most cases nothing else needs to be 
 * changes but there is the occasional time where there is the need to override a particular behaviour 
 * (ie. change the way logging is initialised, how to deal with custom objects etc).<p>
 * It is not recommended to override any other method since this class builds up the relationship between
 * subscribers and publishers that make up an agent, all driven by the SIFAgent.properties file.<p>
 * 
 * @author Joerg Huber
 *
 */
public abstract class SIFBaseAgent extends Agent
{
	private static final int DELAY_BETWEEN_THREAD_START = 10;
	
    /** Classes that extend this class can use this logger directly */
    protected Logger logger = null;

    /* SIF Common Framework Properties */
	private SIFCommonProperties frameworkProperties = null;

	private AgentConfig agentConfig = null;
    private AgentProperties agentProperties = null;
    private String agentID = null;
    private Mappings mappings = null;

	private List<BaseSubscriber> initialisedSubscribers = new ArrayList<BaseSubscriber>();
    private List<BasePublisher> initialisedPublishers = new ArrayList<BasePublisher>();
    
	private ScheduledExecutorService publisherService = null;
	private ScheduledExecutorService subscriberService = null;

    /**
     * This method initialises custom objects that have been created with Pearson's ADKGen. Generally that 
     * would look something like this:<p>
     * 
     * <code>
     *   CustomObjectDTD customObjects = new CustomObjectDTD();<br/>
     *   customObjects.load();<br/>
     * </code>
     * <p>
     * If there are no custom objects then this method can be nulled out. This method will be called as
     * part of the constructor of this class.
     */
    public abstract void initCustomObjects();


    /**
     * Constructor. This creates an SIF agent object with an id of 'agentID'. The passed in 
     * 'propertiesFileName' is used to read environmental data for the agent. Note only the file name 
     * without the extension of '.properties' is required. The properties for various agents can use the 
     * same property file. The properties for each agent are identified by the property name that holds 
     * the 'agentID' as part of it:<p>
     * 
     * agent.<agenttID>.some.property=value<p>
     * 
     * This method will initialise the agent but won't connect it to any ZIS. To connect it to any ZIS 
     * listed in the agent's config file the startAgent method must be called.<p>
     * 
     * @param agentID The unique ID of this Agent. This should be the same value as in the agent id 
     *                reference in the SIFAgent.propertiess file.
     * @param propertiesFileName The name of the SIFAgent.properties file
     */
    public SIFBaseAgent(String agentID, String propertiesFileName) throws Exception
    {
        super(agentID);
        setAgentID(agentID);
        setName(agentID + " Agent");
        setFrameworkProperties(new SIFCommonProperties(propertiesFileName));

        // Initialize the ADK.
        ADK.initialize();

        // read agent's config file and load values into memory
        agentConfig = new AgentConfig();
        agentConfig.read(frameworkProperties.getADKConfigPathAndFileName(agentID), false);

        // setup logging
        configureLogging();
        
        logger.debug("AgentID: "+ agentID);
        logger.debug("SIF Common Framework Properties Filename: "+ propertiesFileName);

        ADK.setVersion(agentConfig.getVersion());

        // Now call the agent's superclass initialize once the configuration file has been read
        super.initialize();

        // Initialse Custom Objects that are created manually
        initailiseCustomObjects();

        // Initialse Custom Objects that are created with adkgen
        initCustomObjects();

        // Ask the AgentConfig instance to "apply" all configuration settings to this Agent; for example,
        // all <property> elements that are children of the root <agent> node are parsed and applied to this
        // Agent's AgentProperties object; all <zone> elements are parsed and registered with the Agent's
        // ZoneFactory, and so on.
        agentConfig.apply(this, true);
        agentProperties = getProperties();
        setAgentMappings(findAgentMappings());
    }

    /**
     * Constructor. Convenience method to above. It assumes that the properties file name is 
     * 'SIFAgent.properties'.<p>
     * 
     * @param agentID
     */
    public SIFBaseAgent(String agentID) throws Exception
    {
        this(agentID, "SIFAgent");
    }

    /*---------------------*/
    /* Getters and Setters */
    /*---------------------*/
    public String getAgentID()
    {
        return agentID;
    }

    public void setAgentID(String agentID)
    {
        this.agentID = agentID;
    }

    public SIFCommonProperties getFrameworkProperties()
    {
    	return frameworkProperties;
    }

	public void setFrameworkProperties(SIFCommonProperties frameworkProperties)
    {
    	this.frameworkProperties = frameworkProperties;
    }

    /**
     * This method returns all subscribers that are initialise (afterstartAgent() call). It is assumed that 
     * the subscribers are initialised using this framework and therefore should be identical to the list of
     * subscribers in the SIFAgent.properties file.<p>
     * 
     * @return See description.
     */
    public List<BaseSubscriber> getInitialisedSubscribers()
    {
        return initialisedSubscribers;
    }

    /**
     * This method returns all publishers that are initialise (after startAgent() call). It is assumed that 
     * the publishers are initialised using this framework and therefore should be identical to the list of
     * publishers in the SIFAgent.properties file.<p>
     * 
     * @return See description.
     */
    public List<BasePublisher> getInitialisedPublishers()
    {
        return initialisedPublishers;
    }

    public Mappings getAgentMappings()
    {
    	return mappings;
    }

	public void setAgentMappings(Mappings mappings)
    {
    	this.mappings = mappings;
    }

    /*--------------------------*/
    /* Start/Stop Agent methods */
    /*--------------------------*/

	/**
     * This method starts the agent. It connects to all required zones and events, request and queries as 
     * described in the agent config file.<p>
     * 
     * <b>NOTE:</b> The publishers will be assigned this agent in each BasePublisher object as part 
     * of this call. The subscribers will be assigned this agent in each BaseSubscriber object as part of 
     * this call.<p>
     * 
     * @param implementShutdownHook If set to true then a shotdownHook is installed that captures the 
     *                              CTRL-C to terminate the agent
     * @param publishers A list of publishers that are registered with the zones of this agent
     * @param subscribers A list of subscribers that are registered with the zones of this agent
     * 
     * @throws Exception An underlying issue such as failing to initialise the publishers, subscribers etc. 
     *                   An error is logged and an appropriate message is stored in the exception.
     */
    public void startAgent(boolean implementShutdownHook, Collection<BasePublisher> publishers, Collection<BaseSubscriber> subscribers) throws Exception
    {
        // System.out.println("startAgent() called...");
        logger.debug("startAgent() called...");
        boolean connected = true;
        if (implementShutdownHook)
        {
            // Install a shutdown hook to cleanup when Ctrl+C is pressed
            logger.debug("Installing shutdown hook");
            final SIFBaseAgent tmpAgent = this;
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                public void run()
                {
                    tmpAgent.stopAgent();
                }
            });
        }

        // Connect all publishers and subscribers to listed Zones in the agent.cfg file
        List<Zone> zones = getZones();
        for (Zone zone : zones)
        {
            try
            {
                logger.info("Connecting to zone: '" + zone.getZoneId() + "' with URL: '" + zone.getZoneUrl() + "'");
                
                // Connect Publishers to this zone
                if ((publishers != null) && (publishers.size() > 0))
                {
                    initialisedPublishers.clear();
                    for (BasePublisher publisher : publishers)
                    {
                        logger.info("\n====================================================================================\n==== Set Publisher: " + publisher.getId() + " for zone "+ zone.getZoneId()+"\n====================================================================================");

                        // Set all required values for the publisher
                    	populateBaseInfo(publisher, zones);

                        // Assign publisher to zone...
                        zone.setPublisher(publisher, publisher.getDtd(), publisher.getOptions());

                        // Add it to list of initialised publishers
                        initialisedPublishers.add(publisher);
                        logger.info("Publisher: " + publisher.getId() + " added to zone "+ zone.getZoneId());
                    }
                }

                // Connect Subscribers to this zone
                if ((subscribers != null) && (subscribers.size() > 0))
                {
                    initialisedSubscribers.clear();
                    for (BaseSubscriber subscriber : subscribers)
                    {
                    	logger.info("\n====================================================================================\n==== Set Subscriber: " + subscriber.getId() + " for zone "+ zone.getZoneId()+ "\n====================================================================================");

                        // Set all required values for the publisher
                    	populateBaseInfo(subscriber, zones);

                        // Assign subscriber to zone...
                        subscriber.provision(zone); // Assign subscriber to zone...

                        // Add it to list of initialised subscribers
                        initialisedSubscribers.add(subscriber);
                        logger.info("Subscriber: " + subscriber.getId() + " provisioned in zone "+ zone.getZoneId());
                    }
                }

                zone.connect(ADKFlags.PROV_REGISTER);
                logger.info("\n====================================================================================\n==== Connection to zone "+ zone.getZoneId()+ " successful.\n====================================================================================");
            }
            catch (Exception ex)
            {
                logger.error("Failed to connect to zone '" + zone.getZoneId() + "': " + ex.getMessage(), ex);
                connected = false;
            }
        }

        if (connected)
        {
        	// Start Publishers
        	startPublishers(initialisedPublishers);
        	
        	// Start subscribers
        	startSubscribers(initialisedSubscribers);

            logger.info("Agent " + getAgentID() + " runs "+Thread.activeCount()+" threads.");
            if (implementShutdownHook)
            {
                logger.info(getAgentID() + " Agent is running (Press Ctrl-C to stop):");
            }
        }
        else
        {
            logger.info("Due to errors in starting the " + getAgentID() + " the agent will now shut down...");
            stopAgent();
        }
    }

    /**
     * Same as above method except that shutdown hook is defaulted to true. Most agent would use this method.
     */
    public void startAgent(Collection<BasePublisher> publishers, Collection<BaseSubscriber> subscribers) throws Exception
    {
        startAgent(true, publishers, subscribers);
    }

    /**
     * Convenience method. It will start the agent with shutdown hook defaulted to true and call the 
     * getPublishers() and getSubscribers() automatically.<p>
     * 
     * @see #getPublishers()
     * @see #getSubscribers()
     * 
     */
    public void startAgent() throws Exception
    {
        startAgent(true, getPublishers(), getSubscribers());
    }

    /**
     * Convenience method. It will start the agent with shutdown hook set as given by the parameter and call
     * the getPublishers() and getSubscribers() automatically.<p>
     * 
     * @see #getPublishers()
     * @see #getSubscribers()
     * 
     */
    public void startAgent(boolean implementShutdownHook) throws Exception
    {
        startAgent(implementShutdownHook, getPublishers(), getSubscribers());
    }

    /**
     * This method must be called to shut down the agent in full and release all resources. Ideally it is 
     * called in a 'finally' block of the agent executable to guarantee that the stopAgent() method is 
     * called in all cases.<p>
     */
    public void stopAgent()
    {
        if (isInitialized())
        {
            logger.info("Shutting down Agent: " + agentID);
            try
            {
                // free up custom resources
                cleanupResources();

                // Shutdown the agent and send a SIF_Unregister to the zone if the unreg command line option was specified
                shutdown(ADKFlags.PROV_NONE);
                // ADKFlags.PROV_UNREGISTER
                //System.exit(0);
            }
            catch (ADKException adkEx)
            {
                System.out.println(adkEx);
            }
        }
        else
        {
            logger.info("Agent " + agentID + " is already shutdown or was not running.");
        }
    }

    /**
     * This method returns the agent's property values for the agent configuration file (generally called 
     * SIFAgent.properties). The agent configuration file is identified by the following properties in the
     * 'propertiesFileName' passed to the constructor of this class:<p>
     * 
     * agent._agenttID_.cfg.location=_dir_of_config_file_<br/>
     * agent._agenttID_.cfg.name=_name_of_config_file_<br/>
     * 
     * @return See description.
     */
    public AgentProperties getAgentProperties()
    {
        return agentProperties;
    }

    /**
     * This methods gets all properties from the actual agent.cfg file.
     * 
     * @return See description
     */
    public AgentConfig getAgentConfig()
    {
        return agentConfig;
    }

    /**
     * This method return mappings for the given mapping ID of the agent's configuration file. If no such
	 * mapping exists or the retrieval of the mappings caused an error then the error is logged and null 
	 * is returned.<p>
     * 
     * @param mappingID The ID for which the mappings shall be returned.<br/>
     * 
     * @return See description.
     */
    public Mappings getMappings(String mappingID)
    {
        try
        {
            return agentConfig.getMappings().getMappings("Default");
        }
        catch (Exception ex)
        {
            logger.error("Failed to retrieve mappings: " + ex.getMessage(), ex);
        }
        return null;
    }
    
    /**
     * Returns the list of Zones this agent is connected to.
     * 
     * @return List of zones.
     */
    public List<Zone> getZones()
    {
    	List<Zone> zones = new ArrayList<Zone>();
    	Zone[] zoneArr = getZoneFactory().getAllZones();
    	if (zones != null)
    	{
    		for (Zone zone : zoneArr)
    		{
    			zones.add(zone);
    		}
    	}
    	return zones;
    }

    /*-------------------------------------------------*/
    /* Methods commonly overwritten by sub-class Agent */
    /*-------------------------------------------------*/
    /**
     * Overwrite this function if customised logging is required. See ADK
     * Developer Guide for details about logging. The default implementation
     * reads the following properties form the 'propertiesFileName' passed to
     * the constructor of this class:<p>
     * 
     * agent._agentID_.log.dir=_dir_of_log_file_
     * agent._agentID_.log.name=_name_of_log_file_
     * agent._agentID_.debugLevel=_name_of_debug_level_
     * 
     * The default debug level is ADK.DBG_MINIMAL. The _name_of_debug_level_
     * should be any value listed in the ADK class with the name 'DBG_xxx'.<p>
     * 
     * Example<p>
     * agent.myAgent.debugLevel=DBG_ALL
     */
    protected void configureLogging()
    {
        try
        {
        	getFrameworkProperties().getLogPathAndFileName(getAgentID());
            ADK.setLogFile(getFrameworkProperties().getLogPathAndFileName(getAgentID()));
            String debugLevel = getFrameworkProperties().getDebugLevel(getAgentID());
            ADK.debug = DEBUG_LEVELS.get(debugLevel);
            logger = ADK.getLog();
        }
        catch (Exception ex)
        {
            ex.printStackTrace(); // we cannot log anything because the log is broken.
        }
    }

    /**
     * Ensure that a known directory is used as the working directory. 
     * 
     * If the value is not set the default is used which generally is the
     * directory where the agent is started from.
     */
    @Override
    public java.lang.String getHomeDir()
    {
        String workDir = getFrameworkProperties().getWorkDir(agentID);
        return (workDir == null) ? super.getHomeDir() : workDir;
    }

    /*---------------------*/
    /*-- private methods --*/
    /*---------------------*/
    /**
     * This method initialises the list of publishers that are handled by this agent. The list of 
     * publishers and base package name must be listed in the global agent config file with the names:<p>
     * 
     * agent._agentID_.publisher.basePackageName=_basePackageName_<p>
     * agent._agentID_.publishers=_comma_separated_list_of_publishers_<p>
     * <p>
     * 
     * Example:<p>
     * agent.TestTimeTableAgent.publisher.basePackageName=au.edu.wa.eddept.SIF.Publisher<p>
     * agent.TestTimeTableAgent.publishers=C21RoomInfoPublisher, C21StaffPersonalPublisher<p>
     * 
     * This method then uses reflection to load and instantiate the publishers and returns the list of 
     * publishers.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Collection<BasePublisher> getPublishers()
    {
        Collection<BasePublisher> publishers = new ArrayList<BasePublisher>();
        String basePackageName = getFrameworkProperties().getPublisherBasePackageName(getAgentID());
        List<String> classList = getFrameworkProperties().getPublishers(getAgentID());
        basePackageName = makePackageName(basePackageName);

        for (String className : classList)
        {
            logger.debug("Publisher class to initialse: '" + className + "'");
            try
            {
                Class clazz = Class.forName(basePackageName + className);
                Class partypes[] = new Class[1];
                partypes[0] = String.class;

                Constructor ct = clazz.getConstructor(partypes);
                Object arglist[] = new Object[1];
                arglist[0] = new String(getIDFromClassName(className));
                Object classObj = ct.newInstance(arglist);
                if (classObj instanceof BasePublisher)
                {
                    publishers.add((BasePublisher)classObj);
                }
                else
                {
                    logger.error("Publisher class " + className + " doesn't extend BasePublisher. Cannot initialse the Publisher.");
                }
            }
            catch (Exception ex)
            {
                logger.error("Cannot create Publisher Object '" + className + "': " + ex.getMessage(), ex);
            }
        }

        return publishers;
    }

    /**
     * This method initialises the list of subscribers that are handled by this agent. The list of 
     * subscribers and base package name must be listed in the global agent config file with the names:<p>
     * 
     * agent._agentID_.subscriber.basePackageName=_basePackageName_<p>
     * agent._agentID_.subscribers=_comma_separated_list_of_subscribers_<p>
     * 
     * Example:<p>
     * agent.TestTimeTableAgent.subscriber.basePackageName=au.edu.wa.eddept.SIF.Subscriber<p>
     * agent.TestTimeTableAgent.subscribers=C21RoomInfoSubscriber,C21StaffPersonalSubscriber<p>
     * 
     * This method then uses reflection to load and instantiate the subscribers and returns the list of 
     * subscribers.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Collection<BaseSubscriber> getSubscribers()
    {
        Collection<BaseSubscriber> subscribers = new ArrayList<BaseSubscriber>();
        String basePackageName = getFrameworkProperties().getSubscriberBasePackageName(getAgentID());
        List<String> classList = getFrameworkProperties().getSubscribers(getAgentID());
        basePackageName = makePackageName(basePackageName);
        
        for (String className : classList)
        {
            logger.debug("Subscriber class to initialse: " + className);
            try
            {
                Class clazz = Class.forName(basePackageName + className);
                Class partypes[] = new Class[1];
                partypes[0] = String.class;

                Constructor ct = clazz.getConstructor(partypes);
                Object arglist[] = new Object[1];
                arglist[0] = new String(getIDFromClassName(className));
                Object classObj = ct.newInstance(arglist);
                if (classObj instanceof BaseSubscriber)
                {
                    subscribers.add((BaseSubscriber)classObj);
                }
                else
                {
                    logger.error("Subscriber class " + className + " doesn't extend BaseSubscriber. Cannot initialse the Subscriber.");
                }
            }
            catch (Exception ex)
            {
                logger.error("Cannot create Subscriber Object " + className + ": " + ex.getMessage());
            }
        }

        return subscribers;
    }

    /**
     * This method cleans up resources used by this agent's publishers and subscribers.
     */
    private void cleanupResources()
    {
    	/* Shutdown each publisher and remove all threads properly for this agent */
    	for (BasePublisher publisher : getInitialisedPublishers())
    	{
    		logger.debug("Call finalise for Publisher "+publisher.getId());
    		publisher.shutdownPublisher();
    		logger.debug("Finalise for Publisher "+publisher.getId()+" complete.");
    	}
    	if (publisherService != null)
    	{
    		// Terminate all publisher threads for this agent.
    		if (publisherService != null)
    		{
    			publisherService.shutdown();
    		}
    	}
    	
    	/* Shutdown each subscriber and remove all threads properly for this agent */
    	for (BaseSubscriber subscriber : getInitialisedSubscribers())
    	{
    		logger.debug("Call finalise for Subscriber "+subscriber.getId());
    		subscriber.shutdownSubscriber();
    		logger.debug("Finalise for Subscriber "+subscriber.getId()+" complete.");
    	}
    	if (subscriberService != null)
    	{
    		// Terminate all subscriber threads for this agent.
    		if (subscriberService != null)
    		{
    			subscriberService.shutdown();
    		}
    	}
    }

    
	private void startPublishers(List<BasePublisher> publishers) throws Exception
	{
		boolean multiThreaded = getFrameworkProperties().getMultiThreaded(getAgentID(), true);
		int delay = getFrameworkProperties().getStartupDelay(DELAY_BETWEEN_THREAD_START);
		
		int i = 0;
		for (BasePublisher publisher : publishers)
		{
			int frequency = getFrameworkProperties().getEventFrequencyInSeconds(getAgentID(), publisher.getId(), 3600);
			logger.debug("Event Frequency for Publisher "+publisher.getId()+" in Agent "+getAgentID()+" is set to "+frequency+" seconds.");
			
			// If frequency is set to NO_EVENT then we default to 3600 because the run() in the publisher
			// will not execute the event processing.
			frequency = (frequency == SIFCommonProperties.NO_EVENT) ? 3600 : frequency;
			
			if (multiThreaded)
			{
				publisherService = Executors.newSingleThreadScheduledExecutor();
				
				// Ensure there is 10 seconds between the start of each publisher so that they don't hammer
				// the system at the same time during startup.
				publisherService.scheduleWithFixedDelay(publisher, i*delay, frequency, TimeUnit.SECONDS);
			}
			else
			{
				if (publisherService == null)
				{
					publisherService = Executors.newSingleThreadScheduledExecutor();
				}
				publisherService.scheduleWithFixedDelay(publisher, i*delay, frequency, TimeUnit.SECONDS);
			}
	        i++;
		}	
	}
	
	private void startSubscribers(List<BaseSubscriber> subscribers) throws Exception
	{
		boolean multiThreaded = getFrameworkProperties().getMultiThreaded(getAgentID(), true);
		int delay = getFrameworkProperties().getStartupDelay(DELAY_BETWEEN_THREAD_START);
		
		int i = 0;
		for (BaseSubscriber subscriber : subscribers)
		{
			int frequency = getFrameworkProperties().getSyncFrequencyInSeconds(getAgentID(), subscriber.getId(), 3600);
			logger.debug("Sync Frequency for Subscriber "+subscriber.getId()+" in Agent "+getAgentID()+" is set to "+frequency+" seconds.");
			
			// If frequency is set to NO_SYNC then we default to 3600 because the run() in the subscriber
			// will not execute the sync processing.
			frequency = (frequency == SIFCommonProperties.NO_SYNC) ? 3600 : frequency;
			
			// Start the consumers
			subscriber.startConsumers();
			
			// Start the actual subscriber
			if (multiThreaded)
			{
				subscriberService = Executors.newSingleThreadScheduledExecutor();
				
				// Ensure there is 10 seconds between the start of each publisher so that they don't hammer
				// the system at the same time during startup.
				subscriberService.scheduleWithFixedDelay(subscriber, i*delay, frequency, TimeUnit.SECONDS);
			}
			else
			{
				if (subscriberService == null)
				{
					subscriberService = Executors.newSingleThreadScheduledExecutor();
				}
				subscriberService.scheduleWithFixedDelay(subscriber, i*delay, frequency, TimeUnit.SECONDS);
			}
	        i++;			
		}			
	}

    /*
     * Uses reflection to determine the list of custom objects to initialse. The list and base package name
     * of the custom objects must be listed in the global agent config file with the names:
     * 
     * agent.<agentID>.customObjects.basePackageName=<basePackageName>
     * agent.<agentID>.customObjects=<comma_separated_list_of_custom_objects>
     * 
     * Example:
     * agent.TestTimeTableAgent.customObjects.basePackageName=au.edu.wa.eddept.SIF.customObject
     * agent.TestTimeTableAgent.customObjects=TimeTableDef,TimeTableInstanceDef,StudentSubjectDef
     */
    private void initailiseCustomObjects()
    {
        String basePackageName = getFrameworkProperties().getCustonObjBasePackageName(getAgentID());
        List<String> classList = getFrameworkProperties().getCustomObjects(getAgentID());
        basePackageName = makePackageName(basePackageName);

        for (String className : classList)
        {
            logger.debug("Custom SIF Object class to initialse: " + className);
            try
            {
                Object classObj = Class.forName(basePackageName + className).newInstance();
                if (classObj instanceof CustomObjectInterface)
                {
                    ((CustomObjectInterface)classObj).initialise(logger);
                }
                else
                {
                    logger.error("Custom SIF Object class " + className + " doesn't implement CustomObjectInterface. Cannot initialse the Object.");
                }
            }
            catch (Exception ex)
            {
                logger.error("Cannot create Custom SIF Object " + className + ": " + ex.getMessage());
            }
        }
    }

    private String makePackageName(String packageName)
    {
	    if (StringUtils.isEmpty(packageName))
	    {
	        return "";
	    }
	    else
	    {
	    	return packageName.trim() + ".";
	    }
    }
    
    private Mappings findAgentMappings() throws ADKMappingException 
    {
        Mappings mappings = getAgentConfig().getMappings();
        String mappingName = getFrameworkProperties().getMappingName(getAgentID());
        if (mappings != null)
        {
        	mappings = mappings.getMappings(mappingName);
			if ((ADK.debug == ADK.DBG_DETAILED) || (ADK.debug == ADK.DBG_VERY_DETAILED))
			{
	        	if (mappings != null)
	        	{
	        		logger.debug("Found '"+mappingName+"' Mapping: "+mappings.toXML());
	        	}
	        	else
	        	{
	        		logger.debug("No '"+mappingName+"' Mappings defined.");                		
	        	}
			}
        }
        else
        {
    		logger.debug("No Mappings defined.");                		                	
        }
        return mappings;
    }
    
    private void populateBaseInfo(BaseInfo baseInfo, List<Zone> zones)
    {
    	baseInfo.setAgentID(getAgentID());
    	baseInfo.setAgentProperties(getAgentProperties());
    	baseInfo.setAgentConfig(getAgentConfig());
    	baseInfo.setFrameworkProperties(getFrameworkProperties());
    	baseInfo.setZones(zones);
    	baseInfo.setMappings(getAgentMappings());
    	baseInfo.setApplicationID(getFrameworkProperties().getApplicationID(getAgentID(), null));
    }

    private String getIDFromClassName(String fullyQualifiedClassName)
    {
    	int lastDotPos = fullyQualifiedClassName.lastIndexOf('.');
    	if (lastDotPos == -1)
    	{
    		return fullyQualifiedClassName;
    	}
    	else
    	{
    		return fullyQualifiedClassName.substring(lastDotPos+1, fullyQualifiedClassName.length());
    	}
    }
    
    private static final HashMap<String, Integer> DEBUG_LEVELS = new HashMap<String, Integer>();
    static
    {
        DEBUG_LEVELS.put("DBG_TRANSPORT", new Integer(ADK.DBG_TRANSPORT));
        DEBUG_LEVELS.put("DBG_MESSAGING", new Integer(ADK.DBG_MESSAGING));
        DEBUG_LEVELS.put("DBG_MESSAGING_EVENT_DISPATCHING", new Integer(ADK.DBG_MESSAGING_EVENT_DISPATCHING));
        DEBUG_LEVELS.put("DBG_MESSAGING_RESPONSE_PROCESSING", new Integer(ADK.DBG_MESSAGING_RESPONSE_PROCESSING));
        DEBUG_LEVELS.put("DBG_MESSAGING_PULL", new Integer(ADK.DBG_MESSAGING_PULL));
        DEBUG_LEVELS.put("DBG_MESSAGING_DETAILED", new Integer(ADK.DBG_MESSAGING_DETAILED));
        DEBUG_LEVELS.put("DBG_MESSAGE_CONTENT", new Integer(ADK.DBG_MESSAGE_CONTENT));
        DEBUG_LEVELS.put("DBG_PROVISIONING", new Integer(ADK.DBG_PROVISIONING));
        DEBUG_LEVELS.put("DBG_POLICY", new Integer(ADK.DBG_POLICY));
        DEBUG_LEVELS.put("DBG_RUNTIME", new Integer(ADK.DBG_RUNTIME));
        DEBUG_LEVELS.put("DBG_LIFECYCLE", new Integer(ADK.DBG_LIFECYCLE));
        DEBUG_LEVELS.put("DBG_EXCEPTIONS", new Integer(ADK.DBG_EXCEPTIONS));
        DEBUG_LEVELS.put("DBG_PROPERTIES", new Integer(ADK.DBG_PROPERTIES));
        DEBUG_LEVELS.put("DBG_ALL", new Integer(ADK.DBG_ALL));
        DEBUG_LEVELS.put("DBG_NONE", new Integer(ADK.DBG_NONE));
        DEBUG_LEVELS.put("DBG_MINIMAL", new Integer(ADK.DBG_MINIMAL));
        DEBUG_LEVELS.put("DBG_MODERATE", new Integer(ADK.DBG_MODERATE));
        DEBUG_LEVELS.put("DBG_MODERATE_WITH_PULL", new Integer(ADK.DBG_MODERATE_WITH_PULL));
        DEBUG_LEVELS.put("DBG_DETAILED", new Integer(ADK.DBG_DETAILED));
        DEBUG_LEVELS.put("DBG_VERY_DETAILED", new Integer(ADK.DBG_VERY_DETAILED));
    }
}
