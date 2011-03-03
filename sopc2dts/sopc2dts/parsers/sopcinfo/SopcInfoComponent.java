package sopc2dts.parsers.sopcinfo;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import sopc2dts.lib.BasicElement;
import sopc2dts.lib.components.BasicComponent;

public class SopcInfoComponent extends SopcInfoElementWithParams {

	public SopcInfoComponent(ContentHandler p, XMLReader xr, BasicElement be) {
		super(p, xr, be);
	}
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if(localName.equalsIgnoreCase("interface"))
		{
			@SuppressWarnings("unused")
			SopcInfoInterface intf = new SopcInfoInterface(this, xmlReader, 
					atts.getValue("name"), (BasicComponent)basicElement, atts);
		} else {
			super.startElement(uri, localName, qName, atts);
		}
	}
	
	@Override
	public String getElementName() {
		return "module";
	}

}
