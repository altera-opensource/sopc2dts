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
import sopc2dts.lib.components.base.FlashPartition;

public class BoardInfo implements ContentHandler {
	FlashPartition part;
	String currTag;
	String flashChip;
	Vector<FlashPartition> vPartitions;
	Vector<String> vMemoryNodes;
	String bootArgs;
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
