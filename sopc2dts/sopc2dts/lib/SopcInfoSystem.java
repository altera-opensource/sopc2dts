package sopc2dts.lib;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import sopc2dts.lib.SopcComponentLib;
import sopc2dts.lib.SopcInfoConnection;
import sopc2dts.lib.SopcInfoElementIgnoreAll;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.SopcInfoComponent;
import sopc2dts.lib.components.SopcInfoInterface;

public class SopcInfoSystem implements ContentHandler {
	public static final float MIN_SUPPORTED_VERSION	= 8.1f;
	public static final float MAX_SUPPORTED_VERSION	= 10.0f;
	private boolean ignoreInput = true;
	private String systemName;
	float version = MIN_SUPPORTED_VERSION;
	String versionStr = "";
	String uniqueID = "";
	String currTag = "";
	private Vector<SopcInfoComponent> vSystemComponents = new Vector<SopcInfoComponent>();
	Vector<SopcInfoConnection> vConnections = new Vector<SopcInfoConnection>();
	SopcComponentLib lib = new SopcComponentLib();
	SopcComponentDescription currComp = null;
	XMLReader xmlReader;
	
	public SopcInfoSystem(InputSource in)
	{
		try {
			xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setContentHandler(this);
			xmlReader.parse(in);
			connectComponents();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	void connectComponents()
	{
		SopcInfoConnection conn;
		while(vConnections.size()>0)
		{
			conn = vConnections.firstElement();
			SopcInfoInterface intf = conn.getStartInterface();
			if(intf!=null)
			{
				intf.getMasterConnections().add(conn);
			}
			intf = conn.getEndInterface();
			if(intf!=null)
			{
				intf.getvSlaveConnections().add(conn);
			}
			vConnections.remove(conn);
		}
	}
	public SopcInfoComponent getComponentByName(String name)
	{
		for(SopcInfoComponent c : getSystemComponents())
		{
			if(c.getInstanceName().equalsIgnoreCase(name))
			{
				return c;
			}
		}
		return null;
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if(ignoreInput)
		{
			if(localName.equalsIgnoreCase("EnsembleReport"))
			{
				setSystemName(atts.getValue("name"));
				version = Float.parseFloat(atts.getValue("version"));
				ignoreInput=false;
			}
		} else{
			if(localName.equalsIgnoreCase("module"))
			{
				SopcInfoComponent comp = lib.getComponentForClass(atts.getValue("kind"),atts.getValue("name"),this,xmlReader);
				if(comp==null)
				{
					System.out.println("Nothing known about modules of kind: " + atts.getValue("kind"));
				} else {
					getSystemComponents().add(comp);
				}
			} else if(localName.equalsIgnoreCase("connection")) {
				vConnections.add(new SopcInfoConnection(this,xmlReader, this));
			} else if(localName.equalsIgnoreCase("plugin") ||
					localName.equalsIgnoreCase("parameter")) {
				@SuppressWarnings("unused")
				SopcInfoElementIgnoreAll ignore = new SopcInfoElementIgnoreAll(this, xmlReader, localName);
			} else if(localName.equalsIgnoreCase("reportVersion") ||
					localName.equalsIgnoreCase("uniqueIdentifier")) {
				currTag = localName;
			} else {
				System.out.println("New element " + localName);
			}
		}
	}
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(!ignoreInput)
		{
			if(localName.equalsIgnoreCase("EnsembleReport"))
			{
				ignoreInput=true;
			} else if(localName.equals(currTag)) {
				currTag = null;
			} else {
				//System.out.println("End element " + localName);
			}
		}
	}	
	
	
	
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		if(currTag!=null)
		{
			if(currTag.equalsIgnoreCase("uniqueIdentifier"))
			{
				uniqueID = String.copyValueOf(ch, start, length);
			} else if(currTag.equalsIgnoreCase("reportVersion"))
			{
				versionStr = String.copyValueOf(ch, start, length);
			}

		}
	}
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		
	}
	public void endPrefixMapping(String arg0) throws SAXException {
		// TODO Auto-generated method stub
		
	}
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}
	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}
	public void setDocumentLocator(Locator arg0) {
		// TODO Auto-generated method stub
		
	}
	public void skippedEntity(String arg0) throws SAXException {
		// TODO Auto-generated method stub
		
	}
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		
	}
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}
	public void setvSystemComponents(Vector<SopcInfoComponent> vSystemComponents) {
		this.vSystemComponents = vSystemComponents;
	}
	public Vector<SopcInfoComponent> getSystemComponents() {
		return vSystemComponents;
	}
	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}
	public String getSystemName() {
		return systemName;
	}

}
