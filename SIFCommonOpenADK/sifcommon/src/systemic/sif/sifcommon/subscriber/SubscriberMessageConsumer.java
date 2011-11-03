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

import openadk.library.ADK;
import openadk.library.SIFDataObject;

import org.apache.log4j.Logger;

import systemic.sif.sifcommon.model.SIFEvent;
import systemic.sif.sifcommon.model.SubscriberMessage;
import systemic.sif.sifcommon.subscriber.queue.SubscriberQueue;


/**
 * This class allows the subscriber to consume messages in a multi-threaded manner according to the
 * producer-consumer design pattern.<p>
 * 
 * @author Joerg Huber
 *
 */
public class SubscriberMessageConsumer implements Runnable
{
	protected Logger logger = ADK.getLog();

	private SubscriberQueue<SubscriberMessage> queue;
	private String consumerID;
	private BaseSubscriber subscriber;
	
	/**
	 * This method initialises a Consumer for SubscriberMessages. The 'subscriber' parameter is required 
	 * by the consumer when messages are processed. At this point the consumer will call the appropriate
	 * method on the subscriber: processResponse() for response type of SubscriberMessage or processEvent()
	 * for event type of SubscriberMessage.<p>
	 * 
	 * @see systemic.sif.sifcommon.subscriber.BaseSubscriber#processEvent
	 * @see systemic.sif.sifcommon.subscriber.BaseSubscriber#processResponse
	 *  
	 * @param queue The queue on which this consumer will listen on.
	 * @param consumerID A name of the consumer. Mainly needed for nice debug and error reporting.
	 * @param subscriber The subscriber to which this consumer will be assigned to.
	 */
	public SubscriberMessageConsumer(SubscriberQueue<SubscriberMessage> queue, String consumerID, BaseSubscriber subscriber)
	{
		this.queue = queue;
		this.consumerID = consumerID;
		this.subscriber = subscriber;
	}
	
	/**
	 * Required for this class to run in its own thread.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	//@Override
    public void run()
    {
		consume();
    }
	
	/*-----------------*/
	/* Private methods */
	/*-----------------*/
	/*
	 * This method will run in an infinite loop and try to retrieve messages from the SubscriberQueue. Once
	 * a message is retrieved it will determine if it is a Event or a response message and then call the 
	 * appropriate message in the subscriber agent.
	 */
	private void consume()
	{
		while (true)
		{
			SubscriberMessage sifMsg = queue.blockingPull();				
			if (sifMsg != null)
			{
				logger.debug(consumerID+" has receive a message from its SubscriberQueue.");
				if (sifMsg.isEvent())
				{
					SIFEvent sifEvent = new SIFEvent(sifMsg.getSIFObject(), sifMsg.getEventAction());
					try
					{
						subscriber.processEvent(sifEvent, sifMsg.getZone(), sifMsg.getMappingInfo(), consumerID);

					}
					catch (Exception ex)
					{
						logger.error("Failed processing SIF Event for subscriber "+subscriber.getId()+": "+ex.getMessage()+"\nEvent Data:\n"+sifEvent, ex);				
					}
				}
				else
				{
					SIFDataObject sifObj = sifMsg.getSIFObject();
					try
					{
						subscriber.processResponse(sifObj, sifMsg.getZone(),  sifMsg.getMappingInfo(), consumerID);
					}
					catch (Exception ex)
					{
						logger.error("Failed processing SIF Object for subscriber "+subscriber.getId()+": "+ex.getMessage()+"\nSIF Object Data:\n"+((sifObj == null) ? "null" : sifObj.toXML()), ex);				
					}
				}
			}
			else
			{
				logger.error(consumerID+" has encountered a problem receiving a message from its SubscriberQueue.");
			}
		}	
	}
}
