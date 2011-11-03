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

import openadk.library.tools.mapping.ADKMappingException;
import openadk.library.tools.mapping.MappingsContext;

import systemic.sif.sifcommon.BaseInfo;
import systemic.sif.sifcommon.model.SIFEvent;


/**
 * For the publisher to be able to deal with large amounts of data it must implement this interface if the
 * publisher broadcasts events. The general idea is that the constructor if the implementation retrieves the
 * data in a manner that it can be iterated and retrieve record by record into memory and returns them as 
 * part of the getNextEvent() method. This could be through SQL result sets, cursors or any other means of 
 * retrieving large data amounts in a sequential manner rather than loading the entire data amount in memory
 * and then return them all in one call.<p>
 * 
 * @author Joerg Huber
 */
public interface SIFEventIterator
{
	/**
	 * This method returns the next available SIF Event. The mappingCtx can be used to utilise the mapping
	 * mechanism available through the SIFWorks ADK functionality.<br />
	 * If there is no mapping defined for the given object then this parameter will be 'null'. It is up to
	 * the implementor of this method to check that parameter accordingly before use.
	 * 
	 * @param baseInfo The base info object of the given publisher. This allows the getNextEvent() method
	 *                 to access important properties and values of the publisher if it is needed.
     * @param mappingCtx The mapping info that can be used if mapping is available. Note that this parameter
     *                   can be null if there is no mapping available for the given publisher and its SIF 
     *                   Object it deals with (ie. No mapping for StudentPersonal). The mappingCtx is for
     *                   outbound mappings as expected since it is publisher that will use the mapping.
     *                   
     * @return SIFEvent The next available SIFEvent. This must return null if there are no further SIF Events
     *                  available.
     *                  
     * @throws ADKMappingException if mapping is used and there is an issue with the mapping.
     */
	public SIFEvent getNextEvent(BaseInfo baseInfo, MappingsContext mappingCtx) throws ADKMappingException;
	
	/**
	 * Returns TRUE if there are more SIF Events available. In this case the getNextEvent() method should
	 * return a SIF Event that is not null. FALSE is returned if there are no more SIF Events available. In
	 * this case the getNextEvent() method should return null.
	 * 
	 * @return See description.
	 */
	public boolean hasNext();
	
	/**
	 * To be able to retrieve SIF Events one by one there might be the need to allocate some resources in the
	 * class that implements this interface. These resources might be allocated until the last SIF Event is
	 * returned to the publisher. This method is called by the BasePublisher class once all events are 
	 * retrieved. It allows the implementor of this interface to release the allocated resources. Typical
	 * examples of allocated resources are DB Connections, SQL Result Sets etc.
	 */
	public void releaseResources();
}
