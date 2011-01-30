package sopc2dts.lib;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import sopc2dts.lib.components.SopcInfoComponent;
import sopc2dts.lib.components.SopcInfoInterface;


public class SopcInfoConnection extends SopcInfoElementWithParams {
	private String startModuleName;
	private String endModuleName;
	SopcInfoInterface masterInterface;
	SopcInfoInterface slaveInterface;	
	String currTag;
	SopcInfoSystem sys;
	
	public SopcInfoInterface getMasterInterface() {
		return masterInterface;
	}
	public SopcInfoComponent getMasterModule() {
		if(masterInterface!=null)
		{
			return masterInterface.getOwner();
		} else {
			return null;
		}
	}
	public void setMasterInterface(SopcInfoInterface masterInterface) {
		this.masterInterface = masterInterface;
	}

	public SopcInfoInterface getSlaveInterface() {
		return slaveInterface;
	}
	
	public SopcInfoComponent getSlaveModule() {
		if(slaveInterface!=null)
		{
			return slaveInterface.getOwner();
		} else {
			return null;
		}
	}

	public void setSlaveInterface(SopcInfoInterface slaveInterface) {
		this.slaveInterface = slaveInterface;
	}

	public SopcInfoConnection(SopcInfoConnection org)
	{
		super(org.parentElement,org.xmlReader);
		sys = org.sys;
		this.endModuleName = org.endModuleName;
		this.masterInterface = org.masterInterface;
		this.slaveInterface = org.slaveInterface;
		this.startModuleName = org.startModuleName;
		this.vParams = org.vParams;
	}

	public SopcInfoConnection(ContentHandler p, XMLReader xr, SopcInfoSystem s) {
		super(p, xr);
		sys = s;
	}
	
	public int getBaseAddress()
	{
		int base = -1;
		String assVal = getParamValue("baseAddress");
		if(assVal!=null)
		{
			base = Integer.decode(assVal);
		}
		return base;
	}
	@Override
	public String getElementName() {
		return "connection";
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
				String startInterfaceName = String.copyValueOf(ch, start, length);
				masterInterface = sys.getComponentByName(startModuleName).getInterfaceByName(startInterfaceName);
			} else if(currTag.equalsIgnoreCase("endModule"))
			{
				endModuleName = String.copyValueOf(ch, start, length);
			} else if(currTag.equalsIgnoreCase("endConnectionPoint"))
			{
				String endInterfaceName = String.copyValueOf(ch, start, length);
				slaveInterface = sys.getComponentByName(endModuleName).getInterfaceByName(endInterfaceName);
			}
		}
	}
}
