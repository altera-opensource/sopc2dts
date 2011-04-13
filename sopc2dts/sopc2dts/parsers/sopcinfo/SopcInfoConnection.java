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
package sopc2dts.parsers.sopcinfo;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.Connection;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.components.Interface;


public class SopcInfoConnection extends SopcInfoElementWithParams {
	String currTag;
	private String startModuleName;
	private Interface masterInterface;
	private String endModuleName;
	private Interface slaveInterface;
	private SopcInfoSystemLoader sys;
	private String kind;
	
	public SopcInfoConnection(ContentHandler p, XMLReader xr, String k, SopcInfoSystemLoader s) {
		super(p, xr, null);
		sys = s;
		kind = k;
	}
	
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		currTag = localName;
		super.startElement(uri, localName, qName, atts);
	}

	public void endElement(String uri, String localName, String qName)
		throws SAXException {
		// TODO Auto-generated method stub
		if(localName.equalsIgnoreCase(currTag))
		{
			currTag = null;
		} else {
			if(localName.equalsIgnoreCase(getElementName()) && (kind!=null))
			{
				Connection bc = null;
				if(kind.equalsIgnoreCase("avalon") || 
						kind.equalsIgnoreCase("avalon_tristate"))
				{
					bc = new Connection(masterInterface, slaveInterface, 
							SystemDataType.MEMORY_MAPPED);
					bc.setConnValue(Integer.decode(getParamValue("baseAddress")));
				} else if(kind.equalsIgnoreCase("clock"))
				{
					bc = new Connection(masterInterface, slaveInterface, 
							SystemDataType.CLOCK);
					bc.setConnValue(bc.getMasterInterface().getInterfaceValue());
				} else if(kind.equalsIgnoreCase("interrupt"))
				{
					bc = new Connection(masterInterface, slaveInterface, 
							SystemDataType.INTERRUPT);
					bc.setConnValue(Integer.decode(getParamValue("irqNumber")));
				} else if(kind.equalsIgnoreCase("avalon_streaming"))
				{
					bc = new Connection(masterInterface, slaveInterface, 
							SystemDataType.STREAMING);
				} else {
					Logger.logln("Unhandled connection of kind: " + kind,LogLevel.DEBUG);
				}
				//Ignore other connections for now...
				if(bc!=null)
				{
					sys.vConnections.add(bc);
				}
			}
			super.endElement(uri, localName, qName);
		}
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(currTag!=null)
		{
			if(currTag.equalsIgnoreCase("startModule"))
			{
				startModuleName = String.copyValueOf(ch, start, length);
			} else if(currTag.equalsIgnoreCase("startConnectionPoint"))
			{
				String startInterfaceName = String.copyValueOf(ch, start, length);
				masterInterface = sys.currSystem.getComponentByName(startModuleName).getInterfaceByName(startInterfaceName);
			} else if(currTag.equalsIgnoreCase("endModule"))
			{
				endModuleName = String.copyValueOf(ch, start, length);
			} else if(currTag.equalsIgnoreCase("endConnectionPoint"))
			{
				String endInterfaceName = String.copyValueOf(ch, start, length);
				slaveInterface = sys.currSystem.getComponentByName(endModuleName).getInterfaceByName(endInterfaceName);
			}
		}
	}

	@Override
	public String getElementName() {
		return "connection";
	}
}
