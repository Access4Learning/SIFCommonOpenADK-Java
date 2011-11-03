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

import openadk.library.SIFDataObject;
import openadk.library.tools.mapping.ADKMappingException;

import systemic.sif.sifcommon.BaseInfo;
import systemic.sif.sifcommon.mapping.MappingInfo;


/**
 * For the publisher to be able to deal with large amounts of data it must implement this interface if the
 * publisher responds to SIF requests from subscribers. The general idea is that the constructor if the 
 * implementation retrieves the data in a manner that it can be iterated and load record by record into 
 * memory and returns them as part of the getNextSIFObject() method. This could be through SQL result sets,
 * cursors or any other means of retrieving large data amounts in a sequential manner rather than loading 
 * the entire data amount in memory and then return them all in one call.
 * 
 * @author Joerg Huber
 */
public interface SIFResponseIterator
{
    /**
	 * This method returns the next available SIF Object. The mappingInfo can be used to utilise the mapping
	 * mechanism available through the SIFWorks ADK functionality.<br />
	 * If there is no mapping defined for the given object then mappingInfo.mappingCtx of this parameter will
	 * be 'null'. It is up to the implementor of this method to check that property accordingly before use.
	 * 
	 * @param baseInfo The base info object of the given publisher. This allows the getNextSIFObject() method
	 *                 to access important properties and values of the publisher if it is needed.
      * @param mappingInfo The mapping info that can be used if mapping is available. Note that property 
     *                    mappingCtx of this parameter can be null if there is no mapping available 
     *                    for the given publisher and its SIF Object it deals with (ie. No mapping for 
     *                    StudentPersonal).
     *                    
     * @return SIFDataObject The next available SIF Object. This must return null if there are no further 
     * 					     SIF Object available.
     *                  
     * @throws ADKMappingException if mapping is used and there is an issue with the mapping.
     */
	public SIFDataObject getNextSIFObject(BaseInfo baseInfo, MappingInfo mappingInfo) throws ADKMappingException;
	
	/**
	 * Returns TRUE if there are more SIF Objects available. In this case the getNextSIFObject() method should
	 * return a SIF Object that is not null. FALSE is returned if there are no more SIF Objects available. In
	 * this case the getNextSIFObject() method should return null.
	 * 
	 * @return See description.
	 */
	public boolean hasNext();
	
	/**
	 * To be able to retrieve SIF Objects one by one there might be the need to allocate some resources in the
	 * class that implements this interface. These resources might be allocated until the last SIF Objects is
	 * returned to the publisher. This method is called by the BasePublisher class once all SIF Objects are 
	 * retrieved. It allows the implementor of this interface to release the allocated resources. Typical
	 * examples of allocated resources are DB Connections, SQL Result Sets etc.
	 */
	public void releaseResources();
}
