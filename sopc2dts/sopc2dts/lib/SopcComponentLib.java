/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 - 2012 Walter Goossens <waltergoossens@home.nl>

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
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
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.altera.GenericTristateController;
import sopc2dts.lib.components.altera.MultiBridge;
import sopc2dts.lib.components.altera.SICEpcs;
import sopc2dts.lib.components.altera.SICLan91c111;
import sopc2dts.lib.components.altera.SICSgdma;
import sopc2dts.lib.components.altera.TSEMonolithic;
import sopc2dts.lib.components.altera.VIPFrameBuffer;
import sopc2dts.lib.components.altera.VIPMixer;
import sopc2dts.lib.components.arm.CortexA9GIC;
import sopc2dts.lib.components.base.CpuComponent;
import sopc2dts.lib.components.base.GpioController;
import sopc2dts.lib.components.base.SCDSelfDescribing;
import sopc2dts.lib.components.base.SICBridge;
import sopc2dts.lib.components.base.SICFlash;
import sopc2dts.lib.components.base.SICEthernet;
import sopc2dts.lib.components.base.SICI2CMaster;
import sopc2dts.lib.components.base.SICSpiMaster;
import sopc2dts.lib.components.base.SICUnknown;
import sopc2dts.lib.components.nxp.USBHostControllerISP1xxx;

public class SopcComponentLib implements ContentHandler {
	private SopcComponentDescription currScd;
	private boolean ignoreInput = true;
	private String currentVendor;
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
			Logger.logException(e);
		} catch (IOException e) {
			Logger.logException(e);
		}
		return (vLibComponents.size() - oldSize);
	}
	
	public void loadComponentLibs()
	{
		try {
			JarFile jf = new JarFile(SopcComponentLib.class.getProtectionDomain().getCodeSource().getLocation().getPath());
			loadComponentLibsInJar(jf);
		} catch(Exception e) {
			Logger.logln("We don't seem to be running from a jar file. " +
					"Trying to load lib from filesystem", LogLevel.DEBUG);
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
							 + " components from " + entryName, LogLevel.DEBUG);
				} catch (IOException e) {
					Logger.logException(e);
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
								 + " components from " + dirContents[i], LogLevel.DEBUG);
					} catch (FileNotFoundException e) {
						Logger.logException(e);
					}
				}
			}
		}
	}

	public SopcComponentDescription getScdByClassName(String className)
	{
		for(SopcComponentDescription scd : vLibComponents)
		{
			if(scd.isSupportingClassName(className))
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
			//return new TSEModular(getScdByClassName(className), instanceName, version);			
			return new TSEMonolithic(className, instanceName, version, getScdByClassName(className));			
		} else if (className.equalsIgnoreCase("altera_avalon_sgdma")) {
			return new SICSgdma(className, instanceName, version, getScdByClassName(className));
		} else if (className.equalsIgnoreCase("altera_avalon_epcs_flash_controller")) {
			return new SICEpcs(className, instanceName, version);
		} else if (className.equalsIgnoreCase("altera_avalon_lan91c111")) {
			return new SICLan91c111(className, instanceName, version, getScdByClassName(className));
		} else if (className.equalsIgnoreCase("altera_avalon_video_sync_generator")) {
			return new VIPFrameBuffer(className, instanceName, version, getScdByClassName(className));
		} else if ((className.equalsIgnoreCase("alt_vip_mix")) ||
				(className.equalsIgnoreCase("alt_vip_switch"))){
			return new VIPMixer(className, instanceName, version, getScdByClassName(className));
		} else if (className.equalsIgnoreCase("isp116x") || 
				className.equalsIgnoreCase("ISP1362_CTRL")) {
			return new USBHostControllerISP1xxx(className, instanceName, version, getScdByClassName(className));
		} else if (className.equalsIgnoreCase("altera_generic_tristate_controller")) {
			return new GenericTristateController(className, instanceName, version);
		} else if (className.equalsIgnoreCase("arm_gic")) {
			return new CortexA9GIC(instanceName, version);
		} else if (className.equalsIgnoreCase("hps_bridge_avalon")) {
			return new MultiBridge(className, instanceName, version, getScdByClassName(className));
		} else {
			return castToGroupSpecificObject(
					new BasicComponent(className, instanceName, version, 
							getScdByClassName(className)));
		}
	}
	private BasicComponent castToGroupSpecificObject(BasicComponent comp) {
		SopcComponentDescription scd = comp.getScd();
		if(scd!=null)
		{
			if (scd.getGroup().equalsIgnoreCase("bridge") &&
					!(comp instanceof SICBridge)) {
				return new SICBridge(comp);
			} else if (scd.getGroup().equalsIgnoreCase("cpu") &&
					!(comp instanceof CpuComponent)) {
				return new CpuComponent(comp);
			} else if (scd.getGroup().equalsIgnoreCase("flash") &&
					!(comp instanceof SICFlash)) {
				return new SICFlash(comp);
			} else if (scd.getGroup().equalsIgnoreCase("i2c") &&
					!(comp instanceof SICI2CMaster)) {
				return new SICI2CMaster(comp);
			} else if (scd.getGroup().equalsIgnoreCase("spi") &&
					!(comp instanceof SICSpiMaster)) {
				return new SICSpiMaster(comp);
			} else if (scd.getGroup().equalsIgnoreCase("ethernet") &&
					!(comp instanceof SICEthernet)) {
				return new SICEthernet(comp);
			} else if (scd.getGroup().equalsIgnoreCase("gpio") &&
					!(comp instanceof GpioController)) {
				return new GpioController(comp);
			}
		}
		return comp;
	}
	public BasicComponent finalCheckOnComponent(BasicComponent comp)
	{
		if(SCDSelfDescribing.isSelfDescribing(comp) && 
				!(comp.getScd() instanceof SCDSelfDescribing))
		{
			if(!(comp.getScd() instanceof SICUnknown))
			{
				Logger.logln("Component " + comp.getInstanceName() + " of class "
						+ comp.getClassName() + " is self-describing "
						+ "but has a lib-version as well. Not using self-described version",
						LogLevel.INFO);
			} else {
				comp.setScd(new SCDSelfDescribing(comp));
				comp = castToGroupSpecificObject(comp);
			}
		} else {
			if(comp.getScd() instanceof SICUnknown)
			{
				Logger.logln("Component " + comp.getInstanceName() + " of class "
						+ comp.getClassName() + " is unknown", LogLevel.WARNING);
			}
		}
		String className = comp.getClassName();
		if(comp.getScd()!=null)
		{
			if(!comp.getScd().isRequiredParamsOk(comp))
			{
				for(SopcComponentDescription scd : vLibComponents)
				{
					if(scd.isSupportingClassName(className))
					{
						if(scd.isRequiredParamsOk(comp))
						{
							comp.setScd(scd);
							return comp;
						}
					}
				}	
				comp.setScd(null);
			}
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
				currScd = new SopcComponentDescription(
						atts.getValue("classname"),atts.getValue("group"),
						currentVendor,atts.getValue("compatDevice"));
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
			} else if((localName.equalsIgnoreCase("TransparentInterfaceBridge"))&&(currScd!=null))
			{
				currScd.getTransparentBridges().add(currScd.new TransparentInterfaceBridge(
						atts.getValue("master"), atts.getValue("slave")));
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
					(!localName.equalsIgnoreCase("TransparentInterfaceBridge")) &&
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
