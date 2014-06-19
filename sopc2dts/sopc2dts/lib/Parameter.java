/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 - 2014 Walter Goossens <waltergoossens@home.nl>

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package sopc2dts.lib;

import sopc2dts.lib.devicetree.DTPropHexNumVal;
import sopc2dts.lib.devicetree.DTPropNumVal;
import sopc2dts.lib.devicetree.DTPropStringVal;
import sopc2dts.lib.devicetree.DTPropVal;
import sopc2dts.lib.devicetree.DTProperty;

public class Parameter {
	public enum DataType { NUMBER, UNSIGNED, BOOLEAN, STRING };
	String name;
	String value;
	DataType dataType = DataType.BOOLEAN;
	
	public Parameter(String name, String value, DataType t)
	{
		this.name = name;
		this.dataType = t;
		switch(t)
		{
		case UNSIGNED: {
			if(value.charAt(0) == '-')
			{
					this.value = String.format("0x%08x", Integer.decode(value));
			} else {
				this.value = value;
			}
		} break;
		default: {
			this.value = value;
		}
		}
	}
	public static DataType getDataTypeByName(String dtName)
	{
		if(dtName == null) 
		{
			return null;
		} else if(dtName.equalsIgnoreCase("BOOLEAN") ||
				dtName.equalsIgnoreCase("BOOL"))
		{
			return DataType.BOOLEAN;
		} else if(dtName.equalsIgnoreCase("NUMBER"))
		{
			return DataType.NUMBER;
		} else if(dtName.equalsIgnoreCase("UNSIGNED"))
		{
			return DataType.UNSIGNED;
		}
		return null;
	}
	public String getName()
	{
		return name;
	}
	public String getValue()
	{
		return value;
	}
	public DataType getType()
	{
		return dataType;
	}
	public boolean getValueAsBoolean() {
		if(value==null) return false;
		if(value.length()==0) return false;
		try {
			if(Integer.decode(value)==0)
			{
				return false;
			}
		} catch (NumberFormatException e) {
			if(value.equalsIgnoreCase("false"))
			{
				return false;
			}
		}
		//Treat all other strings and numbers as true
		 return true;
	 }
	public DTProperty toDTProperty()
	{
		return toDTProperty(name, dataType);
	}
	public DTProperty toDTProperty(String dtsName)
	{
		return toDTProperty(dtsName, dataType);
	}
	public DTProperty toDTProperty(String dtsName, DataType dt)
	{
		DTProperty prop = new DTProperty(dtsName, null,name + " type " + dataType);
		DTPropVal propVal = null;
		if(dt==null)
		{
			dt = dataType;
		}
		switch(dt)
		{
		case UNSIGNED: {
			propVal = new DTPropHexNumVal(Long.decode(value));
		} break;
		case NUMBER: {
			propVal = new DTPropNumVal(Long.decode(value));
		} break;
		case BOOLEAN: {
			if(!getValueAsBoolean())
			{
				prop = null;
			}
		} break;
		case STRING: {
			String tmpVal = value.trim();
			if (tmpVal.startsWith("\""))
			{
				tmpVal = tmpVal.substring(1);
			}
			if(tmpVal.endsWith("\""))
			{
				tmpVal = tmpVal.substring(0, tmpVal.length()-1);
			}
			propVal = new DTPropStringVal(tmpVal);
		} break;
		default:{
			prop = null;
		}
		}
		if((prop != null)&&(propVal!=null)) {
			prop.addValue(propVal);
		}
		return prop;
	}
}
