package sopc2dts.parsers.sopcinfo;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import sopc2dts.lib.BasicElement;
import sopc2dts.lib.Parameter;
import sopc2dts.lib.Parameter.DataType;

public class SopcInfoAssignment extends SopcInfoElement {
	protected String name;
	protected String value;
	String currTag;
	DataType dataType = DataType.BOOLEAN;
	BasicElement basicElement;
	
	public SopcInfoAssignment(ContentHandler p, XMLReader xr, BasicElement be)
	{
		super(p,xr);
		basicElement = be;
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
			if(localName.equalsIgnoreCase(getElementName()) &&
					(basicElement!=null))
			{
				Parameter bp = new Parameter(name, value, dataType);
				basicElement.addParam(bp);
			}
			super.endElement(uri, localName, qName);
		}
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(currTag!=null)
		{
			if(currTag.equalsIgnoreCase("name"))
			{
				name = (String.copyValueOf(ch, start, length));
			} else if(currTag.equalsIgnoreCase("value"))
			{
				value = parseValue(String.copyValueOf(ch, start, length));
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
			int start = 0;
			boolean is_hex = false;
			dataType = DataType.NUMBER;
			String tmpVal = parseValueAsNumber(val);
			if (tmpVal.length() > 2 && tmpVal.substring(0, 2).equals("0x")) {
					start = 2;
					is_hex = true;
			}
			for(int i=start; i<tmpVal.length(); i++)
			{
				if (!is_hex)
				{
					if ((tmpVal.charAt(i)<'0')||(tmpVal.charAt(i)>'9'))
					{
						dataType = DataType.STRING;
					}
				} else {
					if ((tmpVal.charAt(i)<'0')||(tmpVal.charAt(i)>'9'))
					{
						if ((tmpVal.toLowerCase().charAt(i) < 'a') && (tmpVal.toLowerCase().charAt(i) > 'f'))
							dataType = DataType.STRING;
					}

				}
			}
			if(dataType == DataType.NUMBER)
			{
				val = tmpVal;
			}
		}
		return val;
	}
	public String getName() {
		return name;
	}
	public String getValue() {
		return value;
	}
}
