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
package sopc2dts.generators;

import java.util.Vector;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.BoardInfo.PovType;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.MemoryBlock;

public class DTSGenerator extends AbstractSopcGenerator {
	Vector<BasicComponent> vHandled = new Vector<BasicComponent>();
	public DTSGenerator(AvalonSystem s) {
		super(s,true);
	}

	@Override
	public String getTextOutput(BoardInfo bi) {
		int indentLevel = 0;
		vHandled.clear();
		String res = "";
		BasicComponent povComp = getPovComponent(bi);
		if(povComp != null)
		{
			res = getSmallCopyRightNotice("devicetree")
				+ "/dts-v1/;\n"
				+ "/ {\n";
			switch(bi.getPovType())
			{
			case CPU: {
				res += indent(++indentLevel) + "model = \"ALTR," + sys.getSystemName() + "\";\n"
					+ indent(indentLevel) + "compatible = \"ALTR," + sys.getSystemName() + "\";\n"
					+ indent(indentLevel) + "#address-cells = <1>;\n"
					+ indent(indentLevel) + "#size-cells = <1>;\n"
					+ getDTSCpus(bi, indentLevel)
					+ getDTSMemoryFrom(bi, povComp, indentLevel)
					+ indent(indentLevel++) + "sopc@0 {\n"
					+ indent(indentLevel) + "ranges ;\n";
			} break;
			case PCI: {
				indentLevel++;
				res += "";
			} break;
			}
			res += indent(indentLevel) + "#address-cells = <1>;\n" 
				+ indent(indentLevel) + "#size-cells = <1>;\n"
				+ (bi.getPovType().equals(PovType.CPU) ? indent(indentLevel) + "device_type = \"soc\";\n" : "")
				+ indent(indentLevel) + "compatible = \"ALTR,avalon\",\"simple-bus\";\n"
				+ indent(indentLevel) + "bus-frequency = < " + povComp.getClockRate() + " >;\n"
				+ getDTSBusFrom(bi, povComp, indentLevel);
			switch(bi.getPovType())
			{
			case CPU: {
				res += indent(--indentLevel) + "}; //sopc\n"
					+ getDTSChosen(bi, indentLevel);
					
			} break;
			case PCI: {
			} break;
			}
			res += indent(--indentLevel) + "};\n";
		}
		return res;
	}
	
	String getDTSChosen(BoardInfo bi, int indentLevel)
	{
		String res = "";
		if((bi.getBootArgs()==null)||(bi.getBootArgs().length()==0))
		{
			bi.setBootArgs("debug console=ttyAL0,115200");
		} else {
			bi.setBootArgs(bi.getBootArgs().replaceAll("\"", ""));
		}
		res += indent(indentLevel++) + "chosen {\n" +
				indent(indentLevel) + "bootargs = \"" + bi.getBootArgs() + "\";\n" +
				indent(--indentLevel) + "};\n";
		return res;		
	}
	String getDTSCpus(BoardInfo bi, int indentLevel)
	{
		int numCPUs = 0;
		String res="";
		for(BasicComponent comp : sys.getSystemComponents())
		{
			if(comp.getScd().getGroup().equalsIgnoreCase("cpu"))
			{
				if(numCPUs==0)
				{
					res += indent(indentLevel++) + "cpus {\n"
						+ indent(indentLevel) + "#address-cells = <1>;\n"
						+ indent(indentLevel) + "#size-cells = <0>;\n";
					if(bi.getPov()==null) {
						bi.setPov(comp.getInstanceName());
					}
				}
				comp.setAddr(numCPUs);
				res += comp.toDts(bi, indentLevel);
				vHandled.add(comp);
				numCPUs++;
			}
		}
		if(numCPUs>0) {
			res += indent(--indentLevel) + "};\n";
		}
		return res;
	}
	String getDTSMemoryFrom(BoardInfo bi, BasicComponent master, int indentLevel)
	{
		String res = "";
		
		if(master!=null)
		{
			Vector<String> vMemoryMapped = bi.getMemoryNodes();
			if(vMemoryMapped!=null)
			{
				for(Interface intf : master.getInterfaces())
				{
					if(intf.isMemoryMaster())
					{
						for(MemoryBlock mem : intf.getMemoryMap())
						{
							if(vMemoryMapped.contains(mem.getModule().getInstanceName()))
							{
								BasicComponent comp = mem.getModule();
								if((comp!=null)&&(!vHandled.contains(comp)))
								{
									if(res.length()==0)
									{
										res = indent(indentLevel++) + "memory@0 {\n" +
												indent(indentLevel) + "device_type = \"memory\";\n" +
												indent(indentLevel) + "reg = <" + 
													String.format("0x%08X 0x%08X", mem.getBase(), mem.getSize());
									} else {
										res += "\n" + indent(indentLevel) + 
											String.format("\t0x%08X 0x%08X", mem.getBase(), mem.getSize());
									}
									vHandled.add(comp);
								}		
							}
						}
					}
				}
			}

			if(res.length()==0)
			{
				Logger.logln("dts memory section: No memory nodes specified. " +
						"Blindly adding them all", LogLevel.INFO);
				/*
				 * manual memory-map failed or is not present.
				 * Just list all devices classified as "memory"
				 */
				vMemoryMapped = new Vector<String>();
				for(Interface intf : master.getInterfaces())
				{
					if(intf.isMemoryMaster())
					{
						for(MemoryBlock mem : intf.getMemoryMap())
						{
							if(!vMemoryMapped.contains(mem.getModuleName()))
							{
								BasicComponent comp = mem.getModule();
								if(comp!=null)
								{
									if(comp.getScd().getGroup().equalsIgnoreCase("memory"))
									{
										if(res.length()==0)
										{
											res = indent(indentLevel++) + "memory@0 {\n" +
													indent(indentLevel) + "device_type = \"memory\";\n" +
													indent(indentLevel) + "reg = <" + 
														String.format("0x%08X 0x%08X", mem.getBase(), mem.getSize());
										} else {
											res += "\n" + indent(indentLevel) + 
												String.format("\t0x%08X 0x%08X", mem.getBase(), mem.getSize());
										}
										vMemoryMapped.add(mem.getModuleName());
										vHandled.add(comp);
									}
								}		
							}
						}
					}
				}
			}
			if(res.length()>0) {
				res += ">;\n" + indent(--indentLevel) + "};\n";
			}
		}
		return res;
	}

	String getDTSBusFrom(BoardInfo bi, BasicComponent master, int indentLevel)
	{
		String res = "";
		if(master!=null)
		{
			for(Interface intf : master.getInterfaces())
			{
//				res += indent(indentLevel) + "//Port " + intf.getName() + " of " + master.getInstanceName() + " type: " + intf.getType() + " isMaster: " + intf.isMaster() + "\n";
				if(intf.isMemoryMaster())
				{
//					res += indent(indentLevel) + "//Port " + intf.getName() + " of " + master.getInstanceName() + "\n";
					for(Connection conn : intf.getConnections())
					{
						BasicComponent slave = conn.getSlaveModule();						
						if(slave!=null)
						{
							if(!vHandled.contains(slave))
							{
								if(slave.getScd().getGroup().equalsIgnoreCase("bridge"))
								{
									vHandled.add(slave);
									String bridgeContents = getDTSBusFrom(bi, slave, ++indentLevel);
									indentLevel--;
									if(bridgeContents.length()>0)
									{
										res += slave.toDts(bi, indentLevel, conn, false);
										res += "\n" + bridgeContents;
										res += indent(indentLevel) + "}; //end "+slave.getScd().getGroup()+" (" + slave.getInstanceName() + ")\n\n";
									}
								} else {
									res += slave.toDts(bi, indentLevel, conn, false);
									vHandled.add(slave);
									res += indent(indentLevel) + "}; //end "+slave.getScd().getGroup()+" (" + slave.getInstanceName() + ")\n\n";
								}
							}
						}
					}
				}
			}
		}
		return res;
	}
}
