package sopc2dts.parsers.sopcinfo;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;



public class SopcInfoElementIgnoreAll extends SopcInfoElement {
	String elementName;
	public SopcInfoElementIgnoreAll(ContentHandler p, XMLReader xr, String name) {
		super(p, xr);
		elementName = name;
	}
	@Override
	public String getElementName() {
		// TODO Auto-generated method stub
		return elementName;
	}
	
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
	}

}
