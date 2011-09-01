/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 Walter Goossens <waltergoossens@home.nl>

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

import sopc2dts.generators.AbstractSopcGenerator;

public class Parameter {
	public enum DataType { NUMBER, UNSIGNED, BOOLEAN, STRING };
	String name;
	String value;
	DataType dataType = DataType.BOOLEAN;
	
	public Parameter(String name, String value, DataType t)
	{
		this.name = name;
		this.value = value;
		this.dataType = t;
	}
	public boolean isForDts()
	{
		return isForDts(dataType);
	}
	public boolean isForDts(DataType dt)
	{
		if(dt==DataType.BOOLEAN)
		{
			return getValueAsBoolean();
		} else {
			return true;
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
	public String toDts(int indentLevel, String dtsName, DataType dt)
	{
		String res;
		if(dt==null)
		{
			dt = dataType;
		}
		if(isForDts(dt))
		{
			res = AbstractSopcGenerator.indent(indentLevel) + dtsName;
			switch(dt)
			{
			case UNSIGNED:
				if(value.charAt(0) == '-')
				{
					value = String.format("0x%08X", Integer.decode(value));
				}
			/* Fallthrough */
			case NUMBER: {
				res += " = <" + value + '>';
			} break;
			case BOOLEAN: { 
				//Nothing
			} break;
			case STRING: {
				String tmpVal = value.trim();
				if (!tmpVal.startsWith("\"") || !tmpVal.endsWith("\""))
				{
					tmpVal = "\"" + tmpVal + "\"";
				}
				res += " = " + tmpVal;
			} break;
			}
			res += ";\t//" + name + " type " + dataType + "\n";
		} else {
			res = "";
		}
		return res;
	}
}
