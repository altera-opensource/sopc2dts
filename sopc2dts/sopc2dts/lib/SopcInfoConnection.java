package sopc2dts.lib;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import sopc2dts.lib.components.SopcInfoComponent;
import sopc2dts.lib.components.SopcInfoInterface;


public class SopcInfoConnection extends SopcInfoElementWithParams {
	private String startModuleName;
	private String startInterfaceName;
	private String endModuleName;
	private String endInterfaceName;
	String currTag;
	SopcInfoSystem sys;

	public SopcInfoConnection(ContentHandler p, XMLReader xr, SopcInfoSystem s) {
		super(p, xr);
		sys = s;
	}
	
	public int getBaseAddress()
	{
		int base = -1;
		SopcInfoAssignment ass = getParamValue("baseAddress");
		if(ass!=null)
		{
			if(ass.getValue()!=null) 
			{
				base = Integer.decode(ass.getValue());
			}
		}
		return base;
	}
	@Override
	public String getElementName() {
		return "connection";
	}
	
	public SopcInfoComponent getEndModule() {
		return sys.getComponentByName(endModuleName);
	}
	public String getEndModuleName() {
		return endModuleName;
	}

	public SopcInfoInterface getEndInterface() {
		SopcInfoComponent comp = getEndModule();
		if(comp!=null)
		{
			return comp.getInterfaceByName(endInterfaceName);
		}
		return null;
	}
	public String getEndInterfaceName() {
		return endInterfaceName;
	}

	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		currTag = localName;
		super.startElement(uri, localName, qName, atts);
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
			if(currTag.equalsIgnoreCase("startModule"))
			{
				startModuleName = String.copyValueOf(ch, start, length);
			} else if(currTag.equalsIgnoreCase("startConnectionPoint"))
			{
				startInterfaceName = String.copyValueOf(ch, start, length);
			} else if(currTag.equalsIgnoreCase("endModule"))
			{
				endModuleName = String.copyValueOf(ch, start, length);
			} else if(currTag.equalsIgnoreCase("endConnectionPoint"))
			{
				endInterfaceName = String.copyValueOf(ch, start, length);
			}
		}
	}

	public SopcInfoComponent getStartModule() {
		return sys.getComponentByName(startModuleName);
	}
	public String getStartModuleName() {
		return startModuleName;
	}

	public SopcInfoInterface getStartInterface() {
		SopcInfoComponent comp = getStartModule();
		if(comp!=null)
		{
			return comp.getInterfaceByName(startInterfaceName);
		}
		return null;
	}
	public String getStartInterfaceName() {
		return startInterfaceName;
	}
}
