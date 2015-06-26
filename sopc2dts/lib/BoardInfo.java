/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 - 2015 Walter Goossens <waltergoossens@home.nl>

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
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.Parameter;
import sopc2dts.lib.boardinfo.BICDTAppend;
import sopc2dts.lib.boardinfo.BICEthernet;
import sopc2dts.lib.boardinfo.BICI2C;
import sopc2dts.lib.boardinfo.BoardInfoComponent;
import sopc2dts.lib.boardinfo.I2CSlave;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.base.FlashPartition;

public class BoardInfo implements ContentHandler {
	public enum PovType { CPU, PCI };
	public enum SortType { NONE, ADDRESS, NAME, LABEL };
	public enum RangesStyle { NONE, FOR_BRIDGE, FOR_EACH_CHILD };
	public enum AltrStyle { AUTO, FORCE_UPPER, FORCE_LOWER };
	FlashPartition part;
	private File sourceFile;
	String currTag;
	String flashChip;
	boolean includeTime = true;
	boolean showClockTree = false;
	boolean showConduits = false;
	boolean showResets = false;
	boolean showStreaming = false;
	Vector<FlashPartition> vPartitions;
	Vector<String> vMemoryNodes;
	Vector<BoardInfoComponent> vBics = new Vector<BoardInfoComponent>();
	Vector<Parameter> vAliases = new Vector<Parameter>();
	Vector<Parameter> vAliasRefs = new Vector<Parameter>();
	Vector<String> vIrqMasterClassIgnore = new Vector<String>();
	Vector<String> vIrqMasterLabelIgnore = new Vector<String>();
	
	String bootArgs;
	BoardInfoComponent currBic;
	private AltrStyle altrStyle = AltrStyle.AUTO;
	private String pov = "";
	private PovType povType = PovType.CPU;
	private RangesStyle rangesStyle = RangesStyle.FOR_EACH_CHILD;
	private SortType sortType = SortType.NONE;
	private BasicComponent.parameter_action dumpParameters = BasicComponent.parameter_action.NONE;
	HashMap<String, Vector<FlashPartition>> mFlashPartitions = 
			new HashMap<String, Vector<FlashPartition>>(4);

	public BoardInfo()
	{
		vMemoryNodes = new Vector<String>();
	}
	public BoardInfo(File source) throws FileNotFoundException, SAXException, IOException
	{
		load(source);
	}
	public BoardInfo(InputSource in) throws SAXException, IOException
	{
		load(in);
	}
	public void load(File source) throws FileNotFoundException, SAXException, IOException
	{
		sourceFile = source;
		load(new InputSource(new BufferedReader(new FileReader(sourceFile))));
	}
	protected void load(InputSource in) throws SAXException, IOException
	{
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.parse(in);
	}
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if(currBic != null)
		{
			currBic.startElement(uri, localName, qName, atts);
		} else {
			currBic = BoardInfoComponent.getBicFor(localName, atts);
			if(currBic != null)
			{
				vBics.add(currBic);
			} else {

				if (localName.equalsIgnoreCase("alias")) {
					String name = atts.getValue("name");
					String value = atts.getValue("value");
					if(value!=null) {
						Logger.logln("alias "+name+ " " + value,LogLevel.INFO);
						vAliases.add(new Parameter(name, value, Parameter.DataType.STRING));
					} else if((value=atts.getValue("label")) != null) {
						Logger.logln("alias "+name+ " " + value,LogLevel.INFO);
						vAliasRefs.add(new Parameter(name, value, Parameter.DataType.STRING));						
					} else {
						Logger.logln("alias "+name+" is badly formatted in boardinfo file",LogLevel.WARNING);
					}
				} else if(localName.equalsIgnoreCase("BoardInfo"))
				{
					setPov(atts.getValue("pov"));
				} else if(localName.equalsIgnoreCase("Bootargs"))
				{
						bootArgs = atts.getValue("val");
				} else if(localName.equalsIgnoreCase("FlashPartitions")) 
				{
					//attribute chip can be null for a wildcard/fallback map
					vPartitions = new Vector<FlashPartition>(); 
					mFlashPartitions.put(atts.getValue("chip"), vPartitions);
				} else if(localName.equalsIgnoreCase("IRQMasterIgnore"))
				{
					String ignore = atts.getValue("className");
					if(ignore!=null) {
						vIrqMasterClassIgnore.add(ignore);
					}
					ignore = atts.getValue("label");
					if(ignore!=null) {
						vIrqMasterLabelIgnore.add(ignore);
					}
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
				} else if(!localName.equalsIgnoreCase("Chosen"))
				{
					currTag = localName;
					Logger.logln("Boardinfo: Unhandled element " + localName, LogLevel.WARNING);
				}				
			}
		} 
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(currBic!=null)
		{
			currBic.characters(ch, start, length);
		}
	}
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(currBic!=null)
		{
			currBic.endElement(uri, localName, qName);
			if(localName.equalsIgnoreCase(currBic.getXmlTagName()))
			{
				currBic = null;
			}
		}
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
		if(pov!=null)
		{
			this.pov = pov;
		}
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
	public BoardInfoComponent getBicForChip(String instanceName)
	{
		return getBicForChip(instanceName,null);
	}
	public BoardInfoComponent getBicForChip(String instanceName, String tagName) 
	{
		for(BoardInfoComponent bic : vBics)
		{
			if(((instanceName == null) && (bic.getInstanceName() == null)) ||
				((instanceName != null) && instanceName.equalsIgnoreCase(bic.getInstanceName())))
			{
				if((tagName == null) || (tagName.equalsIgnoreCase(bic.getXmlTagName()))) {
					return bic;					
				}
			}
		}
		return null;
	}
	public void setBic(BoardInfoComponent newBic)
	{
		BoardInfoComponent oldBic = getBicForChip(newBic.getInstanceName());
		if(oldBic==null)
		{
			vBics.add(newBic);
		} else if(!oldBic.equals(newBic))
		{
			vBics.remove(oldBic);
			vBics.add(newBic);
		}
	}
	public BICEthernet getEthernetForChip(String instanceName)
	{
		BoardInfoComponent bic = getBicForChip(instanceName, BICEthernet.TAG_NAME);
		if(bic!=null)
		{
			if(bic instanceof BICEthernet)
			{
				return (BICEthernet) bic;
			}
		}
		return new BICEthernet(instanceName);
	}
	public BICI2C getI2CForChip(String instanceName) {
		BoardInfoComponent bic = getBicForChip(instanceName);
		if(bic!=null)
		{
			if(bic instanceof BICI2C)
			{
				return (BICI2C) bic;
			}
		} else {
			//Get wildcard
			for(BoardInfoComponent b : vBics) {
				if((b.getInstanceName()==null)&&(b instanceof BICI2C)) {
					return (BICI2C)b;
				}
			}
		}
		return new BICI2C(instanceName);
	}
	public Vector<BICDTAppend> getDTAppends() {
		Vector<BICDTAppend> vRes = new Vector<BICDTAppend>();
		for(BoardInfoComponent bic : vBics) {
			if(bic instanceof BICDTAppend) {
				vRes.add((BICDTAppend)bic);
			}
		}
		return vRes;
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
	public void setMemoryNodes(Vector<String> vMem) {
		vMemoryNodes = vMem;
	}
	public PovType getPovType() {
		return povType;
	}
	public RangesStyle getRangesStyle() {
		return rangesStyle;
	}
	public SortType getSortType() {
		return sortType;
	}
	public void setEthernetForChip(BICEthernet be)
	{
		BoardInfoComponent old = getBicForChip(be.getInstanceName());
		if(old!=null)
		{
			vBics.remove(old);
		}
		vBics.add(be);
	}
	public void setPovType(PovType povType) {
		this.povType = povType;
	}
	public void setPovType(String povTypeName) {
		if(povTypeName.equalsIgnoreCase("cpu"))
		{
			setPovType(PovType.CPU);
		} else if(povTypeName.equalsIgnoreCase("pci") ||
				povTypeName.equalsIgnoreCase("pcie"))
		{
			setPovType(PovType.PCI);
		}
	}
	public void setRangesStyle(RangesStyle rangesStyle) {
		this.rangesStyle = rangesStyle;
	}
	public void setRangesStyle(String rangesStyleName) {
		if(rangesStyleName.equalsIgnoreCase("child")) {
			setRangesStyle(RangesStyle.FOR_EACH_CHILD);
		} else if (rangesStyleName.equalsIgnoreCase("bridge")) {
			setRangesStyle(RangesStyle.FOR_BRIDGE);
		} else if (rangesStyleName.equalsIgnoreCase("none")) {
			setRangesStyle(RangesStyle.NONE);
		} else {
			Logger.logln("Unsupported ranges-style '" + rangesStyleName + "'", LogLevel.WARNING);
		}
	}
	public void setSortType(SortType sortType) {
		this.sortType = sortType;
	}
	public void setSortType(String sortTypeName) {
		if(sortTypeName.equalsIgnoreCase("address")) {
			setSortType(SortType.ADDRESS);
		} else if(sortTypeName.equalsIgnoreCase("name")) {
			setSortType(SortType.NAME);
		} else if(sortTypeName.equalsIgnoreCase("label")) {
			setSortType(SortType.LABEL);
		} else {
			setSortType(SortType.NONE);
		}
	}
	public void setPartitionsForchip(String instanceName, Vector<FlashPartition> vParts) {
		mFlashPartitions.put(instanceName, vParts);
	}
	public void setI2CBusForchip(String instanceName, Vector<I2CSlave> vSlaves) {
		BoardInfoComponent bic = getBicForChip(instanceName, BICI2C.TAG_NAME);
		if(bic == null) {
			bic = new BICI2C(instanceName);
			vBics.add(bic);
		}
		BICI2C bi2c = (BICI2C)bic;
		bi2c.setSlaves(vSlaves);
	}
	public boolean isValidIRQMaster(BasicComponent comp) {
		for(String imi : vIrqMasterClassIgnore) {
			if(comp.getClassName().equalsIgnoreCase(imi))
			{
				return false;
			}
		}
		for(String imi : vIrqMasterLabelIgnore) {
			if(comp.getInstanceName().equalsIgnoreCase(imi))
			{
				return false;
			}
		}
		return true;
	}
	public String getXml()
	{
		String xml = "<BoardInfo";
		if((pov!=null)&&(pov.length()!=0))
		{
			xml += " pov=\"" + pov + "\"";
		}
		xml+=">\n";
		//Memory
		if((vMemoryNodes!=null)&&(vMemoryNodes.size()>0))
		{
			xml +="\t<Memory>\n";
			for(String node : vMemoryNodes)
			{
				if((node!=null)&&(node.length()>0))
				{
					xml += "\t\t<Node chip=\"" + node + "\"/>\n";
				}
			}
			xml +="\t</Memory>\n";
		}
		//Chosen
		if((bootArgs!=null)&&(bootArgs.length()>0))
		{
			xml += "\t<Chosen>\n" +
					"\t\t<Bootargs val=\"" + bootArgs + "\"/>\n" +
					"\t</Chosen>\n";
		}
		//Flash
		for(String chip : mFlashPartitions.keySet())
		{
			Vector<FlashPartition> vParts = mFlashPartitions.get(chip);
			if((vParts!=null)&&(vParts.size()>0))
			{
				xml += "\t<FlashPartitions";
				if(chip!=null)
				{
					xml += " chip=\"" + chip + "\"";
				}
				xml += ">\n";
				for(FlashPartition part : vParts)
				{
					xml += "\t\t<Partition name=\"" + part.getName() + "\"" +
							" address=\"0x" + Integer.toHexString(part.getAddress()) + "\"" +
							" size=\"0x" + Integer.toHexString(part.getSize()) + "\">\n";
					if(part.isReadonly())
					{
						xml +="\t\t\t<readonly/>\n";
					}
					xml += "\t\t</Partition>\n";
				}
				xml += "\t</FlashPartitions>\n";
			}			
		}
		//IrqMasterIgnore
		for(String imi : vIrqMasterClassIgnore)
		{
			xml += "\t<IRQMasterIgnore className=\"" + imi + "\"/>\n";
		}
		for(String imi : vIrqMasterLabelIgnore)
		{
			xml += "\t<IRQMasterIgnore label=\"" + imi + "\"/>\n";
		}
		//Aliases
		for(Parameter p : vAliases) {
			xml += "\t<alias name=\"" + p.getName() + "\" value=\"" + p.getValue() + "/>\n";
		}
		for(Parameter p : vAliasRefs) {
			xml += "\t<alias name=\"" + p.getName() + "\" label=\"" + p.getValue() + "/>\n";
		}
		//BICs
		for(BoardInfoComponent bic : vBics)
		{
			xml += bic.getXml();
		}
		xml+="</BoardInfo>\n";
		return xml;
	}
	public void setSourceFile(File sourceFile) {
		this.sourceFile = sourceFile;
	}
	public File getSourceFile() {
		return sourceFile;
	}
	public BasicComponent.parameter_action getDumpParameters() {
		return dumpParameters;
	}
	public void setDumpParameters(BasicComponent.parameter_action dumpParameters) {
		this.dumpParameters = dumpParameters;
	}
	public Vector<Parameter> getAliases() {
		return vAliases;
    }
	public Vector<Parameter> getAliasRefs() {
		return vAliasRefs;
    }
	public AltrStyle getAltrStyle() {
		return altrStyle;
	}
	public boolean isIncludeTime() {
		return includeTime;
	}
	public boolean isShowClockTree() {
		return showClockTree;
	}
	public boolean isShowConduits() {
		return showConduits;
	}
	public boolean isShowResets() {
		return showResets;
	}
	public boolean isShowStreaming() {
		return showStreaming;
	}
	public void showClockTree() {
		showClockTree = true;
	}
	public void showConduits() {
		this.showConduits = true;
	}
	public void showResets() {
		this.showResets = true;
	}
	public void showStreaming() {
		this.showStreaming = true;
	}
	public void setAltrStyle(AltrStyle altrStyle) {
		this.altrStyle = altrStyle;
	}
	public void setIncludeTime(boolean includeTime) {
		this.includeTime = includeTime;
	}
}
