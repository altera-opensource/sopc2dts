package sopc2dts.parsers.qsys;

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

import sopc2dts.Logger;
import sopc2dts.lib.Connection;
import sopc2dts.lib.BasicElement;
import sopc2dts.lib.Parameter;
import sopc2dts.lib.Parameter.DataType;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.SopcComponentLib;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.base.SICUnknown;

public class QSysSystemLoader implements ContentHandler {
	private AvalonSystem currSystem;
	private QSysSubSystem currSubSystem;
	private BasicElement currElement;
	private BasicComponent currModule;
	private File sourceFile;
	private XMLReader xmlReader;
	private String hierName;
	private boolean isHierComponent;
	SopcComponentLib lib = SopcComponentLib.getInstance();
	public synchronized AvalonSystem loadSystem(File source)
	{
		loadQSys(source, null, false);
		return currSystem;
	}
	public synchronized QSysSubSystem loadSubSystem(File source, String name)
	{
		loadQSys(source, name, true);
		return currSubSystem;
	}
	public synchronized void loadQSys(File source, String name, boolean hier)
	{
		isHierComponent = hier;
		if(isHierComponent)
		{
			hierName = name;
		} else {
			hierName = parseValue("$${FILE_NAME}", source);
		}
		try {
			Logger.logln("Trying to load " + source.getCanonicalPath());
			InputSource in = new InputSource(new FileReader(source));
			sourceFile = source;
			xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setContentHandler(this);
			xmlReader.parse(in);
			if(currSystem!=null)
			{
				recheckComponents();
				flattenDesign();
			}
			Logger.logln("Loaded " + source.getCanonicalPath());
		} catch (SAXException e) {
			e.printStackTrace();
			currSystem = null;
			currSubSystem = null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			currSystem = null;
			currSubSystem = null;
		} catch (IOException e) {
			e.printStackTrace();
			currSystem = null;
			currSubSystem = null;
		}
	}
	public void flattenDesign()
	{
		int i=0;
		while(i<getComponents().size())
		{
			if(getComponents().get(i) instanceof QSysSubSystem)
			{
				QSysSubSystem qss = (QSysSubSystem)getComponents().get(i);
				Logger.logln("Trying to flatten " + qss.getInstanceName());
				//First re-route connections to internal components.
				while(qss.getInterfaces().size()>0)
				{
					Interface intf = qss.getInterfaces().firstElement();
					String internalName = intf.getParamValByName("internal");
					BasicComponent internalComponent = qss.getComponentByName(internalName.split("\\.")[0]);
					if(internalComponent.getInterfaceByName(internalName.split("\\.")[1])==null)
					{
						//Just move the interface inwards :)
						internalComponent.getInterfaces().add(intf);
						intf.setOwner(internalComponent);
					} else {
						Logger.logln("Interface merging on hierarchical QSys designs is not yet supported.");
					}
					qss.getInterfaces().remove(0);
				}
				//Transfers and rename subcomponents
				for(BasicComponent bc : qss.vSystemComponents)
				{
					bc.setInstanceName(qss.getInstanceName() + "-" + bc.getInstanceName());
					getComponents().add(bc);
				}
				qss.vSystemComponents.clear();
				//Remove QSysSubSystem
				getComponents().remove(qss);
			} else {
				i++;
			}
		}
	}
	public void recheckComponents()
	{
		for(BasicComponent comp : currSystem.getSystemComponents())
		{
			lib.finalCheckOnComponent(comp);
		}
	}
	public BasicComponent getComponentByName(String name)
	{
		if(isHierComponent)
		{
			return currSubSystem.getComponentByName(name);
		} else {
			return currSystem.getComponentByName(name);
		}
	}
	public Vector<BasicComponent> getComponents()
	{
		if(isHierComponent)
		{
			return currSubSystem.vSystemComponents;
		} else {
			return currSystem.getSystemComponents();
		}
	}
	public static DataType getDataTypeFromValue(String val)
	{
		return DataType.STRING;
	}
	public static SystemDataType getSystemDataTypeByName(String name)
	{
		if(name.equalsIgnoreCase("avalon"))	
			return SystemDataType.MEMORY_MAPPED;
		if(name.equalsIgnoreCase("avalon_streaming"))	
			return SystemDataType.STREAMING;
		if(name.equalsIgnoreCase("clock"))	
			return SystemDataType.CLOCK;		
		if(name.equalsIgnoreCase("reset"))	
			return SystemDataType.RESET;
		
		return SystemDataType.CONDUIT;
	}
	public void startElement(String uri, String localName, String qName, 
			Attributes atts) throws SAXException {
		if(localName.equalsIgnoreCase("system"))
		{
			if(isHierComponent)
			{
				currSubSystem = new QSysSubSystem(hierName,
						atts.getValue("version"));
				currElement = currModule = currSubSystem;
			} else {
				currSystem = new AvalonSystem(hierName,
						atts.getValue("version"), sourceFile);
				currElement = currSystem;
			}
		} else if(localName.equalsIgnoreCase("parameter")) {
			currElement.addParam(new Parameter(atts.getValue("name"), 
					atts.getValue("value"), getDataTypeFromValue(atts.getValue("value"))));
		} else if(localName.equalsIgnoreCase("interface")) {
			if (currModule != null) {
				Interface intf = new Interface(atts.getValue("name"),
						getSystemDataTypeByName(atts.getValue("type")),
						(atts.getValue("dir").equalsIgnoreCase("start")), currModule);
				currModule.getInterfaces().add(intf);
				String internal = atts.getValue("internal");
				if(internal != null)
				{
					intf.addParam(new Parameter("internal", internal, DataType.STRING));
				}
			}
		} else if(localName.equalsIgnoreCase("module")) {
			String kind = atts.getValue("kind");
			currModule = lib.getComponentForClass(kind, atts.getValue("name"), atts.getValue("version"));
			if(currModule.getScd() instanceof SICUnknown)
			{
				File f = new File(sourceFile.getParent() + File.separatorChar + kind + ".qsys");
				if(f.exists())
				{
					QSysSystemLoader qsl = new QSysSystemLoader();
					currModule = qsl.loadSubSystem(f,atts.getValue("name"));
				} else {
					try {
						Logger.logln("Nothing known about modules of kind: " + kind + " and " + f.getCanonicalPath() + " Does not exist");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if(isHierComponent)
			{
				currSubSystem.addModule(currModule);
			} else {
				currSystem.getSystemComponents().add(currModule);
			}
			currElement = currModule;
		} else if(localName.equalsIgnoreCase("connection")) {
			String start = atts.getValue("start");
			String end = atts.getValue("end");
			BasicComponent master = getComponentByName(start.split("\\.")[0]);
			BasicComponent slave = getComponentByName(end.split("\\.")[0]);
			if((master==null)||(slave==null))
			{
				Logger.logln("Cannot find master and/or slave to connect (" + start + " to " + end +")");
			} else {
				SystemDataType dt = getSystemDataTypeByName(atts.getValue("kind"));
				Interface mIntf = master.getInterfaceByName(start.split("\\.")[1]);
				Interface sIntf = slave.getInterfaceByName(end.split("\\.")[1]);
				if(mIntf==null)
				{
					mIntf = new Interface(start.split("\\.")[1], dt, true, master);
					master.getInterfaces().add(mIntf);
				}
				if(sIntf==null)
				{
					sIntf = new Interface(end.split("\\.")[1], dt, false, slave);
					slave.getInterfaces().add(sIntf);
				}
				Connection bc = new Connection(mIntf, sIntf, dt);
				currElement = bc;
				mIntf.getConnections().add(bc);
				sIntf.getConnections().add(bc);
				if(dt == SystemDataType.CLOCK)
				{
					if(mIntf.getInterfaceValue()==0)
					{
						String clockRate = master.getParamValByName("clockFrequency");
						if(clockRate!=null)
						{
							mIntf.setInterfaceValue(Integer.decode(clockRate));
						}
					}
					bc.setConnValue(mIntf.getInterfaceValue());
				}
			}
		} else if(!localName.equalsIgnoreCase("component")) {
			System.out.println("New element " + localName);
		}
	}
	public static String parseValue(String val, File f)
	{
		if(val.equalsIgnoreCase("$${FILENAME}"))
		{
			val = f.getName();
			if(val.endsWith(".qsys"))
			{
				val = val.substring(0, val.length()-5);
			}
			return val;
		} else {
			return val;
		}
	}
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ((localName.equalsIgnoreCase("connection")) &&
				(currElement instanceof Connection))
		{
			Connection bc = (Connection)currElement;
			if(bc.getType() == SystemDataType.MEMORY_MAPPED)
			{
				bc.setConnValue(Integer.decode(bc.getParamValByName("baseAddress")));
			}
		} else if(localName.equalsIgnoreCase("module"))
		{
			currModule = null;
		}
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
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
