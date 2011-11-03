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
package systemic.sif.sifcommon;

import java.util.List;

import openadk.library.ADK;
import openadk.library.AgentProperties;
import openadk.library.ElementDef;
import openadk.library.Zone;
import openadk.library.tools.cfg.AgentConfig;
import openadk.library.tools.mapping.Mappings;

import org.apache.log4j.Logger;

import systemic.sif.sifcommon.utils.SIFCommonProperties;


/**
 * This class should be extended by all subscribers and publishers because the Agents expect to have
 * access to that information.
 * 
 * @author Joerg Huber
 */
public class BaseInfo
{	
	/** Classes that extend this class can use this logger directly */
	protected Logger logger = ADK.getLog();

	/* ID of the publisher or subscriber */
	private String id = null;
	
	/* The DTD of the SIF Object this publisher or subscriber deals with */
	private ElementDef dtd = null;
	
	/* Gives access to the SIF Common Framework property file */
	private SIFCommonProperties frameworkProperties = null;
	
	/* Properties from the SIFWorks ADK */
	private AgentProperties agentProperties = null;
	
	/* Agent Configuration from the SIFWorks ADK */
	private AgentConfig agentConfig = null;
	
	/* The agent ID for which this publisher/subscriber BaseInfo is applicable. */
	private String agentID = null;
	
	/* The application ID for which this publisher/subscriber BaseInfo is applicable. */
	private String applicationID = null;
	
	/* The list of zones this publisher/subscriber operates in */
	private List<Zone> zones = null;
	
	/* Mappings available for this agent's publishers/subscribers */
	private Mappings mappings = null;

	/* Default constructor */
	public BaseInfo(){}
	
	/**
	 * Constructor: Assign a unique ID. Will help for logging and debugging.
	 * 
	 * @param id Unique ID for this object which is either a publisherID or subscriberID.
	 */
	public BaseInfo(String id)
	{
		setId(id);
	}

	/*---------------------*/
	/* Setters and Getters */
	/*---------------------*/
	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public ElementDef getDtd()
	{
		return dtd;
	}

	public void setDtd(ElementDef dtd)
	{
		this.dtd = dtd;
	}

	public SIFCommonProperties getFrameworkProperties()
	{
		return frameworkProperties;
	}

	public void setFrameworkProperties(SIFCommonProperties frameworkProperties)
	{
		this.frameworkProperties = frameworkProperties;
	}

	public AgentProperties getAgentProperties()
	{
		return agentProperties;
	}

	public String getAgentID()
    {
    	return agentID;
    }

	public void setAgentID(String agentID)
    {
    	this.agentID = agentID;
    }

	public void setAgentProperties(AgentProperties agentProperties)
	{
		this.agentProperties = agentProperties;
	}

	public List<Zone> getZones()
    {
    	return zones;
    }

	public void setZones(List<Zone> zones)
    {
    	this.zones = zones;
    }

	public AgentConfig getAgentConfig()
    {
    	return agentConfig;
    }

	public void setAgentConfig(AgentConfig agentConfig)
    {
    	this.agentConfig = agentConfig;
    }

	public Mappings getMappings()
    {
    	return mappings;
    }

	public void setMappings(Mappings mappings)
    {
    	this.mappings = mappings;
    }

	public String getApplicationID()
    {
    	return this.applicationID;
    }

	public void setApplicationID(String applicationID)
    {
    	this.applicationID = applicationID;
    }

	/*---------------------*/
	/* Convenience methods */
	/*---------------------*/
	public String getAgentProperty(String propertyName)
	{
		return agentProperties.getProperty(propertyName);
	}
	
    public boolean isValidZone(Zone zoneToTest)
    {
        List<Zone> zones = getZones();
        if (zones != null)
        {
	        for (Zone zone : zones)
	        {
	            if (zone.getZoneId().equalsIgnoreCase(zoneToTest.getZoneId()))
	            {
	                logger.debug("Zone " + zoneToTest.getZoneId() + " is valid.");
	                return true;
	            }
	        }
        }
        logger.debug("Zone " + zoneToTest.getZoneId() + " is invalid.");
        return false;
    }
    
    /**
     * This method returns the Zone information for the given zoneID. If no zone is know for the given zoneID
     * then null is returned.
     * 
     * @param zoneID The id of the zone for which the Zone information shall be returned.
     * 
     * @return See Description
     */
    public Zone getZoneByID(String zoneID)
    {
    	List<Zone> zones = getZones();
        if (zones != null)
        {
	        for (Zone zone : zones)
	        {
	            if (zone.getZoneId().equalsIgnoreCase(zoneID))
	            {
	                logger.debug("Zone " + zoneID + " found.");
	                return zone;
	            }
	        }
        }
        
        // No zone found for the given zoneID
        return null;    	
    }
}
