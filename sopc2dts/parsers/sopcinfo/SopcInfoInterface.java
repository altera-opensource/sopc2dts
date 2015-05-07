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
package sopc2dts.parsers.sopcinfo;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.devicetree.DTHelper;


public class SopcInfoInterface extends SopcInfoElementWithParams {
	protected SystemDataType type = SystemDataType.CONDUIT;
	protected boolean isMaster;
	Interface bi;

	public SopcInfoInterface(ContentHandler p, XMLReader xr, String iName,
			BasicComponent owner, Attributes atts) {
		super(p, xr, null);
		setKind(atts.getValue("kind"));
		bi = new Interface(iName, type, isMaster, owner);
		basicElement = bi;
		owner.getInterfaces().add(bi);
	}
	boolean setKind(String k)
	{
		boolean valid = true;
		if(k.equalsIgnoreCase("avalon_master") || 
				k.equalsIgnoreCase("avalon_tristate_master") ||
				k.equalsIgnoreCase("tristate_conduit_master") ||
				k.equalsIgnoreCase("altera_axi_master")||
				k.equalsIgnoreCase("altera_axi4_master")||
				k.equalsIgnoreCase("apb_master") ||
				k.equalsIgnoreCase("axi4_master")||
				k.equalsIgnoreCase("axi4lite_master"))
		{
			type = SystemDataType.MEMORY_MAPPED;
			isMaster = true;
		} else if(k.equalsIgnoreCase("avalon_slave") ||
				k.equalsIgnoreCase("avalon_tristate_slave") ||
				k.equalsIgnoreCase("tristate_conduit_slave") ||
				k.equalsIgnoreCase("altera_axi_slave") ||
				k.equalsIgnoreCase("altera_axi4_slave") ||
				k.equalsIgnoreCase("altera_axi4lite_slave")||
				k.equalsIgnoreCase("axi4_slave")||
				k.equalsIgnoreCase("axi4lite_slave")||
				k.equalsIgnoreCase("apb_slave") ||
				k.equalsIgnoreCase("ahb_slave"))
		{
			type = SystemDataType.MEMORY_MAPPED;
			isMaster = false;
		} else if(k.equalsIgnoreCase("clock_sink"))
		{
			type = SystemDataType.CLOCK;
			isMaster = false;
		} else if(k.equalsIgnoreCase("clock_source"))
		{
			type = SystemDataType.CLOCK;
			isMaster = true;
		} else if(k.equalsIgnoreCase("interrupt_receiver"))
		{
			type = SystemDataType.INTERRUPT;
			isMaster = true;
		} else if(k.equalsIgnoreCase("interrupt_sender"))
		{
			type = SystemDataType.INTERRUPT;
			isMaster = false;
		} else if(k.equalsIgnoreCase("nios_custom_instruction_master"))
		{
			type = SystemDataType.CUSTOM_INSTRUCTION;
			isMaster = true;
		} else if(k.equalsIgnoreCase("nios_custom_instruction_slave"))
		{
			type = SystemDataType.CUSTOM_INSTRUCTION;
			isMaster = false;
		} else if(k.equalsIgnoreCase("avalon_streaming_sink"))
		{
			type = SystemDataType.STREAMING;
			isMaster = false;
		} else if(k.equalsIgnoreCase("avalon_streaming_source"))
		{
			type = SystemDataType.STREAMING;
			isMaster = true;
		} else if(k.equalsIgnoreCase("reset_sink"))
		{
			type = SystemDataType.RESET;
			isMaster = false;
		} else if(k.equalsIgnoreCase("reset_source"))
		{
			type = SystemDataType.RESET;
			isMaster = true;
		} else if(k.equalsIgnoreCase("conduit") ||
				k.equalsIgnoreCase("conduit_start") ||
				k.equalsIgnoreCase("conduit_end"))
		{
			type = SystemDataType.CONDUIT;
			isMaster = false;
		} else {
			valid = false;
			Logger.logln("Unsupported interface kind: " + k, LogLevel.WARNING);
		}
		return valid;
	}

	public long[] getAddressableSize()
	{
		long[] res;
		long span = 1;
		int stepSize = 1;
		String assVal = getParamValue("addressSpan");
		if(assVal!=null)
		{
			if((assVal.equalsIgnoreCase("18446744073709551616")) ||
				(assVal.equalsIgnoreCase("0x10000000000000000"))) {
				/* Make a special case for slave interfaces that span the
				 * entire 64bits spectrum.
				 */
				bi.setSecondaryWidth(3);
				return new long[]{ 1l, 0l, 0l };
			} else {
				span = Long.decode(assVal);
			}
		}
		assVal = getParamValue("addressAlignment");
		if((assVal!=null)&&(assVal.equals("NATIVE")))
		{
			assVal = getParamValue("addressUnits");
			if(assVal!=null)
			{
				if(assVal.equalsIgnoreCase("WORDS"))
				{
					stepSize = 4;
				} else {
					System.out.println("Unsupported AddressUnits: " + assVal + " reverting to bytes");
				}
			}			
		}
		switch(bi.getSecondaryWidth()) {
		case 1: {
			res = new long[1];
			res[0] = span*stepSize;
		} break;
		case 2: {
			res = new long[2];
			DTHelper.long2longArr(span * stepSize, res);
		} break;
		default: {
			Logger.logln("Unsupported cell-count: " + bi.getSecondaryWidth(), LogLevel.ERROR);
			res = new long[0];
		}
		}
		return res;
	}
	@Override
	public void endElement(String uri, String localName, String qName)
		throws SAXException {
		if(localName.equalsIgnoreCase(getElementName()))
		{
			if(bi.isMemorySlave())
			{
				bi.setInterfaceValue(getAddressableSize());
			} else if(bi.isClockMaster())
			{
				bi.setInterfaceValue(DTHelper.parseSize4Intf(getParamValue("clockRate"), bi));
			}
		}
		super.endElement(uri, localName, qName);
	}

	@Override
	public String getElementName() {
		return "interface";
	}
}
