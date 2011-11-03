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
package systemic.sif.sifcommon.publisher;

import java.util.ArrayList;
import java.util.List;

import openadk.library.ADKException;
import openadk.library.DataObjectOutputStream;
import openadk.library.Event;
import openadk.library.EventAction;
import openadk.library.MessageInfo;
import openadk.library.Publisher;
import openadk.library.PublishingOptions;
import openadk.library.Query;
import openadk.library.SIFDataObject;
import openadk.library.SIFException;
import openadk.library.SIFMessageInfo;
import openadk.library.SIFParser;
import openadk.library.Zone;
import openadk.library.tools.mapping.ADKMappingException;
import openadk.library.tools.mapping.MappingsContext;

import systemic.sif.sifcommon.BaseInfo;
import systemic.sif.sifcommon.mapping.MappingInfo;
import systemic.sif.sifcommon.model.SIFEvent;
import systemic.sif.sifcommon.utils.SIFCommonProperties;
import au.com.systemic.framework.utils.FileReaderWriter;
import au.com.systemic.framework.utils.StringUtils;


/**
 * All publishers should extend this class. It ensures that resources are dealt with correctly.
 * 
 * @author Joerg Huber
 */
public abstract class BasePublisher extends BaseInfo implements Publisher, Runnable
{
	private PublishingOptions options = new PublishingOptions(true);
	
    /**
     * This class is a basic initialiser of a publisher. No properties except the publisherID and
     * DTD for the applicable publisher are expected to be set in this method. The BaseAgent will call
     * this method first, then set appropriate properties of the BaseInfo class.<p>
     * 
     * @param publisherID The unique ID of this publisher. Ideally this is the class name but this is not
     *                    necessary. This ID is used throughout the SIFCommon Framework classes to access
     *                    properties in the SIFAgent.properties file. This ID must match the reference
     *                    in this property file for the given publisher.
     */
	public BasePublisher(String publisherID)
	{
		super(publisherID);
	}	
	      
    /*------------------*/
    /* Abstract Methods */
    /*------------------*/

    /**
     * This method returns a SIFEventIterator. This needs to be implemented (i.e. not returning null) if the 
     * Agent should publish events. The method broadcastEvents() will utilise this method to retrieve all the events 
     * to be published. It is up to the Agent that implements a publisher to decide the following:<br />
     * a) Do events need to be published (ie. if not then this method can be  implemented as 'return null')<br />
     * b) The frequency/method (i.e. event driven or polling) how events are published.<p>
     * 
     * @return SIFEventIterator An iterator through which the broadcast() method retrieve event by event and
     *                          publish it to all the zones listed in the Agent's configuration file.
     *                             
     * @throws ADKException If there is an error with retrieving data.
     */
    public abstract SIFEventIterator getSIFEvents() throws ADKException;
    
    /**
     * This method must returns a SIFResponseIterator. This needs to be implemented (i.e. not returning null) 
     * if the should respond to SIF Requests from a subscriber. The internal method onRequest() will utilise
     * this method to finally send the objects to the ZIS. The returned object must meet the given SIFQuery.<p>
     * 
     * @param query The query this agent is requested to meet.
     * @param zone The Zone that for which the request has been received.
     *                    
     * @return SIFResponseIterator An iterator through which the onRequest() method retrieve SIF Objects
     *                             one by one and responses to the original request.
     *                             
     * @throws ADKException If there is an error with retrieving data.
     * @throws SIFException If the query cannot be dealt with (ie not supported). In this case the 
     * following fields must be set:<br />
     *    Error Category of SIFErrorCodes.CAT_REQRSP_8 and<br />
     *    An Error Code of SIFErrorCodes.REQRSP_UNSUPPORTED_QUERY_9
     */
	public abstract SIFResponseIterator getRequestedSIFObjects(Query query, Zone zone) throws ADKException, SIFException;

    /**
     * This method should release all resources associated with the given publisher. It will be called as part
     * of the shutdown process of the agent.<p>
     */
    public abstract void finalise();


    /*---------------------*/
    /* Setters and Getters */
    /*---------------------*/
    public PublishingOptions getOptions()
    {
        return options;
    }

    public void setOptions(PublishingOptions options)
    {
        this.options = options;
    }

    /*----------------------------------------*/
    /* Implemented Method for Multi-threading */
    /*----------------------------------------*/
	/**
	 * This method is all that is needed to run the publisher in its own thread. The thread is executed at
	 * given intervals driven by a property in the SIFCommonFramework property file. The interval/frequency
	 * defined in there is used to determine how often this thread is run.
	 */
    //@Override
    public final void run()
    {
		boolean sendEvents = (getFrameworkProperties().getEventFrequencyInSeconds(getAgentID(), getId(), SIFCommonProperties.NO_EVENT) != SIFCommonProperties.NO_EVENT);
    	logger.debug("Thread woken up for Publisher "+getId()+". Event sending required: "+sendEvents);
		
		if (sendEvents)
		{
	    	logger.debug("Start sending events for Publisher "+getId()+"...");
			broadcastEvents();
			logger.debug("Sending all events to all zones for Publisher "+getId()+" complete.");
		}
		
		logger.debug("Run() for Publisher "+getId()+" finished.");
    }
   
    /*-------------------------*/
    /* Request related methods */
    /*-------------------------*/
    /**
     * This is the implementation of the SIFWorks ADK method. It calls the abstract method getRequestedSIFObjects()
     * that is defined in this class. It iterates through all objects and sends it as a response to the
     * subscriber that requested the data.<br/>
     * It is not expected that any sub-classes call this method at all.
     *
     * @see #getRequestedSIFObjects
     */
    public final void onRequest(DataObjectOutputStream dataobjectoutputstream, Query query, Zone zone, MessageInfo msgInfo) throws ADKException
	{
    	logger.debug("================================ onRequest() called for publisher "+getId());
		int totalRecords = 0;
		int failedRecords = 0;    	
		MappingInfo mappingInfo = new MappingInfo((SIFMessageInfo)msgInfo, getOutboundMappingCtx((SIFMessageInfo)msgInfo));
		SIFResponseIterator iterator = getRequestedSIFObjects(query, zone);
		if (iterator != null)
		{
			while (iterator.hasNext())
			{
				try
				{
					SIFDataObject sifObj = iterator.getNextSIFObject(this, mappingInfo);
					// This should not return null since the hasNext() returned true, but just in case we check
					// and exit the loop if it should return null. In this case we assume that there is no more
					// data. We also log an error to make the coder aware of the issue.
					if (sifObj != null)
					{
						sendData(dataobjectoutputstream, sifObj);
						totalRecords++;
					}
					else
					{
						logger.error("iterator.hasNext() has returned true but iterator.getNextSIFObject() has retrurned null => no further SIF Object are sent.");
						break;
					}
				}
				catch (Exception ex)
				{
					logger.error("Failed to retrieve next sif object for publisher "+getId()+": "+ex.getMessage(), ex);
					failedRecords++;
				}
			}
			iterator.releaseResources();
		}
		else
		{
			logger.info("getRequestedSIFObjects() for publisher "+getId()+" returned null.");
		}
		logger.info("Total "+getDtd().name()+" Objects sent in response: "+totalRecords);
		logger.info("Total "+getDtd().name()+" Objects failed          : "+failedRecords);
    	logger.debug("================================ Finished onRequest() for publisher "+getId());
	}
    
    /**
     * This method sends a sifObject to a DataObjectOutputStream (response to a SIF Query). If there are any issues the 
     * error is logged and the object is not sent.
     * 
     * @param dataobjectoutputstream The response stream where the object needs to be sent to.
     * @param sifObject The object to send.
     */
    protected void sendData(DataObjectOutputStream dataobjectoutputstream, SIFDataObject sifObject)
    {
        try
        {
            dataobjectoutputstream.write(sifObject);
        }
        catch (Exception ex)
        {
            logger.error(getId() + "-onRequest: Failed to send " + sifObject.getClass().getSimpleName() + " to zone: " + ex.getMessage());
            logger.error(getId() + "-onRequest: " + sifObject.getClass().getSimpleName() + " data: " + sifObject.toXML(), ex);
        }
    }

    /*---------------------------*/
    /* SIF Event related methods */
    /*---------------------------*/
    
    /**
     * This method retrieves all events to be published by calling the abstract method getSIFEvents(). The returned list
     * is then broadcasted to all zones known to the implementing agent.
     * 
     * @see #getSIFEvents
     */
    public void broadcastEvents()
    {
    	logger.debug("================================ broadcastEvents() called for publisher "+getId());
    	MappingsContext mappingCtx = getOutboundMappingCtx(null);
		int totalRecords = 0;
		int failedRecords = 0;
		try
		{
			SIFEventIterator iterator = getSIFEvents();
			if (iterator != null)
			{
				while (iterator.hasNext())
				{
					try
					{
						SIFEvent sifEvent = iterator.getNextEvent(this, mappingCtx);
						// This should not return null since the hasNext() returned true, but just in case we check
						// and exit the loop if it should return null. In this case we assume that there is no more
						// data. We also log an error to make the coder aware of the issue.
						if (sifEvent != null)
						{
				            for (Zone zone : getZones())
				            {
				                logger.debug(getId() + " broadcast event to zone: " + zone.getZoneId());
				                sendEvent(sifEvent, zone);
				            }
				            totalRecords++;
						}
						else
						{
							logger.error("iterator.hasNext() has returned true but iterator.getNextEvent() has retrurned null => no further events are broadcasted.");
							break;
						}
					}
					catch (Exception ex)
					{
						logger.error("Failed to retrieve next event for publisher "+getId()+": "+ex.getMessage(), ex);					
						failedRecords++;
					}
				}
				iterator.releaseResources();
			}
			else
			{
				logger.info("getSIFEvents() for publisher "+getId()+" returned null.");
			}
		}
		catch (Exception ex)
		{
			logger.error("Failed to retrieve events for publisher "+getId()+": "+ex.getMessage(), ex);								
		}
		logger.info("Total SIF Events broadcasted: "+totalRecords);
		logger.info("Total SIF Events failed     : "+failedRecords);
    	logger.debug("================================ Finished broadcastEvents() for publisher "+getId());
    }

    /**
     * If one doesn't want certain events to be published to a given zone then this method needs to be 
     * overridden. It allows to test for the event and zone and make the appropriate decision if the event
     * shall be sent.
     * 
     * @param event The event to be published to the zone.
     * @param zone The zone to which the event is published to.
     */
    protected void sendEvent(SIFEvent event, Zone zone)
    {
		try
		{
			Event sifEvent = new Event(event.getSifObject(), event.getEventAction());
			zone.reportEvent(sifEvent);
		}
		catch (Exception ex)
		{
			logger.error(getId() + " failed to broadcast to zone " + zone.getZoneId() + ": " + ex.getMessage());
			logger.error("SIFObject: " + event.toString());
		}
    }
    
	/**
	 * This method shuts down this publisher gracefully. It is called by the Agent when a shutdown request
	 * has been issued to the agent. It is not expected that sub-classes of this class call this method.
	 * Specifics of the sub-class shutdowns must be handled in the finalise() method of the sub-class. The 
	 * finalise() method is called as part of this method.
	 */
	public final void shutdownPublisher()
	{
		finalise();
	}
	
	/*-----------------*/
	/* Private methods */
	/*-----------------*/
	private MappingsContext getOutboundMappingCtx(SIFMessageInfo msgInfo)
	{
		MappingsContext mappingCtx = null;
		if (getMappings() != null) // mappings are available
		{
			// Check if there is a mapping for this object
			try
			{
		    	//logger.debug("Check if there is a mapping for object "+getDtd().name());
				if (msgInfo != null)
				{
					mappingCtx = getMappings().selectOutbound(getDtd(), msgInfo);
				}
				else
				{
					mappingCtx = getMappings().selectOutbound(getDtd(), null, null, null);
				}

				if (mappingCtx != null)
				{
			    	logger.debug("Mapping for Object: "+mappingCtx.getObjectDef().name());
			    	logger.debug("Numer of fields mapped: "+mappingCtx.getFieldMappings().size());
			    	logger.debug("Mapping Direction: "+mappingCtx.getDirection().name());

					if ((mappingCtx.getFieldMappings() == null) || (mappingCtx.getFieldMappings().size() == 0))
					{
						mappingCtx = null;
					}
				}
		    	logger.debug("Mapping exists for "+getDtd().name()+": "+(mappingCtx != null));
			}
			catch (ADKMappingException ex)
			{
				logger.error("Failed retrieving mapping context for Publisher "+getId()+": "+ex.getMessage(), ex);
				mappingCtx = null;
			}
		}

		return mappingCtx;
	}
    /*--------------------------*/
    /* Methods used for testing */
    /*--------------------------*/

    public String loadXMLFileData(String fullPathAndName)
    {
    	logger.debug("loadXMLFileData() called ...");
        String xmlStr = null;
        xmlStr = FileReaderWriter.getFileContent(fullPathAndName);
        if (xmlStr == null)
        {
            logger.error("No file or empty file found at '" + fullPathAndName + "'");
        }
    	logger.debug("Size of XML String read: "+((xmlStr==null)?null:xmlStr.length()));

        return xmlStr;
    }
   
    public List<SIFDataObject> getObjectsFromFile()
    {
    	logger.debug("getObjectsFromFile() called ...");
        ArrayList<SIFDataObject> sifObjs = null;
        String fileName = getDtd().name();
        String testDir = getFrameworkProperties().getTestDir(getAgentID());
        String fullName = null;
        
		if (StringUtils.notEmpty(fileName))
		{
			fullName = (StringUtils.notEmpty(testDir) ? testDir+"/"+fileName : fileName)+".xml";
		}

    	logger.debug("Read SIF Object from file: "+fullName);
		if (fullName != null)        
        {
            String sifMsg = loadXMLFileData(fullName);
        	logger.debug("Message loaded");
            if (sifMsg != null)
            {
            	logger.debug("Message not null...");
                try
                {
                    SIFParser parser = SIFParser.newInstance();
                    sifObjs = new ArrayList<SIFDataObject>();
                    List<String> xmlObjs = StringUtils.splitXML(sifMsg, getDtd().name());
                	logger.debug("Number of messages in String: "+xmlObjs.size());
                    for (String xmlMsg : xmlObjs)
                    {
                        sifObjs.add((SIFDataObject)parser.parse(xmlMsg));
                    	//logger.debug("XML Message: "+xmlMsg);
                    	//logger.debug("Attempt to create SIF Object of Type: "+getDtd().name());
                    	
                    	logger.debug("SIF Object parsed and created.");
                    }
                }
                catch (Exception ex)
                {
                    logger.error("Failed to parse '" + fullName+"':\n" + sifMsg, ex);
                }
            }
        }
    	logger.debug("Total SIF Objects found from file: "+sifObjs.size());
        return sifObjs;
    } 
    
    public List<SIFEvent> getEventsFromFile()
    {
    	logger.debug("getEventsFromFile() called ...");
    	List<SIFEvent> events = new ArrayList<SIFEvent>();
    	List<SIFDataObject> sifObjects = getObjectsFromFile();
    	if (sifObjects != null)
    	{
    		for (SIFDataObject sifObj : sifObjects)
    		{
    			events.add(new SIFEvent(sifObj, EventAction.CHANGE));
    		}
    	}
    	logger.debug("Total events found from file: "+events.size());
    	return events;
    }
}
