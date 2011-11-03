/*
 * XPathValue.java
 * Created: 20/10/2011
 *
 * Copyright 2011 Systemic Pty Ltd
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

/**
 * @author Joerg Huber
 *
 */
public class XPathValue implements Serializable
{
    private static final long serialVersionUID = 103947654987L;
    
	private String xpath = null;
	private String value = null;
	
	public XPathValue() {}
	
	public XPathValue(String xpath,String value)
	{
		setXpath(xpath);
		setValue(value);
	}

	public String getXpath()
    {
    	return this.xpath;
    }

	public void setXpath(String xpath)
    {
    	this.xpath = xpath;
    }

	public String getValue()
    {
    	return this.value;
    }

	public void setValue(String value)
    {
    	this.value = value;
    }
	
	@Override
	public String toString()
	{
		return "xpath = '"+xpath+"'  value = '"+value+"'";
	}
}
