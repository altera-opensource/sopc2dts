package sopc2dts.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import sopc2dts.Logger;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.altera.SICEpcs;
import sopc2dts.lib.components.altera.SICLan91c111;
import sopc2dts.lib.components.altera.SICSgdma;
import sopc2dts.lib.components.altera.SICTrippleSpeedEthernet;
import sopc2dts.lib.components.base.SICBridge;
import sopc2dts.lib.components.base.SICFlash;
import sopc2dts.lib.components.base.SICEthernet;
import sopc2dts.lib.components.base.SICUnknown;

public class SopcComponentLib implements ContentHandler {
	private SopcComponentDescription currScd;
	private boolean ignoreInput = true;
	private String currentVendor;
	SopcComponentDescription unknownComponent = new SICUnknown();
	Vector<SopcComponentDescription> vLibComponents = new Vector<SopcComponentDescription>();
	private static SopcComponentLib me = new SopcComponentLib();
	
	private SopcComponentLib()
	{
		loadComponentLibs();
	}
	public static SopcComponentLib getInstance()
	{
		return me;
	}
	int loadComponentLib(InputSource in)
	{
		int oldSize = vLibComponents.size();
		try {
			XMLReader xr = XMLReaderFactory.createXMLReader();
			xr.setContentHandler(this);
			xr.parse(in);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (vLibComponents.size() - oldSize);
	}
	public void loadComponentLibs()
	{
		try {
			JarFile jf = new JarFile(SopcComponentLib.class.getProtectionDomain().getCodeSource().getLocation().getPath());
			loadComponentLibsInJar(jf);
		} catch(Exception e) {
			Logger.logln("We seem to be not running from a jar file. Trying to load lib from filesystem");
			loadComponentLibsInWorkDir();
		}
	}

	public void loadComponentLibsInJar(JarFile jf)
	{
		Enumeration<JarEntry> je = jf.entries();
		while(je.hasMoreElements()) {
			String entryName = je.nextElement().getName();
			//System.out.println("JAR: " + devicename);
			if(entryName.endsWith(".xml") && entryName.startsWith("sopc_components_")) {
				try {
					Logger.logln("Loaded " + loadComponentLib(new InputSource(
							jf.getInputStream(jf.getEntry(entryName))))
							 + " components from " + entryName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void loadComponentLibsInWorkDir()
	{
		File fDir = new File(".");
		if(fDir.isDirectory())
		{
			String[] dirContents = fDir.list();
			for(int i=0; i<dirContents.length; i++)
			{
				if(dirContents[i].endsWith(".xml")&&
						dirContents[i].startsWith("sopc_components_"))
				{
					try {
						Logger.logln("Loaded " + loadComponentLib(new InputSource(
								new BufferedReader(new FileReader(dirContents[i]))))
								 + " components from " + dirContents[i]);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	protected SopcComponentDescription getScdByClassName(String className)
	{
		for(SopcComponentDescription scd : vLibComponents)
		{
			if(className.equalsIgnoreCase(scd.getClassName()))
			{
				return scd;
			}
		}
		return null;
	}
	public BasicComponent getComponentForClass(String className, String instanceName, String version)
	{
		if(className.equalsIgnoreCase("triple_speed_ethernet"))
		{
			return new SICTrippleSpeedEthernet(getScdByClassName(className), instanceName, version);			
		} else if (className.equalsIgnoreCase("altera_avalon_sgdma")) {
			return new SICSgdma(getScdByClassName(className), instanceName, version);
		} else if (className.equalsIgnoreCase("altera_avalon_epcs_flash_controller")) {
			return new SICEpcs(getScdByClassName("altera_avalon_spi"), instanceName, version);
		} else if (className.equalsIgnoreCase("altera_avalon_lan91c111")) {
			return new SICLan91c111(getScdByClassName(className), instanceName, version);
		} else {
			SopcComponentDescription scd = getScdByClassName(className);
			if(scd!=null)
			{
				if (scd.getGroup().equalsIgnoreCase("bridge")) {
					return new SICBridge(getScdByClassName(className), instanceName, version);
				} else if (scd.getGroup().equalsIgnoreCase("flash")) {
					return new SICFlash(scd, instanceName, version);
				} else if (scd.getGroup().equalsIgnoreCase("ethernet")) {
					return new SICEthernet(scd, instanceName, version);
				} else {
					return new BasicComponent(scd,instanceName, version);
				}
			} else {
				System.out.println("Unknown class: " + className);
				return new BasicComponent(unknownComponent, instanceName, version);
			}
		}
	}
	public BasicComponent finalCheckOnComponent(BasicComponent comp)
	{
		String className = comp.getScd().getClassName();
		if(!comp.getScd().isRequiredParamsOk(comp))
		{
			for(SopcComponentDescription scd : vLibComponents)
			{
				if(scd.getClassName().equalsIgnoreCase(className))
				{
					if(scd.isRequiredParamsOk(comp))
					{
						comp.setScd(scd);
						return comp;
					}
				}
			}	
			comp.setScd(unknownComponent);
		}
		return comp;
	}
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if(ignoreInput)
		{
			if(localName.equalsIgnoreCase("SOPC2DTS_COMPONENTLIB"))
			{
				ignoreInput=false;
				currentVendor=atts.getValue("vendor");
			}
		} else{
			if(localName.equalsIgnoreCase("S2DComponent"))
			{
				currScd = new SopcComponentDescription();
				currScd.setClassName(atts.getValue("classname"));
				currScd.setGroup(atts.getValue("group"));
				currScd.setVendor(currentVendor);
				currScd.setDevice(atts.getValue("compatDevice"));
				vLibComponents.add(currScd);
			} else if((localName.equalsIgnoreCase("compatible"))&&(currScd!=null))
			{
				currScd.addCompatible(atts.getValue("name"));
			} else if((localName.equalsIgnoreCase("parameter"))&&(currScd!=null))
			{
				String dtsName = atts.getValue("dtsName");
				if(dtsName==null)
				{
					dtsName = atts.getValue("dtsVName");
					if(dtsName!=null) dtsName = currentVendor + ',' + dtsName;
				}
				if(dtsName!=null) {
					currScd.addAutoParam(dtsName, atts.getValue("sopcName"), 
							atts.getValue("forceType"));
				}
			} else if((localName.equalsIgnoreCase("RequiredParameter"))&&(currScd!=null))
			{
				currScd.addRequiredParam(atts.getValue("name"), atts.getValue("value"));
			} else if((localName.equalsIgnoreCase("CompatibleVersion"))&&(currScd!=null))
			{
				currScd.addCompatibleVersion(atts.getValue("value"));
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
				currentVendor = null;
			} else if((!localName.equalsIgnoreCase("S2DComponent")) &&
					(!localName.equalsIgnoreCase("compatible")) &&
					(!localName.equalsIgnoreCase("parameter")) &&
					(!localName.equalsIgnoreCase("RequiredParameter")) &&
					(!localName.equalsIgnoreCase("CompatibleVersion"))) 
			{
				System.out.println("End element " + localName);
			}
		}
	}
	
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
	}
	public void endDocument() throws SAXException {
	}
	public void endPrefixMapping(String arg0) throws SAXException {
	}
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
	}
	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
	}
	public void setDocumentLocator(Locator arg0) {
	}
	public void skippedEntity(String arg0) throws SAXException {
	}
	public void startDocument() throws SAXException {
	}
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
	}
}
