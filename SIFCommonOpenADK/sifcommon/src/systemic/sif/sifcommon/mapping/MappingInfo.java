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
package systemic.sif.sifcommon.mapping;

import java.io.Serializable;

import openadk.library.SIFMessageInfo;
import openadk.library.tools.mapping.MappingsContext;


/**
 * This is a simple class that links the mapping context to the appropriate SIF Message Info Object.
 * This structure is used when events or sif requests/responses are either created (publisher) or processed 
 * (subscriber). The MappingsContext can be null (no mapping available) or holding the appropriate 
 * Outbound (publisher) or Inbound (subscriber) mapping information for the SIF Object the message is 
 * applicable to.<p>
 * 
 * @see systemic.sif.sifcommon.subscriber.BaseSubscriber  
 * @see systemic.sif.sifcommon.publisher.BasePublisher
 * 
 * @author Joerg Huber
 *
 */
public class MappingInfo implements Serializable
{
    private static final long serialVersionUID = 7534355635540L;
    
	private SIFMessageInfo sifMsgInfo = null;
	private MappingsContext mappingCtx = null;  

	public MappingInfo()
	{
		this(null, null);
	}

	public MappingInfo(SIFMessageInfo sifMsgInfo)
	{
		this(sifMsgInfo, null);
	}

	public MappingInfo(SIFMessageInfo sifMsgInfo, MappingsContext mappingCtx)
	{
		setSIFMsgInfo(sifMsgInfo);
		setMappingCtx(mappingCtx);
	}

	public SIFMessageInfo getSifMsgInfo()
	{
		return sifMsgInfo;
	}

	public void setSIFMsgInfo(SIFMessageInfo sifMsgInfo)
	{
		this.sifMsgInfo = sifMsgInfo;
	}

	public MappingsContext getMappingCtx()
	{
		return mappingCtx;
	}

	public void setMappingCtx(MappingsContext mappingCtx)
	{
		this.mappingCtx = mappingCtx;
	}
}
