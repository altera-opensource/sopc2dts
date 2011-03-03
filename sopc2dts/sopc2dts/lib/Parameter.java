package sopc2dts.lib;

import sopc2dts.generators.AbstractSopcGenerator;

public class Parameter {
	public enum DataType { NUMBER, BOOLEAN, STRING };
	String name;
	String value;
	DataType dataType = DataType.BOOLEAN;
	
	public Parameter(String name, String value, DataType t)
	{
		this.name = name;
		this.value = value;
		dataType = t;
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
