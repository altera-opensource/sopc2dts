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
package sopc2dts.parsers.ptf;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.Parameter;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.Parameter.DataType;
import sopc2dts.lib.components.Interface;

public class PHInterface extends PtfHandler {

	protected PHInterface(PtfHandler p, AvalonSystem sys, String elementName) {
		super(p, sys, elementName);
	}
	@Override
	protected PtfHandler handleEnd(String tag, String iName)
	{
		String type = mapAssignments.get("Bus_Type");
		boolean isMaster = (tag.equals("MASTER") ? true : false);
		Interface intf = null;
		if(iName!=null)
		{
			if(type.equalsIgnoreCase("avalon") || 
					type.equalsIgnoreCase("avalon_tristate"))
			{
				intf = new Interface(iName, 
						SystemDataType.MEMORY_MAPPED, isMaster, null);
				intf.setInterfaceValue(getAdressSpan());
			} else if(type.equalsIgnoreCase("nios_custom_instruction"))
			{
				intf = new Interface(iName, 
						SystemDataType.CUSTOM_INSTRUCTION, isMaster, null);
			} else if(type.equalsIgnoreCase("atlantic"))
			{
				intf = new Interface(iName, 
						SystemDataType.STREAMING, isMaster, null);
			} else {
				Logger.logln("Unhandled PTF interface type " + type,LogLevel.DEBUG);
			}
			if(intf!=null)
			{
				while(vConnections.size()>0)
				{
					intf.addParam(new Parameter("PTFConnection",vConnections.get(0), DataType.STRING));
					vConnections.remove(0);
				}
				parent.addChildObject(intf);
			}
		}
		return parent;
	}
	protected Long getAdressSpan()
	{
		//For some reason this assignment is mostly not present :S
		String as = mapAssignments.get("Address_Span");
		if(as!=null)
		{
			try {
				long span = Long.decode(as);
				return span;
			} catch(NumberFormatException e) {
				Logger.logln("Failed to parse " + as,LogLevel.ERROR);
			}
		}
		try {
			String aw = mapAssignments.get("Address_Width");
			if(aw!=null)
			{
				long iaw = Long.decode(aw);
				String dw = mapAssignments.get("Data_Width");
				if(dw!=null)
				{
					long idw = Long.decode(dw);
					return (1l<<iaw)*(idw/8l);
				}
			}
		} catch(Exception e)
		{
			//Exceptions are OK. just fail.
			Logger.logln("Failed to get adress spane for " + name, LogLevel.WARNING);
		}
		return 0l;
	}
}
