package sopc2dts.lib.components.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.SopcInfoConnection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.SopcInfoComponent;

public class SICFlash extends SopcInfoComponent {
	Vector<FlashPartition> vPartitions = new Vector<FlashPartition>();
	
	public SICFlash(ContentHandler p, XMLReader xr,
			SopcComponentDescription scd, String iName) {
		super(p, xr, scd, iName);
	}
	public String toDtsExtrasFirst(int indentLevel, SopcInfoConnection conn, Boolean endComponent)
	{
		String res = "";
		XMLReader xmlReader;
		vPartitions.clear();
		try {
			xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setContentHandler(new FlashPartLoader());
			File f =  new File("flash_layout_" + getInstanceName() + ".xml");
			if(!f.exists()) {
				f = new File("flash_layout.xml");
			}
			if(f.exists())
			{
				xmlReader.parse(new InputSource(new FileReader(f)));
			}
		} catch (SAXException e) {
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		if(vPartitions.size()>0)
		{
			res += AbstractSopcGenerator.indent(indentLevel) + "#address-cells = <1>;\n" +
					AbstractSopcGenerator.indent(indentLevel) + "#size-cells = <1>;\n";
		}
		return res;
	}
	public String toDtsExtras(int indentLevel, SopcInfoConnection conn, Boolean endComponent)
	{
		int bankw = 2;
		try {
			bankw = Integer.decode(getParamValue("dataWidth").getValue())/8;
		}catch(Exception e) { } //ignore
		String res = AbstractSopcGenerator.indent(indentLevel) + "bank-width = <"+bankw+">;\n" +
					AbstractSopcGenerator.indent(indentLevel) + "device-width = <1>;\n";
		for(FlashPartition part : vPartitions)
		{
			res += AbstractSopcGenerator.indent(indentLevel++) + part.name + "@" + Integer.toHexString(part.address) + " {\n" +
					AbstractSopcGenerator.indent(indentLevel) + String.format("reg = < 0x%08X 0x%08X >;\n", part.address,part.size);
			if(part.readonly)
			{
				res += AbstractSopcGenerator.indent(indentLevel) + "read-only;\n";
			}
			res += AbstractSopcGenerator.indent(--indentLevel) + "};\n";
		}
		return res;	
	}
	
	protected class FlashPartition {
		String name;
		int address;
		int size;
		boolean readonly = false;
	}
	protected class FlashPartLoader implements ContentHandler {

		private String name;
		private String value;
		String currTag = null;
		FlashPartition part = new FlashPartition();
		
		public void startElement(String uri, String localName, String qName,
				Attributes atts) throws SAXException {
			if(localName.equalsIgnoreCase("Partition"))
			{
				part = new FlashPartition();
				part.name = atts.getValue("name");
				part.address = Integer.decode(atts.getValue("address"));
				part.size = Integer.decode(atts.getValue("size"));
			} else if(localName.equalsIgnoreCase("readonly"))
			{
				part.readonly = true;
			} else {
				currTag = localName;				
			}
		}
		
		public void endElement(String uri, String localName, String qName)
			throws SAXException {
			if(localName.equalsIgnoreCase("Partition")&&(part!=null))
			{
				vPartitions.add(part);
			} else if(localName.equalsIgnoreCase(currTag))
			{
				currTag = null;
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
					if(getValue().endsWith("u") && (getValue().length()>1))
					{
						String tmpVal = getValue().substring(0, getValue().length()-1); 
						if(tmpVal.equalsIgnoreCase(Integer.decode(tmpVal).toString()))
						{
							setValue(tmpVal); 
						}
					}
				}
			}
		}
		public void setValue(String value) {
			this.value = value;
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

		public void endDocument() throws SAXException {
			// TODO Auto-generated method stub
			
		}

		public void endPrefixMapping(String prefix) throws SAXException {
			// TODO Auto-generated method stub
			
		}

		public void ignorableWhitespace(char[] ch, int start, int length)
				throws SAXException {
			// TODO Auto-generated method stub
			
		}

		public void processingInstruction(String target, String data)
				throws SAXException {
			// TODO Auto-generated method stub
			
		}

		public void setDocumentLocator(Locator locator) {
			// TODO Auto-generated method stub
			
		}

		public void skippedEntity(String name) throws SAXException {
			// TODO Auto-generated method stub
			
		}

		public void startDocument() throws SAXException {
			// TODO Auto-generated method stub
			
		}

		public void startPrefixMapping(String prefix, String uri)
				throws SAXException {
			// TODO Auto-generated method stub
			
		}
		
	}
}
