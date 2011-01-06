package sopc2dts.lib;

import java.io.BufferedReader;
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

import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.SopcInfoComponent;
import sopc2dts.lib.components.altera.SICSgdma;
import sopc2dts.lib.components.altera.SICTrippleSpeedEthernet;
import sopc2dts.lib.components.base.SICBridge;
import sopc2dts.lib.components.base.SICFlash;
import sopc2dts.lib.components.base.SICUnknown;

public class SopcComponentLib implements ContentHandler {
	private SopcComponentDescription scd;
	private boolean ignoreInput = true;
	SopcComponentDescription unknownComponent = new SICUnknown();
	Vector<SopcComponentDescription> vLibComponents = new Vector<SopcComponentDescription>();
	boolean bVerbose = false;
	
	public SopcComponentLib()
	{
		this(false);
	}
	public SopcComponentLib(boolean verbose)
	{
		bVerbose = verbose;
		try {
			int oldSize = 0;
			XMLReader xr = XMLReaderFactory.createXMLReader();
			xr.setContentHandler(this);
			File folder = new File(".");
		    File[] listOfFiles = folder.listFiles();

		    for (File f : listOfFiles) {
		    	if (f.isFile()) {
		    		if(f.getName().startsWith("sopc_components_")&&f.getName().endsWith(".xml"))
		    		{
						xr.parse(new InputSource(new BufferedReader(new FileReader(f))));
						if(bVerbose) System.out.println("Loaded " + (vLibComponents.size() - oldSize) + " components from " + f);
						oldSize = vLibComponents.size();
		    		}
		    	}
			}
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public SopcInfoComponent getComponentForClass(String className, String instanceName, ContentHandler p, XMLReader xr)
	{
		for(SopcComponentDescription scd : vLibComponents)
		{
			if(className.equalsIgnoreCase(scd.getClassName()))
			{
				//Handle a few hardcoded special cases
				if (scd.getGroup().equalsIgnoreCase("bridge")) {
					return new SICBridge(p, xr, scd, instanceName);
					//Handle a few hardcoded special cases
				} else if (scd.getGroup().equalsIgnoreCase("flash")) {
					return new SICFlash(p, xr, scd, instanceName);
				} else if (scd.getClassName().equalsIgnoreCase("triple_speed_ethernet")) {
					return new SICTrippleSpeedEthernet(p, xr, scd, instanceName);
				} else if (scd.getClassName().equalsIgnoreCase("altera_avalon_sgdma")) {
					return new SICSgdma(p, xr, scd, instanceName);
				} else {
					return new SopcInfoComponent(p,xr,scd,instanceName);
				}
			}
		}
		System.out.println("Unknown class: " + className);
		return new SopcInfoComponent(p, xr, unknownComponent, instanceName);
	}
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if(ignoreInput)
		{
			if(localName.equalsIgnoreCase("SOPC2DTS_COMPONENTLIB"))
			{
				ignoreInput=false;
			}
		} else{
			if(localName.equalsIgnoreCase("S2DComponent"))
			{
				scd = new SopcComponentDescription();
				scd.setClassName(atts.getValue("classname"));
				scd.setGroup(atts.getValue("group"));
				scd.setVendor(atts.getValue("compatVendor"));
				scd.setDevice(atts.getValue("compatDevice"));
				if((scd.getVendor()!=null)&&(scd.getDevice()!=null))
				{
					scd.getCompatible().add(scd.getVendor() + "," + scd.getDevice());
				}
				vLibComponents.add(scd);
			} else if((localName.equalsIgnoreCase("compatible"))&&(scd!=null))
			{
				scd.getCompatible().add(atts.getValue("name"));
			} else if((localName.equalsIgnoreCase("parameter"))&&(scd!=null))
			{
				scd.addAutoParam(atts.getValue("dtsName"), atts.getValue("sopcName"));
			} else {
				System.out.println("New element " + localName);
			}
		}
	}
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(!ignoreInput)
		{
			if(localName.equalsIgnoreCase("SOPC2DTS_COMPONENTLIB"))
			{
				ignoreInput=true;
			} else if(localName.equalsIgnoreCase("S2DComponent")) {
				//mwa...
			} else if(localName.equalsIgnoreCase("compatible")) {
				//mwa...
			} else if(localName.equalsIgnoreCase("parameter")) {
				//mwa...
			} else {
				System.out.println("End element " + localName);
			}
		}
	}
	
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		// TODO Auto-generated method stub
		
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

}
