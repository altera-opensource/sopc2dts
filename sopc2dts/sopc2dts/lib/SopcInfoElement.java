package sopc2dts.lib;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


public abstract class SopcInfoElement implements ContentHandler {
	protected ContentHandler parentElement = null;
	protected XMLReader xmlReader = null;
	
	public SopcInfoElement(ContentHandler p, XMLReader xr)
	{
		parentElement = p;
		xmlReader = xr;
		xmlReader.setContentHandler(this);
	}
	public abstract String getElementName();
	
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		System.out.println("Unhandled SopcInfo element: " + localName);
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(localName.equalsIgnoreCase(getElementName()))
		{
			if((xmlReader!=null)&&(parentElement!=null))
			{
				xmlReader.setContentHandler(parentElement);
			}
		}
	}


	public void characters(char[] ch, int start, int length)
			throws SAXException {
	}

	public void endDocument() throws SAXException {
	}

	public void endPrefixMapping(String prefix) throws SAXException {
	}

	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
	}

	public void processingInstruction(String target, String data)
			throws SAXException {
	}

	public void setDocumentLocator(Locator locator) {
	}

	public void skippedEntity(String name) throws SAXException {
	}

	public void startDocument() throws SAXException {
	}

	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
	}

}
