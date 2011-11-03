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
package systemic.sif.sifcommon.mapping.adapter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import openadk.library.ADK;
import openadk.library.DefaultValueBuilder;
import openadk.library.SIFDataObject;
import openadk.library.SIFSimpleType;
import openadk.library.SIFTypeConverter;
import openadk.library.ValueBuilder;
import openadk.library.tools.mapping.ADKMappingException;
import openadk.library.tools.mapping.FieldAdaptor;
import openadk.library.tools.mapping.FieldMapping;
import openadk.library.tools.mapping.MappingsContext;

import org.apache.log4j.Logger;


/**
 * Basic implementation of the ADK FieldAdaptor interface for  java.sql.ResultSet. An initial implementation
 * was provided by Andrew Elmhorst. This implementation has been further enhanced by Joerg Huber.<p>
 * 
 * <b>Note:</b> This class is only expected to be used for Outbound message mappings (ie. publishers). The
 * behaviour is unknown (in fact it will not work) for Inbound messages (ie. subscribers).
 * 
 * @author Joerg Huber & Andrew Elmhorst
 *
 */
public class ResultSetAdapter implements FieldAdaptor
{
	private Logger logger = ADK.getLog();
	
	private ResultSet resultSet;
	private HashMap<String,Integer> columnNames;
	private ValueBuilder valueBuilder = null;
	
	@SuppressWarnings("rawtypes")
    private Class clazz = null;
	
	/**
	 * Initialises the ResultSet Adapter.
	 * 
	 * @param sourceData The resultset for this adapter.
	 * @param clazz The class that will be returned as part of the map() method.
	 * 
	 * @throws Exception If there is a problem with the resultset and therefore this class cannot be
	 *                   created.
	 */
    @SuppressWarnings("rawtypes")
    public ResultSetAdapter(ResultSet sourceData,  Class clazz) throws Exception
	{
    	this.clazz = clazz;
		this.resultSet = sourceData;
		ResultSetMetaData rsMetadata = sourceData.getMetaData();
		columnNames = new HashMap<String,Integer>();
		for( int i=0; i<rsMetadata.getColumnCount(); i++ )
		{
			columnNames.put(rsMetadata.getColumnName(i+1), i+1 );
		}
		valueBuilder = new DefaultValueBuilder(this);
	}

    /**
     * This methods will return an object of the type specified in the constructor of this class. The
     * properties of the class are populated according to the mappings provided in the MappingsContext. It
     * is expected that the MappingsContext is an Outbound context since this class is intended to be used
     * by publishers. 
     * 
     * @param mappingCtx The mapping context that shall be used to populate the returned sif object.
     * 
     * @return A SIF object of the type defined in the 'clazz' parameter in the constructor of this class.
     * 
     * @throws ADKMappingException Failure to map due to invalid mapping syntax, context or semantics.
     */
	public SIFDataObject map(MappingsContext mappingCtx) throws ADKMappingException
	{
		try
		{
			SIFDataObject sifObj = (SIFDataObject)clazz.newInstance();
			mappingCtx.setValueBuilder(valueBuilder);
			mappingCtx.map(sifObj, this);
			
			return sifObj;
		}
		catch (Exception ex)
		{
			logger.error("Failed create Object: "+clazz.getName()+":"+ex.getMessage(), ex);
			return null;			
		}
	}
	
	/**
	 * Moves the position in the resultset forward by one record. After this call the resultset points to
	 * the latest position. If there are more resultsets then this method will return true, otherwise false 
	 * is returned.<p>
	 * 
	 * <b>Note:</b> Do not call resultSet.next() outside of this class!!!!
	 * 
	 * @return TRUE if more rows are available in the result set otherwise FALSE is returned.
	 * 
	 * @throws SQLException 
	 */
	public boolean hasNext() throws SQLException
	{
		return resultSet.next();
	}
	
	/* (non-Javadoc)
	 * @see com.edustructures.sifworks.tools.mapping.FieldAdaptor#getSIFValue(java.lang.String, com.edustructures.sifworks.SIFTypeConverter, com.edustructures.sifworks.tools.mapping.FieldMapping)
	 */
	@SuppressWarnings("rawtypes")
    public SIFSimpleType getSIFValue(String name, SIFTypeConverter typeConverter, FieldMapping fm)
	{	
		return typeConverter.getSIFSimpleType( getValue(name) );
	}

	/* (non-Javadoc)
	 * @see com.edustructures.sifworks.tools.mapping.FieldAdaptor#getValue(java.lang.String)
	 */
	public Object getValue(String name) 
	{
		if (name == null)
		{
			return null;
		}
		
		try 
		{
			return resultSet.getObject(name);
		} 
		catch (SQLException ex) 
		{
			logger.error("Failed to get value '"+name+"' from the result set:" +ex.getMessage(), ex);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.edustructures.sifworks.tools.mapping.FieldAdaptor#hasField(java.lang.String)
	 */
	public boolean hasField(String fieldName) 
	{
		return columnNames.containsKey(fieldName);
	}

	/* (non-Javadoc)
	 * @see com.edustructures.sifworks.tools.mapping.FieldAdaptor#setSIFValue(java.lang.String, com.edustructures.sifworks.SIFSimpleType, com.edustructures.sifworks.tools.mapping.FieldMapping)
	 */
	@SuppressWarnings("rawtypes")
    public void setSIFValue(String fieldName, SIFSimpleType sifDataElement, FieldMapping fm) 
	{
		// Update of ResultSet not yet supported
	}

}
