package sopc2dts.lib.components;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import sopc2dts.lib.SopcInfoElement;


public class SopcInfoMemoryBlock extends SopcInfoElement {
	Boolean isBridge = false;
	String name;
	private String module;
	private int base = 0;
	private int size = 0;
	String currTag;
	
	public SopcInfoMemoryBlock(ContentHandler p, XMLReader xr) {
		super(p, xr);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getElementName() {
		return "memoryBlock";
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
				name = String.copyValueOf(ch, start, length);
			} else if(currTag.equalsIgnoreCase("moduleName"))
			{
				setModule(String.copyValueOf(ch, start, length));
			} else if(currTag.equalsIgnoreCase("isBridge"))
			{
				isBridge = Boolean.valueOf(String.copyValueOf(ch, start, length));
			} else if(currTag.equalsIgnoreCase("baseAddress"))
			{
				setBase(Integer.decode(String.copyValueOf(ch, start, length)));
			} else if(currTag.equalsIgnoreCase("span"))
			{
				setSize(Integer.decode(String.copyValueOf(ch, start, length)));
			}
		}
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getModule() {
		return module;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public void setBase(int base) {
		this.base = base;
	}

	public int getBase() {
		return base;
	}
}
