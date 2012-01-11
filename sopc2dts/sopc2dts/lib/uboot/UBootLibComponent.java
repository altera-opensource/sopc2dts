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
package sopc2dts.lib.uboot;

import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;

public class UBootLibComponent {
	public static final long KERNEL_REGION_BASE_NOMMU	= 0x00000000l;
	public static final long IO_REGION_BASE_NOMMU 		= 0x80000000l;
	public static final long KERNEL_REGION_BASE_MMU	= 0xc0000000l;
	public static final long IO_REGION_BASE_MMU 		= 0xe0000000l;
	String[] compatible;
	HashMap<String, String> propertyDefines;
	String extraData;

	protected UBootLibComponent(String[] comp, HashMap<String, String> props, String extra)
	{
		compatible = comp;
		propertyDefines = props;
		extraData = extra;
	}
	protected static boolean isMMUEnabled(BasicComponent comp)
	{
		if(comp.getParamByName("embeddedsw.CMacro.MMU_PRESENT")!=null)
		{
			return true;
		}
		return false;
	}
	public static long getKernelBase(BasicComponent master)
	{
		if(isMMUEnabled(master))
		{
			return KERNEL_REGION_BASE_MMU;
		} else {
			return KERNEL_REGION_BASE_NOMMU;
		}
	}
	public static long getIOBase(BasicComponent master)
	{
		if(isMMUEnabled(master))
		{
			return IO_REGION_BASE_MMU;
		} else {
			return IO_REGION_BASE_NOMMU;
		}
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
	public String getHeadersFor(BasicComponent memMaster, BasicComponent irqMaster, BasicComponent comp, int ifNum, long addrOffset)
	{
		String res = "" ;
		if(comp.getScd().getGroup().equalsIgnoreCase("bridge"))
		{
			return res;
		} else 	if(propertyDefines == null)
		{
			res = getMemoryDefinesForConn(memMaster, comp, null, (addrOffset | getIOBase(irqMaster)));
			res += getInterruptDefinesForConn(irqMaster, comp, null);
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
					} else if(valType[1].startsWith("ioaddr"))
					{
						if(valType[1].length()==7)
						{
							res += getMemoryDefinesForConn(memMaster, comp, define, (addrOffset | getIOBase(irqMaster)), valType[1].charAt(6)-0x30);
						} else {
							res += getMemoryDefinesForConn(memMaster, comp, define, (addrOffset | getIOBase(irqMaster)));
						}
					} else if(valType[1].equalsIgnoreCase("kerneladdr"))
					{
						res += getMemoryDefinesForConn(memMaster, comp, define, addrOffset | getKernelBase(irqMaster));
					} else if(valType[1].equalsIgnoreCase("irq"))
					{
						res += getInterruptDefinesForConn(irqMaster, comp, define);
					} else if(valType[1].startsWith("size"))
					{
						if(valType[1].length()==5)
						{
							Vector<Interface> vIntf = comp.getInterfaces(SystemDataType.MEMORY_MAPPED, false);
							if(vIntf.size()>(valType[1].charAt(4)-0x30))
							{
								val = String.format("0x%08X", vIntf.get(valType[1].charAt(4)-0x30).getInterfaceValue());
							}
						} else {
							val = String.format("0x%08X", comp.getInterfaces().get(ifNum).getInterfaceValue());
						}
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
	protected String getInterruptDefinesForConn(BasicComponent master, BasicComponent slave, 
			String name)
	{
		return getDefinesForConn(master, slave, name, SystemDataType.INTERRUPT, 0,-1);
	}
	protected String getMemoryDefinesForConn(BasicComponent master, BasicComponent slave, 
			String name, long offset)
	{
		return getMemoryDefinesForConn(master, slave, name, offset,-1);
	}
	protected String getMemoryDefinesForConn(BasicComponent master, BasicComponent slave, 
			String name, long offset, int index)
	{
		return getDefinesForConn(master, slave, name, SystemDataType.MEMORY_MAPPED, offset,index);
	}
	protected String getDefinesForConn(BasicComponent master, BasicComponent slave, 
			String name, SystemDataType type, long offset, int index)
	{
		String res = "";
		Vector<Connection> vConns = slave.getConnections(type, false, master);
		if(name == null)
		{
			name = AbstractSopcGenerator.definenify(slave.getInstanceName());
			switch(type)
			{
			case MEMORY_MAPPED: name += "_BASE";	break;
			case INTERRUPT:		name += "_IRQ";		break;
			case CLOCK:			name += "_FREQ";	break;
			default: {
				
			}
			}
		}
		/* Remove instruction master stuff */
		int i=0;
		while(i<vConns.size())
		{
			if(vConns.get(i).getMasterInterface().getName().equalsIgnoreCase("instruction_master"))
			{
				vConns.remove(i);
			} else {
				i++;
			}
		}
		if((index>=0) && (index<vConns.size()))
		{
			vConns = new Vector<Connection>(vConns.subList(index, index+1));
		}
		for(Connection conn : vConns)
		{
			String val = (type == SystemDataType.MEMORY_MAPPED ? 
					String.format("0x%08X",(conn.getConnValue() + offset)) :
						"" + (conn.getConnValue() + offset));
			if(vConns.size() != 1)
			{
				name = AbstractSopcGenerator.definenify(slave.getInstanceName())
					+ "_" + AbstractSopcGenerator.definenify(conn.getSlaveInterface().getName());
			}
			res += String.format("#define %s\t%s\n", name,val);
		}
		return res;
	}
}
