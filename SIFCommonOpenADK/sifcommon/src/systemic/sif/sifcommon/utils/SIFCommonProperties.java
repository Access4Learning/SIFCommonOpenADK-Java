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
package systemic.sif.sifcommon.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import systemic.sif.sifcommon.agent.SIFBaseAgent;
import au.com.systemic.framework.utils.StringUtils;

/**
 * This class provides a number of handy methods to access the values of the SIFCommon Framework properties
 * file. Generally this file is called SIFAgent.properties but it doesn't have to.
 * 
 * @author Joerg Huber
 *
 */
public class SIFCommonProperties
{
	/**
	 * Constant to check if the event frequency is indicating that no events should be sent.
	 */
	public static final int NO_EVENT = 0;

	/**
	 * Constant to check if the sync frequency is indicating that no sync should be performed.
	 */
	public static final int NO_SYNC = 0;

	private Properties properties =null;
	
	/**
	 * Name of the SIFCommon Framework Property file. This is the file that holds the names and locations
	 * of all publishers, subscribers etc. The property file must be on the classpath.
	 * 
	 * @param propertyFileName File Name without the extension '.properties'.
	 */
	public SIFCommonProperties(String propertyFileName)
	{
		properties = null;
        try
        {
        	properties = new Properties();
        	properties.load(SIFBaseAgent.class.getClassLoader().getResourceAsStream(propertyFileName+".properties"));
        }
        catch (Exception ex)
        {
            System.out.println("Error accessing/loading " + propertyFileName + ".properties.");
            propertyFileName = null;
        } 
	}

	/**
	 * Same as the method above but it assumes that the property file is called SIFAgent.properties.
	 */
	public SIFCommonProperties()
	{
		this("SIFAgent");
	}
	
	/**
	 * This method returns the applicationID for the given agent. If that doesn't exist then
	 * the defaultValue is returned.<p>
	 * 
	 * Each agent should be responsible for a particular application. More than one agent can be
	 * responsible for the same application where they each deal with different SIF Objects.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.application=_applicationID_</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the application shall be returned.
	 * @param defaultValue The default value to be returned if no frequency can be found.
	 * 
	 * @return See description.
	 */
	public String getApplicationID(String agentID, String defaultValue)
	{
		return getPropertyAsString("agent."+agentID+".application" ,defaultValue);		
	}
	
	/**
	 * This method returns the event frequency for the given agent and publisher. If no such frequency exists
	 * then it will attempt to return the default frequency of this agent. If that doesn't exist either then
	 * the defaultValue is returned.<p>
	 * 
	 * The frequency should be interpreted as seconds.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_._publisherID_.event.frequency=_int_value_</code><p>
	 * or<p>
	 * <code>agent._agentID_.event.frequency=_int_value_</code><p>
	 * in case of the global setting
	 * 
	 * @param agentID The agentID of the agent for which the frequency shall be returned.
	 * @param publisherID The publisherID of the publisher for which the frequency shall be returned.
	 * @param defaultValue The default value to be returned if no frequency can be found.
	 * 
	 * @return See description.
	 */
	public int getEventFrequencyInSeconds(String agentID, String publisherID, int defaultValue)
	{
		Integer publisherValue= getPropertyAsInt("agent."+agentID+"."+publisherID+".event.frequency");
		return (publisherValue != null) ? publisherValue.intValue() : getEventFrequencyInSeconds(agentID, defaultValue);
	}
	
	/**
	 * This method returns the default event frequency for the given agent. If that doesn't exist then
	 * the defaultValue is returned.<p>
	 * 
	 * The frequency should be interpreted as seconds.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.event.frequency=_int_value_</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the frequency shall be returned.
	 * @param defaultValue The default value to be returned if no frequency can be found.
	 * 
	 * @return See description.
	 */
	public int getEventFrequencyInSeconds(String agentID, int defaultValue)
	{
		return getPropertyAsInt("agent."+agentID+".event.frequency" ,defaultValue);
	}
	
	/**
	 * This method returns the sync frequency for the given agent and subscriber. If no such frequency exists
	 * then it will attempt to return the default frequency of this agent. If that doesn't exist either then
	 * the defaultValue is returned.<p>
	 * 
	 * The frequency should be interpreted as seconds.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_._subscriber_.sync.frequency=_int_value_</code><p>
	 * or<p>
	 * <code>agent._agentID_.sync.frequency=_int_value_</code><p>
	 * in case of the global setting
	 * 
	 * @param agentID The agentID of the agent for which the frequency shall be returned.
	 * @param subscriberID The subscriberID of the subscriber for which the frequency shall be returned.
	 * @param defaultValue The default value to be returned if no frequency can be found.
	 * 
	 * @return See description.
	 */
	public int getSyncFrequencyInSeconds(String agentID, String subscriberID, int defaultValue)
	{
		Integer value= getPropertyAsInt("agent."+agentID+"."+subscriberID+".sync.frequency");
		return (value != null) ? value.intValue() : getSyncFrequencyInSeconds(agentID, defaultValue);
	}
	
	/**
	 * This method returns the default sync frequency for the given agent. If that doesn't exist then
	 * the defaultValue is returned.<p>
	 * 
	 * The frequency should be interpreted as seconds.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.sync.frequency=_int_value_</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the frequency shall be returned.
	 * @param defaultValue The default value to be returned if no frequency can be found.
	 * 
	 * @return See description.
	 */
	public int getSyncFrequencyInSeconds(String agentID, int defaultValue)
	{
		return getPropertyAsInt("agent."+agentID+".sync.frequency" ,defaultValue);
	}
	
	/**
	 * This method returns the multi-threaded indicator for the given agent. If that doesn't exist then
	 * the defaultValue is returned.<p>
	 * 
	 * Valid values in the property file for this property is 'true' or 'false'.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.multiTreaded=true|false</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the multi-threaded indicator shall be returned.
	 * @param defaultValue The default value to be returned if indicator can be found.
	 * 
	 * @return See description.
	 */
	public boolean getMultiThreaded(String agentID, boolean defaultValue)
	{
		return getPropertyAsBool("agent."+agentID+".multiTreaded" ,defaultValue);
	}
	
	/**
	 * Returns the SIFWorks ADK configuration file directory for the given agent. If the 
	 * property doesn't exist then null is returned.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.cfg.location=_path_to_config_file</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the SIFWorks ADK configuration file directory shall
	 * be returned.
	 * 
	 * @return See description.
	 */
	public String getADKConfigDir(String agentID)
	{
		return getPropertyAsString("agent." + agentID + ".cfg.location", null);
	}
	
	/**
	 * Returns the SIFWorks ADK configuration file name for the given agent. If the property doesn't exist 
	 * then the agentID is returned as the file name.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.cfg.name=_name_of_config_file</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the SIFWorks ADK configuration file name shall
	 * be returned.
	 * 
	 * @return See description.
	 */
	public String getADKConfigFileName(String agentID)
	{
		return getPropertyAsString("agent." + agentID + ".cfg.name", agentID);
	}
	
	/**
	 * Returns the SIFWorks ADK configuration file name with the full path for the given agent. This is a
	 * convenience method that simply concatenates the getADKConfigDir() with the getADKConfigFileName().
	 * If any of the values (path of filename) is null then null is returned.<p>
	 * 
	 * @param agentID The agentID of the agent for which the SIFWorks ADK configuration file name shall
	 * be returned.
	 * 
	 * @return See description.
	 */
	public String getADKConfigPathAndFileName(String agentID)
	{
		return getPathAndFileName(getADKConfigDir(agentID), getADKConfigFileName(agentID));
	}

	/**
	 * Returns the directory for Log4J logging for the given agent. If the property doesn't exist then null 
	 * is returned.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.log.dir=_path_to_directory</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the logging directory shall be returned.
	 * 
	 * @return See description.
	 */
	public String getLogDir(String agentID)
	{
		return getPropertyAsString("agent." + agentID + ".log.dir", null);
	}
	
	/**
	 * Returns the logging file name for the given agent. If the property doesn't exist then the agentID 
	 * is returned as the file name.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.log.name=_name_of_log_file</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the log file name shall be returned.
	 * 
	 * @return See description.
	 */
	public String getLogFileName(String agentID)
	{
		return getPropertyAsString("agent." + agentID + ".log.name", agentID);
	}
	
	/**
	 * Returns the logging file name with the full path for the given agent. This is a convenience method 
	 * that simply concatenates the getLogDir() with the getLogFileName().
	 * If any of the values (path of filename) is null then null is returned.<p>
	 * 
	 * @param agentID The agentID of the agent for which the logging configuration file name shall
	 * be returned.
	 * 
	 * @return See description.
	 */
	public String getLogPathAndFileName(String agentID)
	{
		return getPathAndFileName(getLogDir(agentID), getLogFileName(agentID));
	}

	/**
	 * Returns the SIFWorks ADK working directory for the given agent. If the property doesn't exist then null 
	 * is returned.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.workdir=_path_to_directory</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the working directory shall be returned.
	 * 
	 * @return See description.
	 */
	public String getWorkDir(String agentID)
	{
		return getPropertyAsString("agent." + agentID + ".workdir", null);
	}
	
	/**
	 * Returns the debug level for the given agent. If no value is found then DBG_NONE is returned.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.debugLevel=_debug_level_</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the debug level shall be returned.
	 * 
	 * @return See description.
	 */
	public String getDebugLevel(String agentID)
	{
		return getPropertyAsString("agent." + agentID + ".debugLevel", "DBG_NONE");
	}

	/**
	 * Returns the package name of the subscribers. All subscriber classes listed in the property file
	 * will be assumed to be in this package. This value must be a fully qualified java package name. 
	 * If no value is found then null is returned.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.subscriber.basePackageName=_fully_quaified_package_name_</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the package name shall be returned.
	 * 
	 * @return See description.
	 */
	public String getSubscriberBasePackageName(String agentID)
	{
		return getPropertyAsString("agent." + agentID + ".subscriber.basePackageName", null);
	}

	/**
	 * Returns the package name of the publishers. All publisher classes listed in the property file
	 * will be assumed to be in this package. This value must be a fully qualified java package name. 
	 * If no value is found then null is returned.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.publisher.basePackageName=_fully_quaified_package_name_</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the package name shall be returned.
	 * 
	 * @return See description.
	 */
	public String getPublisherBasePackageName(String agentID)
	{
		return getPropertyAsString("agent." + agentID + ".publisher.basePackageName", null);
	}

	/**
	 * This method returns a list of publisher names for a given agent. If no publishers are listed
	 * then an empty list is returned. The names of the publishers must match a class name for an
	 * implemented publisher. The publisher class must exist and must extend the BasePublisher class.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.publishers=_comma_separated_list_</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the publisher list shall be returned.
	 * 
	 * @return See description.
	 */
	public List<String> getPublishers(String agentID)
	{
		return getFromCommaSeparated("agent." + agentID + ".publishers");
	}

	/**
	 * This method returns a list of subscriber names for a given agent. If no subscribers are listed
	 * then an empty list is returned. The names of the subscribers must match a class name for an
	 * implemented subscriber. The subscriber class must exist and must extend the BaseSubscriber class.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.subscribers=_comma_separated_list_</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the subscriber list shall be returned.
	 * 
	 * @return See description.
	 */
	public List<String> getSubscribers(String agentID)
	{
		return getFromCommaSeparated("agent." + agentID + ".subscribers");
	}

	/**
	 * This method returns the test mode indicator for the given agent. If that doesn't exist then
	 * the false is returned.<p>
	 * 
	 * Valid values in the property file for this property is 'true' or 'false'.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.testMode=true|false</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the test mode indicator shall be returned.
	 * 
	 * @return See description.
	 */
	public boolean getTestMode(String agentID)
	{
		return getPropertyAsBool("agent."+agentID+".testMode" ,false);
	}
	
	/**
	 * Returns the test directory for the given agent. If the property doesn't exist then null 
	 * is returned. The test directory is used if the agent runs in test mode and messages are only
	 * read/written to the file system.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.test.data.directory=_path_to_directory_</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the working directory shall be returned.
	 * 
	 * @return See description.
	 */
	public String getTestDir(String agentID)
	{
		return getPropertyAsString("agent." + agentID + ".test.data.directory", null);
	}
	
	/**
	 * Returns the name of the mapping section to be used for this agent. This name must match the 
	 * node <mappings id="Default"> in the SIFWorks ADK configuration file for the given agent. If the
	 * property is not given then 'Default' is assumed and returned.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.mapping.name=_name_</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the mapping name shall be returned.
	 * 
	 * @return See description.
	 */
	public String getMappingName(String agentID)
	{
		return getPropertyAsString("agent." + agentID + ".mapping.name", "Default");
	}
	
	/**
	 * Returns the value for the start up delay in seconds between publishers and subscribers.
	 * There might be a reason for starting them with some delay in between such as DB or any other
	 * resource access.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent.thread.startup.delay=_delay_in_seconds_</code><p>
	 * 
	 * @return See description.
	 */
	public int getStartupDelay(int defalutValue)
	{
		return getPropertyAsInt("agent.thread.startup.delay", defalutValue);
	}

	/**
	 * This method returns the number of threads that shall be initiated to consume messages retrieved by 
	 * the given subscriber. If no such number exists for the given subscriber then it will attempt to 
	 * return the number of threads of this agent. If that doesn't exist either then the default number
	 * of 1 is returned.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_._subscriber_.consumer.numThreads=_int_value_</code><p>
	 * or<p>
	 * <code>agent._agentID_.consumer.numThreads=_int_value_</code><p>
	 * in case of the global setting
	 * 
	 * @param agentID The agentID of the agent for which the number of threads shall be returned.
	 * @param subscriberID The subscriberID of the subscriber for which the number of threads shall be returned.
	 * 
	 * @return See description.
	 */
	public int getNumConsumerThreads(String agentID, String subscriberID)
	{
		Integer value= getPropertyAsInt("agent."+agentID+"."+subscriberID+".consumer.numThreads");
		return (value != null) ? value.intValue() : getNumConsumerThreads(agentID);
	}
	
	/**
	 * This method returns the number of threads that shall be initiated to consume messages retrieved by 
	 * the given agent. If that doesn't exist then the default value of 1 is returned.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.consumer.numThreads=_int_value_</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the frequency shall be returned.
	 * 
	 * @return See description.
	 */
	public int getNumConsumerThreads(String agentID)
	{
		return getPropertyAsInt("agent."+agentID+".consumer.numThreads" ,1);
	}
	
	/**
	 * This method returns the values of the SIFCommon Framework Property file as a property structure. This
	 * method is intended to be used if one adds additional properties to the file that are not the default
	 * properties for which a method is provided within this class.
	 * Generally one should use the other getter methods of this class to access the values of the SIFCommon
	 * Framework Property file.
	 * 
	 * @return the values of the SIFCommon Framework Property file as a property structure. Null if the file
	 * could not be read.
	 */
	public Properties getProperties()
	{
		return properties;
	}

    /*------------------------*/
    /*-- Experimental stuff --*/
    /*------------------------*/
	public int getEventBatchSize()
	{
		return getPropertyAsInt("agent.eventBatchSize", 1);
	}

	/**
	 * Returns the package name of the custom objects. All custom object classes listed in the property file
	 * will be assumed to be in this package. This value must be a fully qualified java package name. 
	 * If no value is found then null is returned.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.customObjects.basePackageName=_fully_quaified_package_name_</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the package name shall be returned.
	 * 
	 * @return See description.
	 */
	public String getCustonObjBasePackageName(String agentID)
	{
		return getPropertyAsString("agent." + agentID + ".customObjects.basePackageName", null);
	}

	/**
	 * This method returns a list of custom object names for a given agent. If no custom objects are listed
	 * then an empty list is returned. The names of the custom objects must match a class name for an
	 * implemented custom objects. The custom object class must exist and must implement the 
	 * CustomObject interface.<p>
	 * 
	 * The property that this method attempts to access must have the following structure:<p>
	 * 
	 * <code>agent._agentID_.customObjects=_comma_separated_list_</code><p>
	 * 
	 * @param agentID The agentID of the agent for which the publisher list shall be returned.
	 * 
	 * @return See description.
	 */
	public List<String> getCustomObjects(String agentID)
	{
		return getFromCommaSeparated("agent." + agentID + ".customObjects");
	}
	
    /*-------------------------*/
    /*-- Other handy methods --*/
    /*-------------------------*/

	/**
	 * Returns the given property as a Integer object. If it doesn't exist or is not an Integer then null is
	 * returned.
	 */
	public Integer getPropertyAsInt(String propertyName)
	{
		if (properties != null)
		{
			String stringVal = properties.getProperty(propertyName);
			if (stringVal != null)
			{
				try
				{
					return Integer.valueOf(stringVal);
				}
				catch (Exception ex)
				{}
			}
		}
		return null;
	}

	/**
	 * Returns the given property as a int. If it doesn't exist or is not an Integer then the default value
	 * is returned.
	 */
	public int getPropertyAsInt(String propertyName, int defaultValue)
	{
		Integer integer = getPropertyAsInt(propertyName);
		return (integer == null) ? defaultValue : integer.intValue();
	}

	
	/**
	 * Returns the given property as a boolean object. If it doesn't exist or is not a boolean then null is
	 * returned.
	 */
	public Boolean getPropertyAsBool(String propertyName)
	{
		if (properties != null)
		{
			String stringVal = properties.getProperty(propertyName);
			if (stringVal != null)
			{
				try
				{
					return Boolean.valueOf(stringVal);
				}
				catch (Exception ex)
				{}
			}
		}
		return null;
	}

	/**
	 * Returns the given property as a boolean. If it doesn't exist or is not a boolean then the default 
	 * value is returned.
	 */
	public boolean getPropertyAsBool(String propertyName, boolean defaultValue)
	{
		Boolean bool = getPropertyAsBool(propertyName);
		return (bool == null) ? defaultValue : bool.booleanValue();
	}
	
	
	/**
	 * Returns the given property as a String. If it doesn't exist or is empty then the default value is returned.
	 */
	public String getPropertyAsString(String propertyName, String defaultValue)
	{
		if (properties != null)
		{
			String value = properties.getProperty(propertyName);
			return (StringUtils.isEmpty(value) ? defaultValue : value);
		}
		return defaultValue;
	}
	
	public List<String> getFromCommaSeparated(String propertyName)
	{
		List<String> list = new ArrayList<String>();
        String classListStr = getPropertyAsString(propertyName, null);
        
        if (classListStr != null)
        {
        	String classNames[] = classListStr.split(",");
            for (int i = 0; i < classNames.length; i++)
            {
            	list.add(classNames[i].trim());
            }
        }
		
		return list;
	}
	
	/**
	 * Builds a full path and file name. If path is not given then the file name is returned.
	 */
	public String getPathAndFileName(String path, String name)
	{
		if (StringUtils.notEmpty(name))
		{
			return StringUtils.notEmpty(path) ? path+"/"+name : name;
		}
		
		return null;
	}

	
}
