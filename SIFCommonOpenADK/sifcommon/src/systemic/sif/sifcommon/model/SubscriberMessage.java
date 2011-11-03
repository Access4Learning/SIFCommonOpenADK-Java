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
package systemic.sif.sifcommon.model;

import java.io.Serializable;

import openadk.library.EventAction;
import openadk.library.SIFDataObject;
import openadk.library.Zone;

import systemic.sif.sifcommon.mapping.MappingInfo;


/**
 * This class encapsulates a SIF message that has been received by a subscriber, might this be an event or a
 * response. It holds all required information relating to a SIF Message that is given by the ZIS to the 
 * subscriber.<p>
 * 
 * The main intent of this class is the ability to package all the parameters of the onEvent() or the 
 * onRequest() method in one object that can then be passed around to processes, threads, persistence etc.
 * 
 * @author Joerg Huber
 *
 */
public class SubscriberMessage extends BaseMessage implements Serializable
{
    private static final long serialVersionUID = 7354758860476L;
    
	/* Properties that applicable for Events and Responses */
	private boolean isEvent = false; // TRUE = This object represents a SIF Event, FALSE = Response
	private SIFDataObject sifObject = null;
	private Zone zone = null;
	private MappingInfo mappingInfo;
	
	/* Properties only applicable for SIF Events */
	private EventAction eventAction = null;
	
	/**
	 * Will create a default subscriber message with a auto-generated messageID.
	 */
	public SubscriberMessage() 
	{
		super(true);
	}

	/**
	 * Constructor if this object should represent a SIF Event. A auto-generated messageID will be assigned.<p>
	 * 
	 * @param sifObject The SIF Object for this Subscriber Message (Should not  be null).
	 * @param zone  The zone for this Subscriber Message.
	 * @param mappingInfo  The mapping info for this Subscriber Message.
	 * @param eventAction  The event action for this Subscriber Message (Should not  be null).
	 */
	public SubscriberMessage(SIFDataObject sifObject, Zone zone, MappingInfo mappingInfo, EventAction eventAction)
	{
		super(true);
		setSIFObject(sifObject);
		setZone(zone);
		setMappingInfo(mappingInfo);
		setEventAction(eventAction);
		setEvent(true);
	}
	
	/**
	 * Constructor if this object should represent a SIF Response. A auto-generated messageID will be assigned.<p>
	 * 
	 * @param sifObject The SIF Object for this Subscriber Message (Should not  be null).
	 * @param zone  The zone for this Subscriber Message.
	 * @param mappingInfo  The mapping info for this Subscriber Message.
	 */
	public SubscriberMessage(SIFDataObject sifObject, Zone zone, MappingInfo mappingInfo)
	{
		super(true);
		setSIFObject(sifObject);
		setZone(zone);
		setMappingInfo(mappingInfo);
		setEvent(false);		
	}
	
	public boolean isEvent()
    {
    	return isEvent;
    }

	public void setEvent(boolean isEvent)
    {
    	this.isEvent = isEvent;
    }

	public SIFDataObject getSIFObject()
    {
    	return sifObject;
    }

	public void setSIFObject(SIFDataObject sifObject)
    {
    	this.sifObject = sifObject;
    }

	public Zone getZone()
    {
    	return zone;
    }

	public void setZone(Zone zone)
    {
    	this.zone = zone;
    }

	public EventAction getEventAction()
    {
    	return eventAction;
    }

	public void setEventAction(EventAction eventAction)
    {
    	this.eventAction = eventAction;
    }
	
	public MappingInfo getMappingInfo()
    {
    	return mappingInfo;
    }

	public void setMappingInfo(MappingInfo mappingInfo)
    {
    	this.mappingInfo = mappingInfo;
    }
}
