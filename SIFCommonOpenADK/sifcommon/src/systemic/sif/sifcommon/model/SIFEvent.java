/*
 * Copyright 2010-2011 Systemic Pty Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package systemic.sif.sifcommon.model;

import java.io.Serializable;

import openadk.library.EventAction;
import openadk.library.SIFDataObject;


/**
 * This is a simple POJO to encapsulate a SIF Object and its associated SIF Event Action. Throughout
 * the SIFWorks ADK the event is always referenced with two parameters. Within the SIFCommon
 * Framework these two parameters are stored in this object to simplify method calls within the
 * framework.
 * 
 * @author Joerg Huber
 * 
 */
public class SIFEvent implements Serializable
{
	private static final long serialVersionUID = 7122348903009L;

	private SIFDataObject sifObject;
	private EventAction eventAction;

	public SIFEvent(){}

	public SIFEvent(SIFDataObject sifObject, EventAction eventAction)
	{
		setSifObject(sifObject);
		setEventAction(eventAction);
	}

	public SIFDataObject getSifObject()
	{
		return sifObject;
	}

	public void setSifObject(SIFDataObject sifObject)
	{
		this.sifObject = sifObject;
	}

	public EventAction getEventAction()
	{
		return eventAction;
	}

	public void setEventAction(EventAction eventAction)
	{
		this.eventAction = eventAction;
	}

	@Override
	public String toString()
	{
		return "eventAction: " + eventAction.name() + "\nsifObject:\n" + sifObject.toXML();
	}
}
