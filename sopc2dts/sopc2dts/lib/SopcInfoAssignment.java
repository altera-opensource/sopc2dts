package sopc2dts.lib;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import sopc2dts.generators.AbstractSopcGenerator;


public class SopcInfoAssignment extends SopcInfoElement {
	enum DataType { NUMBER, BOOLEAN, STRING };
	private String name;
	protected String value;
	String currTag = null;
	DataType dataType = DataType.BOOLEAN;
	
	public SopcInfoAssignment(ContentHandler p, XMLReader xr)
	{
		super(p,xr);
	}
	@Override
	public String getElementName() {
		return "assignment";
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
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		currTag = localName;
	}
	public void endElement(String uri, String localName, String qName)
		throws SAXException {
		// TODO Auto-generated method stub
		if(localName.equalsIgnoreCase(currTag))
		{
			currTag = null;
		} else {
			super.endElement(uri, localName, qName);
		}
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(currTag!=null)
		{
			if(currTag.equalsIgnoreCase("name"))
			{
				setName(String.copyValueOf(ch, start, length));
			} else if(currTag.equalsIgnoreCase("value"))
			{
				setValue(String.copyValueOf(ch, start, length));
			}
		}
	}
	protected String parseValueAsNumber(String val)
	{
		int strip = 0;
		if(val.endsWith("u")||val.endsWith("U"))
		{
			strip = 1;
		} else if(val.endsWith("UL"))
		{
			strip = 2;
		} else if(val.endsWith("ULL"))
		{
			strip = 3;
		}
		if((strip>0)&&(val.length()>strip))
		{
			val = val.substring(0, val.length() - strip);
		}
		return val;
	}
	/*
	 * Note: This stuff is decimal ONLY
	 */
	protected String parseValue(String val)
	{
		if((val==null)||(val.length()==0))
		{
			dataType = DataType.BOOLEAN;
		} else {
			dataType = DataType.NUMBER;
			String tmpVal = parseValueAsNumber(val);
			for(int i=0; i<tmpVal.length(); i++)
			{
				if((tmpVal.charAt(i)<'0')||(tmpVal.charAt(i)>'9'))
				{
					dataType = DataType.STRING;
				}
			}
			if(dataType == DataType.NUMBER)
			{
				val = tmpVal;
			}
		}
		return val;
	}
	public boolean isForDts(DataType dt)
	{
		if(dt == null) {
			dt = dataType; 
		}
		if(dt == DataType.BOOLEAN)
		{
			return getValueAsBoolean();
		} else {
			return true;
		}
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
			res += ";\t//" + name + " from sopcinfo type " + dataType + "\n";
		} else {
			res = "";
		}
		return res;
	}
	
	public void setValue(String value) {
		this.value = parseValue(value);
	}
	public String getValue() {
		return value;
	}
	public boolean getValueAsBoolean() {
		if(value==null) return false;
		if(value.length()==0) return false;
		try {
			if(Integer.decode(parseValueAsNumber(value))==0)
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
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
}
