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
import java.util.Date;

import au.com.systemic.framework.utils.GUIDGenerator;

/**
 * This class is primarily used as a base class for any messages that ought to be used in a SubscriberQueue.
 * It has properties that might be needed if persistence is implemented in the SubscriberQueue.
 * 
 * @see systemic.sif.sifcommon.subscriber.queue.SubscriberQueue
 * 
 * @author Joerg Huber
 *
 */
public class BaseMessage implements Serializable
{
    private static final long serialVersionUID = 329475200476L;

    private String messageGUID = null;
    private Date creationDate = new Date();
    private int numRetries = 0;

    /**
     * This constructor will create this object with the creation date set to now.
     */
	public BaseMessage() 
	{
		this(false);
	}
    
	/**
	 * This constructor will create this object with a generated GUID if autoGenerate=TRUE otherwise no
	 * GUID is assigned at this point. The creation date will be set to now.
	 * 
	 * @param autoGenerate TRUE=>A GUID will be assigned to the object.
	 */
    public BaseMessage(boolean autoGenerate) 
    {
    	this( autoGenerate ? GUIDGenerator.getRandomGUID() : null);
    }
    
    /**
     * This constructor will create this object with the given messageGUID. The creation date will be set to
     * now.
	 * 
     * @param messageGUID Sets the messageGUID of this object to that value.
     */
    public BaseMessage(String messageGUID) 
    {
    	setMessageGUID(messageGUID);
    }
    
	public String getMessageGUID()
    {
    	return messageGUID;
    }

	public void setMessageGUID(String messageGUID)
    {
    	this.messageGUID = messageGUID;
    }
    
    public Date getCreationDate()
    {
    	return creationDate;
    }

	public void setCreationDate(Date creationDate)
    {
    	this.creationDate = creationDate;
    }

	public int getNumRetries()
    {
    	return numRetries;
    }

	public void setNumRetries(int numRetries)
    {
    	this.numRetries = numRetries;
    }
}
