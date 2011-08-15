/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 Walter Goossens <waltergoossens@home.nl>

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
import java.io.Serializable;
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
import sopc2dts.lib.components.base.FlashPartition;

public class BoardInfo implements ContentHandler, Serializable {
	public enum PovType { CPU, PCI };
	private static final long serialVersionUID = -6963198643520147067L;
	FlashPartition part;
	private File sourceFile;
	String currTag;
	String flashChip;
	Vector<FlashPartition> vPartitions;
	Vector<String> vMemoryNodes;
	String bootArgs;
	private String pov = "";
	private PovType povType = PovType.CPU;
	
	HashMap<String, Vector<FlashPartition>> mFlashPartitions = 
			new HashMap<String, Vector<FlashPartition>>(4);

	HashMap<String, HashMap<Integer, String>> mI2CMaps = 
			new HashMap<String, HashMap<Integer,String>>(4);
	
	HashMap<Integer, String> mI2C;
	
	public BoardInfo()
	{
		vMemoryNodes = new Vector<String>();
	}
	public BoardInfo(File source) throws FileNotFoundException, SAXException, IOException
	{
		sourceFile = source;
		load(new InputSource(new BufferedReader(new FileReader(sourceFile))));
	}
	public BoardInfo(InputSource in) throws SAXException, IOException
	{
		load(in);
	}
	protected void load(InputSource in) throws SAXException, IOException
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
		} else if(localName.equalsIgnoreCase("FlashPartitions")) 
		{
			//attribute chip can be null for a wildcard/fallback map
			vPartitions = new Vector<FlashPartition>(); 
			mFlashPartitions.put(atts.getValue("chip"), vPartitions);
		} else if(localName.equalsIgnoreCase("I2CBus")) 
		{
			//attribute master can be null for a wildcard/fallback map
			mI2C = new HashMap<Integer, String>(4); 
			mI2CMaps.put(atts.getValue("master"), mI2C);
		} else if(localName.equalsIgnoreCase("I2CChip")) 
		{
			mI2C.put(Integer.decode(atts.getValue("addr")),atts.getValue("name"));
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

	public void characters(char[] ch, int start, int length)
			throws SAXException {
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
	public HashMap<Integer, String> getI2CChipsForMaster(String instanceName) {
		HashMap<Integer, String> res = mI2CMaps.get(instanceName);
		if(res==null)
		{
			// Try to get a backup map
			res = mI2CMaps.get(null);
		}
		return res;
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
	public void setPartitionsForchip(String instanceName, Vector<FlashPartition> vParts) {
		mFlashPartitions.put(instanceName, vParts);
	}
	public void setI2CBusForchip(String instanceName, HashMap<Integer, String> i2cMap) {
		mI2CMaps.put(instanceName, i2cMap);
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
		//I2c
		for(String chip : mI2CMaps.keySet())
		{
			HashMap<Integer, String> mI2CMap = mI2CMaps.get(chip);
			if((mI2CMap!=null)&&(mI2CMap.size()>0))
			{
				xml += "\t<I2CBus";
				if(chip!=null)
				{
					xml += " master=\"" + chip + "\"";
				}
				xml += ">\n";
				for(Integer addr : mI2CMap.keySet())
				{
					xml += "\t\t<I2CChip addr=\"0x" + Integer.toHexString(addr) + "\"" +
							" name=\"" + mI2CMap.get(addr) + "\"/>\n";
				}
				xml += "\t</I2CBus>\n";
			}			
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
}
