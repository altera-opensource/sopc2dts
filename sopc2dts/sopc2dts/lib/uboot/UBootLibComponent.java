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
package sopc2dts.lib.uboot;

import java.util.HashMap;
import java.util.Set;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;

public class UBootLibComponent {
	public static final long IO_REGION_BASE = 0x80000000l;
	String[] compatible;
	HashMap<String, String> propertyDefines;
	String extraData;

	protected UBootLibComponent(String[] comp, HashMap<String, String> props, String extra)
	{
		compatible = comp;
		propertyDefines = props;
		extraData = extra;
	}

	boolean isCompatible(String name) {
		for(String comp : compatible)
		{
			if(comp.equalsIgnoreCase(name))
			{
				return true;
			} else if(comp.startsWith("*") && comp.endsWith("*"))
			{
				if(name.contains(comp.subSequence(1, comp.length()-1)))
				{
					return true;
				}
			} else if(comp.startsWith("*"))
			{
				if(name.endsWith(comp.substring(1)))
				{
					return true;
				}
			} else if(comp.endsWith("*"))
			{
				if(name.startsWith(comp.substring(0, comp.length()-1)))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public HashMap<String, String> getPropertyDefines() {
		return propertyDefines;
	}
	
	public String getExtraData() {
		return extraData;
	}
	public String getHeadersFor(BasicComponent comp, int ifNum, long addrOffset)
	{
		String res = "" ;
		if(comp.getScd().getGroup().equalsIgnoreCase("bridge"))
		{
			return res;
		} else 	if(propertyDefines == null)
		{
			String irq = getIrqSimple(comp);
			res = String.format("#define CONFIG_SYS_%s_BASE\t0x%08X\n",
						AbstractSopcGenerator.definenify(comp.getInstanceName()),
						((comp.getAddrFromMaster() + addrOffset) | IO_REGION_BASE));
			if(irq!=null)
			{
				res += String.format("#define CONFIG_SYS_%s_IRQ\t%s\n",
						AbstractSopcGenerator.definenify(comp.getInstanceName()),irq);
			}
		} else {
			Set<String> keys = propertyDefines.keySet();
			for(String define : keys)
			{
				String[] valType = propertyDefines.get(define).split("\\|");
				String val = null;
				if(valType[0].equalsIgnoreCase("prop"))
				{
					val = comp.getParamValByName(valType[1]);
					if(val==null)
					{
						val = "not found: " + valType[1];
					}
				} else if(valType[0].equalsIgnoreCase("gen"))
				{
					if(valType[1].equalsIgnoreCase("clk"))
					{
						val = "" + comp.getClockRate();
					} else if(valType[1].equalsIgnoreCase("addr"))
					{
						val = String.format("0x%08X", 
								((comp.getAddrFromMaster() + addrOffset) | IO_REGION_BASE));
					} else if(valType[1].equalsIgnoreCase("addr_raw"))
					{
						val = String.format("0x%08X", (comp.getAddrFromMaster() + addrOffset));
					} else if(valType[1].equalsIgnoreCase("irq"))
					{
						val = getIrqSimple(comp);
					} else if(valType[1].equalsIgnoreCase("size"))
					{
						val = String.format("0x%08X", comp.getInterfaces().get(ifNum).getInterfaceValue());
					} else {
						val = "Don't know how to generate " + valType[1];
					}
				} else {
					val = "Unsupported type: " + valType[0];
				}
				if(val!=null)
				{
					res += String.format("#define %s\t%s\n", define,val);
				}
			}
		}
		if(extraData!=null)
		{
			res += extraData;
		}
		return res;
	}
	protected String getIrqSimple(BasicComponent comp)
	{
		BasicComponent irqParent = null;
		for(Interface intf : comp.getInterfaces())
		{
			if(intf.isIRQSlave())
			{
				if(intf.getConnections().size()>0)
				{
					irqParent = intf.getConnections().get(0).getMasterModule();
					if(intf.getConnections().get(0).getMasterModule().equals(irqParent))
					{
						return "" + intf.getConnections().get(0).getConnValue();
					}
				}
			}
		}
		return null;
	}
}
