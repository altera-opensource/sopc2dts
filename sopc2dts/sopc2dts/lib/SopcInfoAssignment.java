package sopc2dts.lib;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


public class SopcInfoAssignment extends SopcInfoElement {
	private String name;
	private String value;
	String currTag = null;
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
		int strip=0;
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
			String tmpVal = val.substring(0, val.length() - strip);
			boolean isAllNumber = true;
			for(int i=0; i<tmpVal.length(); i++)
			{
				if((tmpVal.charAt(i)<'0')&&(tmpVal.charAt(i)>'9'))
				{
					isAllNumber = false;
				}
			}
			if(isAllNumber)
			{
				val = tmpVal;
			}
		}
		return val;
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
