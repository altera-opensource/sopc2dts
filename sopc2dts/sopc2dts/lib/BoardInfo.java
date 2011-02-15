package sopc2dts.lib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import sopc2dts.Logger;
import sopc2dts.lib.components.base.FlashPartition;

public class BoardInfo implements ContentHandler {
	FlashPartition part = null;
	String currTag = null;
	String flashChip = null;
	Vector<FlashPartition> vPartitions = null;
	Vector<String> vMemoryNodes = null;
	String bootArgs = null;
	private String pov = "";

	HashMap<String, Vector<FlashPartition>> mFlashPartitions = 
			new HashMap<String, Vector<FlashPartition>>(4);

	public BoardInfo()
	{
		//TODO Set some sane defaults
	}
	public BoardInfo(InputSource in) throws SAXException, IOException
	{
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.parse(in);
	}

	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if(localName.equalsIgnoreCase("BoardInfo"))
		{
			setPov(atts.getValue("pov"));
		} else if(localName.equalsIgnoreCase("Bootargs"))
		{
				bootArgs = atts.getValue("val");
		} else if(localName.equalsIgnoreCase("Chosen"))
		{
			//Hmmm will we do anything with this?
		} else if(localName.equalsIgnoreCase("FlashPartitions")) 
		{
			//attribute chip can be null for a wildcard/fallback map
			vPartitions = new Vector<FlashPartition>(); 
			mFlashPartitions.put(atts.getValue("chip"), vPartitions);
		} else if(localName.equalsIgnoreCase("Memory"))
		{
			vMemoryNodes = new Vector<String>();
		} else if(localName.equalsIgnoreCase("Node"))
		{
			if(vMemoryNodes!=null)
			{
				vMemoryNodes.add(atts.getValue("chip"));
			}
		} else if(localName.equalsIgnoreCase("Partition"))
		{
			if(vPartitions!=null)
			{
				part = new FlashPartition();
				part.setName(atts.getValue("name"));
				part.setAddress(Integer.decode(atts.getValue("address")));
				part.setSize(Integer.decode(atts.getValue("size")));
				vPartitions.add(part);
			}
		} else if(localName.equalsIgnoreCase("readonly"))
		{
			part.setReadonly(true);
		} else {
			currTag = localName;
			Logger.logln("Boardinfo: Unhandled element " + localName);
		}
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}
	public void endElement(String uri, String localName, String qName)
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
	public void setPov(String pov) {
		this.pov = pov;
	}
	public String getPov() {
		return pov;
	}
	public String getBootArgs() {
		return bootArgs;
	}
	public void setBootArgs(String bootArgs) {
		this.bootArgs = bootArgs;
	}
	public Vector<FlashPartition> getPartitionsForChip(String instanceName) {
		Vector<FlashPartition> res = mFlashPartitions.get(instanceName);
		if(res==null)
		{
			// Try to get a backup map
			res = mFlashPartitions.get(null);
		}
		return res;
	}
	public Vector<String> getMemoryNodes() {
		return vMemoryNodes;
	}
}
