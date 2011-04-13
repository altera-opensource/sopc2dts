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

import java.util.HashMap;
import java.util.Vector;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.Connection;
import sopc2dts.lib.SopcComponentLib;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;

class PtfHandler {
	HashMap<String, String> mapAssignments = new HashMap<String, String>();
	Vector<Object> vChildren = new Vector<Object>();
	Vector<String> vConnections = new Vector<String>();
	
	PtfHandler parent;
	AvalonSystem system;
	String name;
	int depth = 0;
	protected PtfHandler(PtfHandler p, AvalonSystem sys, String elementName)
	{
		parent = p;
		system = sys;
		name = elementName;
	}
	protected PtfHandler handle(String line)
	{
		String[] ass = line.split("=");
		if(ass.length==2)
		{
			mapAssignments.put(ass[0].trim(), ass[1].trim().substring(1, ass[1].trim().length()-2));
		} else if(line.startsWith("MASTER ")||line.startsWith("SLAVE "))
		{
			return new PHInterface(this, system, line);
		} else if(line.startsWith("MODULE")||
				line.startsWith("MASTER ")||line.startsWith("SLAVE ")||
				line.startsWith("IRQ_MASTER ")||
				line.startsWith("CLOCK ")||
				line.equals("CLOCKS") ||
				line.startsWith("MASTERED_BY ")) {
			return new PtfHandler(this, system, line);
		} else if((line.equals("WIZARD_SCRIPT_ARGUMENTS")&&(!name.startsWith("SYSTEM"))) ||
				line.equals("HDL_INFO") ||
				line.equals("PORT_WIRING") ||
				line.equals("BOARD_INFO") ||
				line.equals("MEMORY_MAP") ||
				line.equals("View") ||
				line.equals("SIMULATION"))
		{
			return new PHIgnore(this, system, line);
		} else if(!line.equals("}") && !line.equals("{")  &&
				!line.equals("SYSTEM_BUILDER_INFO")&&
				!line.equals("WIZARD_SCRIPT_ARGUMENTS")) {
			Logger.logln("unhandled PTF:" + line, LogLevel.DEBUG);
		}
		if(line.equalsIgnoreCase("{"))
		{
			depth++;
		} else if(line.equalsIgnoreCase("}"))
		{
			depth--;
		}
		if(depth<=0)
		{
			String[] splitTag = name.split(" ");
			if(splitTag.length>=2)
			{
				return handleEnd(splitTag[0], splitTag[1]);
			} else {
				return handleEnd(splitTag[0], null);
			}
		} else {
			return this;
		}
	}
	protected PtfHandler handleEnd(String tag, String tagName)
	{
		if(tag.equals("SYSTEM"))
		{
			system.setVersion(mapAssignments.get("System_Wizard_Version"));
		} else if(tag.equals("CLOCK"))
		{
			String sFreq = mapAssignments.get("frequency");
			if((tagName!=null)&&(sFreq!=null))
			{
				try {
					Interface intf = new Interface(tagName, SystemDataType.CLOCK, true, null);
					intf.setInterfaceValue(Long.decode(sFreq));
					parent.addChildObject(intf);
				} catch(NumberFormatException e) {
					Logger.logln("Failed to get frequency \"" + sFreq + "\"" +
							" for clock " + tagName, LogLevel.WARNING);
				}
			}
		} else if(tag.equals("CLOCKS"))
		{
			BasicComponent clock = new BasicComponent(new SopcComponentDescription("CLOCKS","clock","ALTR","clock"),"clocks","0");
			int i=0;
			while(i<vChildren.size())
			{
				if(vChildren.get(i) instanceof Interface)
				{
					clock.getInterfaces().add((Interface)vChildren.get(i));
					vChildren.remove(i);
				} else {
					i++;
				}
			}
			system.getSystemComponents().add(clock);			
		} else if(tag.equals("MODULE"))
		{
			BasicComponent comp = SopcComponentLib.getInstance().getComponentForClass(
					getClassName(), tagName, getClassVersion());
			while(vChildren.size()>0)
			{
				if(vChildren.get(0) instanceof Interface)
				{
					Interface intf = (Interface)(vChildren.get(0));
					intf.setOwner(comp);
					comp.getInterfaces().add(intf);
				} else {
					Logger.logln("Unhandled child object of type: " + 
							vChildren.get(0).getClass().getCanonicalName(),
							LogLevel.WARNING);
				}
				vChildren.remove(0);
			}
			String clkSrc = mapAssignments.get("Clock_Source");
			BasicComponent clkMaster = system.getComponentByName("CLOCKS");
			if((clkSrc!=null)&&(clkMaster!=null))
			{
				Interface clkMI = clkMaster.getInterfaceByName(clkSrc);
				if(clkMI!=null)
				{
					Interface clkSI = new Interface("PtfClk", SystemDataType.CLOCK, false, comp);
					comp.getInterfaces().add(clkSI);
					Connection conn = new Connection(clkMI, clkSI, SystemDataType.CLOCK);
					conn.setConnValue(clkMI.getInterfaceValue());
					clkSI.getConnections().add(conn);
					clkMI.getConnections().add(conn);
				}
			}
			system.getSystemComponents().add(comp);
		} else if(tag.equals("MASTERED_BY"))
		{
			String baseAddr = mapAssignments.get("Offset_Address");
			if(baseAddr==null)
			{
				parent.vConnections.add(name);
			} else {
				parent.vConnections.add(name + ' ' + baseAddr);					
			}
		} else if(tag.equals("IRQ_MASTER"))
		{
			String irq = mapAssignments.get("IRQ_Number");
			if(irq==null)
			{
				parent.vConnections.add(name);
			} else {
				parent.vConnections.add(name + ' ' + irq);					
			}
		}
		return parent;
	}
	protected String getClassName()
	{
		String cn = mapAssignments.get("class");
		if(cn!=null)
		{
			if(cn.equals("no_legacy_module"))
			{
				return mapAssignments.get("gtf_class_name");
			}
		}
		return cn;
	}
	protected String getClassVersion()
	{
		String version = mapAssignments.get("gtf_class_version");
		if(version==null)
		{
			return mapAssignments.get("class_version");
		}
		return version;
	}
	protected void addChildObject(Object o)
	{
		vChildren.add(o);
	}
}
