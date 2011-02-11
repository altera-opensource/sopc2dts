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
	/*
	 * Note: This stuff is decimal ONLY
	 */
	protected String parseValue(String val)
	{
		if((val==null)||(val.length()==0))
		{
			dataType = DataType.BOOLEAN;
		} else {
			int strip=0;
			String tmpVal = val;
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
				tmpVal = val.substring(0, val.length() - strip);
			}
			dataType = DataType.NUMBER;
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
	public boolean isForDts()
	{
		return true;
	}
	public String toDts(int indentLevel, String dtsName)
	{
		String res;
		if(isForDts())
		{
			res = AbstractSopcGenerator.indent(indentLevel) + dtsName;
			switch(dataType)
			{
			case NUMBER: {
				res += " = <" + value + ">";
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
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
}
