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
package systemic.sif.sifcommon.customObject;

import org.apache.log4j.Logger;

/**
 * It is not expected that one will manually create Custom Objects but if one has done so then they must
 * implement this interface. There is a high likelihood that this interface will become obsolete in the
 * near future. It is only supported for backwards compability.<p>
 * 
 * @author Joerg Huber
 *
 */
public interface CustomObjectInterface
{
  /**
   * Initialise this Custom Object. The passed in logger must already be initailsed as part of the agent.
   * @param logger
   */
  public void initialise(Logger logger);
}
