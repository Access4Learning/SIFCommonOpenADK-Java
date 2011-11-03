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
package systemic.sif.sifcommon.subscriber;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import openadk.library.ADK;
import openadk.library.ADKException;
import openadk.library.DataObjectInputStream;
import openadk.library.Event;
import openadk.library.EventAction;
import openadk.library.MessageInfo;
import openadk.library.Query;
import openadk.library.QueryResults;
import openadk.library.QueryResultsOptions;
import openadk.library.SIFDataObject;
import openadk.library.SIFMessageInfo;
import openadk.library.Subscriber;
import openadk.library.SubscriptionOptions;
import openadk.library.Zone;
import openadk.library.infra.SIF_Error;
import openadk.library.tools.mapping.ADKMappingException;
import openadk.library.tools.mapping.MappingsContext;
import systemic.sif.sifcommon.BaseInfo;
import systemic.sif.sifcommon.mapping.MappingInfo;
import systemic.sif.sifcommon.model.SIFEvent;
import systemic.sif.sifcommon.model.SubscriberMessage;
import systemic.sif.sifcommon.subscriber.queue.SubscriberQueue;
import systemic.sif.sifcommon.utils.SIFCommonProperties;


/**
 * All subscribers should extend this class. It ensures that resources are dealt with correctly.
 * 
 * @author Joerg Huber
 */
public abstract class BaseSubscriber extends BaseInfo implements Subscriber, QueryResults, Runnable
{
	private SubscriptionOptions subscriptionOptions = null;
	private QueryResultsOptions queryResultsOptions = null;
	private SubscriberQueue<SubscriberMessage> queue = null;
    private ExecutorService service = null;

    /**
     * This class is a basic initialiser of a subscriber. No properties except the subscriberID and
     * DTD for the applicable subscriber are expected to be set in this method. The BaseAgent will call
     * this method first, then set appropriate properties of the BaseInfo class. Finally the BaseAgent will
     * call the startConsumers() method to complete the initialisation process of a subscriber.<p>
     * 
     * @param subscriberID The unique ID of this subscriber. Ideally this is the class name but this is not
     *                     necessary. This ID is used throughout the SIFCommon Framework classes to access
     *                     properties in the SIFAgent.properties file. This ID must match the reference
     *                     in this property file for the given subscriber.
     */
    public BaseSubscriber(String subscriberID)
	{
		super(subscriberID);
	}
    
    /*------------------*/
    /* Abstract Methods */
    /*------------------*/

	/**
     * This method will process the given event. Mappings can be used of the mappingInfo.mappingCtx is not
     * null. It is up to the implementor of this method to check that property before any mapping is
     * attempted. The mappingCtx is an inbound mapping context as expected for subscribers.<p>
     * This method is being called as part of the default implementation of the onEvent() method of the
     * SIFworks ADK.<p>
     * 
     * @param sifEvent The SIF Event to be processed.
     * @param zone The Zone that has received the event.
     * @param mappingInfo The mapping info that can be used if mapping is available. Note that property 
     *                    mappingCtx of this parameter can be null if there is no mapping available 
     *                    for the given subscriber and its SIF Object it deals with (ie. No mapping for 
     *                    StudentPersonal).
     * @param consumerID The ID of the consumer that processes this event. Because the subscriber uses 
     *                   a multi-threaded consumer model it can be useful to know which particular consumer
     *                   is asked to process this event.
     * @throws ADKException Any issues that arise with the processing of the event that need to be captured
     *                      and logged.
     */
	public abstract void processEvent(SIFEvent sifEvent, Zone zone, MappingInfo mappingInfo, String consumerID) throws ADKException;

	/**
     * This method will process the given SIF Object. Mappings can be used of the mappingInfo.mappingCtx is 
     * not null. It is up to the implementor of this method to check that property before any mapping is
     * attempted. The mappingCtx is an inbound mapping context as expected for subscribers.<p>
     * This method is being called as part of the default implementation of the onRequest() method of the
     * SIFworks ADK.<p>
	 * 
	 * @param sifObject The SIF Object to be processed.
	 * @param zone The Zone that has received the SIF Object.
     * @param mappingInfo The mapping info that can be used if mapping is available. Note that property 
     *                    mappingCtx of this parameter can be null if there is no mapping available 
     *                    for the given subscriber and its SIF Object it deals with (ie. No mapping for 
     *                    StudentPersonal).
     * @param consumerID The ID of the consumer that processes this SIF Message. Because the subscriber uses 
     *                   a multi-threaded consumer model it can be useful to know which particular consumer
     *                   is asked to process this SIF Message.
     * @throws ADKException Any issues that arise with the processing of the SIF Object that need to be 
     *                      captured and logged.
	 */
	public abstract void processResponse(SIFDataObject sifObject, Zone zone, MappingInfo mappingInfo, String consumerID) throws ADKException;

    /**
     * This method should release all resources associated with the given subscriber. It will be called as 
     * part of the shutdown process of the agent.<p>
     */
    public abstract void finalise();

    /*----------------------------------------------------------------*/
    /* Commonly Override Methods to customise some special behaviour. */
    /*----------------------------------------------------------------*/
	/**
	 * Signals this class to begin the synchronisation process. This class is responsible for querying the
	 * zone for any data it needs to synchronise itself. This method should be called after the subscriber 
	 * has connected to the zone.
	 * 
	 * @param zone The zone for which the a sync request will be sent.
	 */
	protected void sync(Zone zone) throws ADKException
	{
		Query query = new Query(getDtd()); // request the specified DTD object from the zone.
		query.setSIFVersions(getAgentConfig().getVersion());
		addToInitialSyncQuery(query, zone); // Add any query conditions you may have

		zone.query(query);
	}

	/**
	 * This method can be overwritten by the sub-class to specify further query conditions for the initial sync. 
	 * The query object is passed along and is not null and therefore can simply be modified like:<p>
	 * 
	 * <code>query.addCondition( StudentDTD.STUDENTPERSONAL_LOCALID, ComparisonOperators.LE, "M830540004340" );</code>
	 * 
	 * @param query  The query that is passed to the sub-class.
	 * @param zone the zone for which the query shall be modified.
	 */
	protected void addToInitialSyncQuery(Query query, Zone zone)
	{
	}  
 
	/**
	 * This method is called first when an event is received. By default it does nothing. This method can be used
	 * to do some custom processing of the raw event. The method must return TRUE for it to be processed by this
	 * subscriber in a standard way. If FALSE is returned then no further processing of this event is done. This
	 * pretty much equals to a discard of the event. It will be lost after this call return FALSE.
	 */
	protected boolean preProcessEvent(SIFDataObject sifObject, EventAction eventAction, Zone zone, MappingInfo mappingInfo)
    {
    	return true;
    }
    
	/**
	 * This method is called first when an sif object from a response is received. By default it does nothing. 
	 * This method can be used to do some custom processing of the raw sif object. The method must return TRUE 
	 * for it to be processed by this subscriber in a standard way. If FALSE is returned then no further 
	 * processing of this sif object is done. This pretty much equals to a discard of the object. It will be 
	 * lost after this call return FALSE.
	 */
	protected boolean preProcessQueryResults(SIFDataObject sifObject, Zone zone, MappingInfo mappingInfo)
	{
    	return true;		
	}

    /*----------------------------------------*/
    /* Implemented Method for Multi-threading */
    /*----------------------------------------*/

    /**
	 * This method is all that is needed to run the subscriber in its own thread. The thread is executed at
	 * given intervals driven by a property in the SIFCommonFramework property file. The interval/frequency
	 * defined in there is used to determine how often this thread is run. If some other mechanism (ie. DB)
	 * drives the sync() method such as check if a sync really is required then this needs to be coded in each
	 * subscriber specifically by means of overriding the sync(zone) or syncAllZones() method. For example if
	 * no sync is needed at all, simply override the syncAllZones() and null it out. If a sync is not required
	 * for a specific zone the override the sync(zone) method so that no sync is called for a specific zone.
	 */
	@Override
    public final void run()
    {
		boolean syncRequired = (getFrameworkProperties().getSyncFrequencyInSeconds(getAgentID(), getId(), SIFCommonProperties.NO_SYNC) != SIFCommonProperties.NO_SYNC);
		logger.debug("Thread woken up for Subscriber "+getId()+". Syncing Test Required accoring to Config file: "+syncRequired);
		
		if (syncRequired)
		{
			try
			{
				syncAllZones();
				logger.debug("Sync across all zones for Sudscriber "+getId()+" complete.");
			}
			catch (Exception ex)
			{
				logger.error("Failed to synchronise data for Sudscriber "+getId()+": "+ex.getMessage(),ex);
			}
		}
		
		logger.debug("Run() for Subscriber "+getId()+" finished.");
    }
	
	/**
	 * This method starts up all the consumers for this subscriber. This cannot be done as part of the
	 * constructor of this class because at that time not all properties of the BaseInfo class are populated
	 * and the startup of the consumers requires some of the properties.<p>
	 * 
	 * This class requires the 'frameworkProperties' of the BaseInfo to be set, otherwise an 
	 * ExecutionException will be thrown.
	 * 
	 * @throws ExecutionException Issue with starting the consumers of missing properties in the BaseInfo
	 *                            class.
	 */
	public final void startConsumers() throws ExecutionException
	{
		if (getFrameworkProperties() == null)
		{
			throw new ExecutionException("The property 'frameworkProperties' is not set for the "+getId()+".", null);
		}
		
		// Start up all consumers for this subscriber.
		int numThreads = getFrameworkProperties().getNumConsumerThreads(getAgentID(), getId());
		logger.debug("Start "+numThreads+" Consumer(s) for "+getId()+"...");
		queue = new SubscriberQueue<SubscriberMessage>(numThreads, getDtd().name()+"Queue", getFrameworkProperties().getWorkDir(getAgentID()));	
		service = Executors.newFixedThreadPool(numThreads);
		for (int i = 0; i < numThreads; i++)
		{
			String consumerID = getId()+"Consumer "+(i+1);
			logger.debug("Start Consumer "+consumerID);
			SubscriberMessageConsumer consumer = new SubscriberMessageConsumer(queue, consumerID, this);
			service.execute(consumer);
		}
		logger.debug(numThreads+" Consumer(s) for "+getId()+" initilaised and started.");
	}


	/**
	 * Allows this data handler class to provision itself with the zone.<p>
	 * 
	 * Provisioning involves notifying the zone of any objects this class subscribes, or provides
	 * data for, or whether this class should be notified of SIF_Responses for a specific data type.
	 * 
	 * @param zone The representation of a SIF Zone inside the ADK
	 * 
	 * @throws ADKException
	 */
	public void provision(Zone zone) throws ADKException
	{
		zone.setSubscriber(this, getDtd(), subscriptionOptions);
		zone.setQueryResults(this, getDtd(), queryResultsOptions);
	}

	/**
	 * This method calls the sync for all zones this subscriber is subscribed to. If a sync is not desired 
	 * or should be controlled through other means, simply override the default behaviour of this method.<p>
	 * 
	 * @throws ADKException
	 */
	protected void syncAllZones() throws ADKException
	{
	    for (Zone zone : getZones())
	    {
	    	sync(zone); // Grab all available updates for this zone...
	    }
	}

	/*
	 * Default implementation of the SIFWorks ADK onQueryPending() method. If this is required then your 
	 * subscriber should override this method.
	 * @see com.edustructures.sifworks.QueryResults#onQueryPending(com.edustructures.sifworks.MessageInfo, com.edustructures.sifworks.Zone)
	 */
	@Override
	public void onQueryPending(MessageInfo msgInfo, Zone zone) throws ADKException
	{
		logger.debug("==========================================================================================");
		logger.debug("onQueryPending() NOT IMPLEMENTED for subscriber "+getId());
		logger.debug("==========================================================================================");
	} 
	
    /*---------------------------------*/
    /* Implemented Event Based Methods */
    /*---------------------------------*/
	
	/*
	 * This method pushes the given SIF Object with its associated information as an event to the processing Queue.
	 */
	protected final void pushSIFEventToProcessQueue(SIFDataObject sifObject, Zone zone, MappingInfo mappingInfo, EventAction eventAction)
	{
		queue.blockingPush(new SubscriberMessage(sifObject, zone, mappingInfo,  eventAction));
	}

	/**
	 * Default implementation of the SIFWorks ADK onEvent() method. Do not call this method from
	 * any sub-classes.
	 */
	@Override
	public final void onEvent(Event event, Zone zone, MessageInfo msgInfo) throws ADKException
	{
		MappingInfo mappingInfo = getInboundMappingInfo(msgInfo);
		
		while (event.getData().available())
		{
			logger.debug("==================================================================================");
			logger.debug(getId()+".onEvent() received for SIF Event Type "+event.getAction()+" from zone: " + zone.getZoneId());

			SIFDataObject sifObject = event.getData().readDataObject();

			if (preProcessEvent(sifObject, event.getAction(), zone, mappingInfo))
			{
				// Push the event to the SubscriberQueue
				pushSIFEventToProcessQueue(sifObject, zone, mappingInfo, event.getAction());
				//queue.blockingPush(new SubscriberMessage(sifObject, zone, mappingInfo,  event.getAction()));
			}
			logger.debug("==================================================================================");
		}
	}

    /*--------------------------------------------*/
    /* Implemented Request-Response Based Methods */
    /*--------------------------------------------*/
	
	/*
	 * This method pushes the given SIF Object with its associated information to the processing Queue.
	 */
	protected final void pushSIFObjectToProcessQueue(SIFDataObject sifObject, Zone zone, MappingInfo mappingInfo)
	{
		queue.blockingPush(new SubscriberMessage(sifObject, zone, mappingInfo));
	}
	/**
	 * Default implementation of the SIFWorks ADK onQueryResults() method. Do not call this method from
	 * any sub-classes.
	 */
	@Override
	public final void onQueryResults(DataObjectInputStream dataobjectinputstream, SIF_Error sifError, Zone zone, MessageInfo msgInfo) throws ADKException
	{
		logger.debug("==========================================================================================");
		logger.debug(getId() + ".onQueryResults() received from zone:" + zone.getZoneId());

		if (sifError != null)
		{
			reportSIFError(sifError, zone);
		}
		else
		{
			MappingInfo mappingInfo = getInboundMappingInfo(msgInfo);
			int totalRecords = 0;
			while (dataobjectinputstream.available())
			{
				totalRecords++;
				SIFDataObject sifObject = dataobjectinputstream.readDataObject();
				// Push the event to the SubscriberQueue if the pre-prosessing indicates so.
				if (preProcessQueryResults(sifObject, zone, mappingInfo))
				{
					pushSIFObjectToProcessQueue(sifObject, zone, mappingInfo);
					//queue.blockingPush(new SubscriberMessage(sifObject, zone, mappingInfo));
				}
			}
			logger.info("Total "+getDtd().name()+" received: "+totalRecords);
		}
		logger.debug("==========================================================================================");
	}
	
	/**
	 * This method deals with an error that has been received from the zone in relation to a SIF
	 * response. This method is called as part of the SIFWorks ADK onQueryResults() method. The
	 * current implementation of the reportSIFError() is simply to log the error. If a different 
	 * behaviour is required the the sub-class can override this method to meet its requirements.
	 *
	 * @param sifError The SIF Error that has been received. This value is not null.
	 * @param zone The zone for which the error has been received.
	 */
	public void reportSIFError(SIF_Error sifError, Zone zone)
	{
		
		String errorStr = "An Error has been received for " + getId() + " for zone "+ zone.getZoneId() + ":"+
				"\nError Code   : "+sifError.getSIF_Code()+
				"\nCategory     : "+sifError.getSIF_Category()+
				"\nDescription  : "+sifError.getSIF_Desc()+
				"\nDetailed Desc: "+sifError.getSIF_ExtendedDesc();

		logger.error(errorStr);
	}
	
	/**
	 * This method shuts down this subscriber gracefully. It is called by the Agent when a shutdown request
	 * has been issued to the agent. It is not expected that sub-classes of this class call this method.
	 * Specifics of the sub-class shutdowns must be handled in the finalise() method of the sub-class. The 
	 * finalise() method is called as part of this method.
	 */
	public final void shutdownSubscriber()
	{
		// Terminate all consumer threads for this subscriber.
		if (service != null)
		{
			service.shutdown();
		}

		// Call user defined finalise of the subscriber.
		finalise();
	}
	
	/*---------------------*/
	/* Setters and Getters */
	/*---------------------*/

	public SubscriptionOptions getSubscriptionOptions()
	{
		return subscriptionOptions;
	}

	public void setSubscriptionOptions(SubscriptionOptions subscriptionOptions)
	{
		this.subscriptionOptions = subscriptionOptions;
	}

	public QueryResultsOptions getQueryResultsOptions()
	{
		return queryResultsOptions;
	}

	public void setQueryResultsOptions(QueryResultsOptions queryResultsOptions)
	{
		this.queryResultsOptions = queryResultsOptions;
	}  
  
	/*---------------------------*/
	/* A few Handy Debug methods */
	/*---------------------------*/
	/**
	 * Used for debugging purpose. It simply writes the SIFObjects from the dataobjectinputstream to
	 * the log file.
	 */
	public void debugOnQueryResults(DataObjectInputStream dataobjectinputstream, SIF_Error sif_error, Zone zone, MessageInfo messageinfo) throws ADKException
	{
		logger.debug("===================================================================");
		logger.debug(getClass().getSimpleName() + ".onQueryResults() received from zone:" + zone.getZoneId());
		int numRecords = 0;
		while (dataobjectinputstream.available())
		{
			debugSIFObjectAsXML(dataobjectinputstream.readDataObject());
			numRecords++;
		}
		logger.debug("Number of Records received: " + numRecords);
		logger.debug("===================================================================");
	}

	/**
	 * Used for debugging purpose. It simply writes the SIFObjects from the event to the log file.
	 */
	public void debugOnEvent(Event event, Zone zone, MessageInfo messageinfo) throws ADKException
	{
		logger.debug("===================================================================");
		logger.debug(getClass().getSimpleName() + ".onEvent() received from zone:" + zone.getZoneId());
		logger.debug("Received event: " + event.getAction());

		if (isValidZone(zone))
		{
			int numRecords = 0;
			while (event.getData().available())
			{
				debugSIFObjectAsXML(event.getData().readDataObject());
				numRecords++;
			}
			logger.debug("Number of Records received: " + numRecords);
		}
		logger.debug("===================================================================");
	}

	/*-----------------*/
	/* Private methods */
	/*-----------------*/	
	/*
	 * This method retrieves the Inbound mapping for a given Message.
	 */
	private MappingInfo getInboundMappingInfo(MessageInfo msgInfo)
	{
		MappingInfo mappingInfo = new MappingInfo((SIFMessageInfo)msgInfo);
		if (getMappings() != null) // mappings are available
		{
			// Check if there is a mapping for this object
			try
			{
		    	MappingsContext mappingCtx = getMappings().selectInbound(getDtd(), (SIFMessageInfo)msgInfo);
				if (mappingCtx != null)
				{
					if ((ADK.debug == ADK.DBG_DETAILED) || (ADK.debug == ADK.DBG_VERY_DETAILED))
					{
				    	logger.debug("Mapping for Object: "+mappingCtx.getObjectDef().name());
				    	logger.debug("Numer of fields mapped: "+mappingCtx.getFieldMappings().size());
				    	logger.debug("Mapping Direction: "+mappingCtx.getDirection().name());
					}
			    	if ((mappingCtx.getFieldMappings() == null) || (mappingCtx.getFieldMappings().size() == 0))
					{
						mappingCtx = null;
					}
				}
				mappingInfo.setMappingCtx(mappingCtx);
		    	logger.debug("Mapping found for "+getDtd().name()+": "+(mappingInfo.getMappingCtx() != null));
			}
			catch (ADKMappingException ex)
			{
				logger.error("Failed retrieving mapping for subscriber "+getId()+": "+ex.getMessage(), ex);
			}
		}
		return mappingInfo;
	}

	private void debugSIFObjectAsXML(SIFDataObject sifObject)
	{
		if (sifObject != null)
		{
			logger.debug(sifObject.getClass().getSimpleName() + " Data in XML:\n" + sifObject.toXML());
		}
		else
		{
			logger.debug(getClass().getSimpleName() + ": No data available.");
		}
	}
}
