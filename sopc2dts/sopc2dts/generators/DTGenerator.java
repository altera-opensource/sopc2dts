/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2012 Walter Goossens <waltergoossens@home.nl>

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
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.BoardInfo.PovType;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.MemoryBlock;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTPropBool;
import sopc2dts.lib.devicetree.DTPropHexNumber;
import sopc2dts.lib.devicetree.DTPropNumber;
import sopc2dts.lib.devicetree.DTPropString;

public abstract class DTGenerator extends AbstractSopcGenerator {
	Vector<BasicComponent> vHandled;
	public DTGenerator(AvalonSystem s, boolean isText) {
		super(s, isText);
	}

	protected synchronized DTNode getDTOutput(BoardInfo bi)
	{
		vHandled = new Vector<BasicComponent>();
		DTNode rootNode = new DTNode("/");
		BasicComponent povComponent = getPovComponent(bi);
		DTNode sopcNode;
		DTNode chosenNode;
		if(povComponent!=null)
		{
			if(bi.getPovType().equals(PovType.CPU))
			{
				DTNode cpuNode = getCpuNodes(bi);
				DTNode memNode = getMemoryNode(bi, povComponent);
				sopcNode = new DTNode("sopc@0");
				chosenNode = getChosenNode(bi);
				DTPropString dtps = new DTPropString("model","ALTR," + sys.getSystemName());
				rootNode.addProperty(dtps);
				dtps = new DTPropString("compatible","ALTR," + sys.getSystemName());
				rootNode.addProperty(dtps);
				rootNode.addChild(cpuNode);
				rootNode.addChild(memNode);
				sopcNode.addProperty(new DTPropString("device_type", "soc"));
			} else {
				sopcNode = rootNode;
				chosenNode = null;
			}
			DTPropNumber dtpn = new DTPropNumber("#address-cells",1L);
			rootNode.addProperty(dtpn);
			dtpn = new DTPropNumber("#size-cells",1L);
			rootNode.addProperty(dtpn);

			sopcNode = getSlavesFor(bi, povComponent, sopcNode);
			sopcNode.addProperty(new DTPropBool("ranges"));
			sopcNode.addProperty(new DTPropNumber("#address-cells",1L));
			sopcNode.addProperty(new DTPropNumber("#size-cells",1L));
			Vector<String> vCompat = new Vector<String>();
			vCompat.add("ALTR,avalon");
			vCompat.add("simple-bus");
			sopcNode.addProperty(new DTPropString("compatible", vCompat));
			sopcNode.addProperty(new DTPropNumber("bus-frequency", povComponent.getClockRate()));
			if(bi.getPovType().equals(PovType.CPU))
			{
				rootNode.addChild(sopcNode);
				rootNode.addChild(chosenNode);
			}
		}
		return rootNode;
	}

	DTNode getChosenNode(BoardInfo bi)
	{
		DTNode chosenNode = new DTNode("chosen");
		if((bi.getBootArgs()==null)||(bi.getBootArgs().length()==0))
		{
			bi.setBootArgs("debug console=ttyAL0,115200");
		} else {
			bi.setBootArgs(bi.getBootArgs().replaceAll("\"", ""));
		}
		chosenNode.addProperty(new DTPropString("bootargs", bi.getBootArgs()));
		return chosenNode;
	}
	DTNode getCpuNodes(BoardInfo bi)
	{
		int numCPUs = 0;
		DTNode cpuNode = new DTNode("cpus");
		if(bi.getPovType() == PovType.CPU)
		{
			cpuNode.addProperty(new DTPropNumber("#address-cells",1L));
			cpuNode.addProperty(new DTPropNumber("#size-cells",0L));
			for(BasicComponent comp : sys.getSystemComponents())
			{
				if(comp.getScd().getGroup().equalsIgnoreCase("cpu"))
				{
					if(bi.getPov()==null) {
						bi.setPov(comp.getInstanceName());
					}
					comp.setAddr(numCPUs);
					cpuNode.addChild(comp.toDTNode(bi, null));
					vHandled.add(comp);
					numCPUs++;
				}
			}
		}
		if(cpuNode.getChildren().isEmpty())
		{
			cpuNode = null;
		}
		return cpuNode;
	}
	DTNode getMemoryNode(BoardInfo bi, BasicComponent master)
	{
		DTNode memNode = new DTNode("memory@0");
		DTPropHexNumber dtpReg = new DTPropHexNumber("reg");
		dtpReg.getValues().clear();
		memNode.addProperty(new DTPropString("device_type", "memory"));
		memNode.addProperty(dtpReg);
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
									dtpReg.addValue(mem.getBase());
									dtpReg.addValue(mem.getSize());
									vHandled.add(comp);
								}		
							}
						}
					}
				}
			}

			if(dtpReg.getValues().size()==0)
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
										dtpReg.addValue(mem.getBase());
										dtpReg.addValue(mem.getSize());
										vMemoryMapped.add(mem.getModuleName());
										vHandled.add(comp);
									}
								}		
							}
						}
					}
				}
			}
		}
		if(dtpReg.getValues().size()==0) {
			return null;
		} else {
			return memNode;
		}
	}
	
	DTNode getSlavesFor(BoardInfo bi, BasicComponent masterComp, DTNode masterNode)
	{
		if(masterComp!=null)
		{
			Vector<Connection> vSlaveConn = masterComp.getConnections(SystemDataType.MEMORY_MAPPED, true);
			for(Connection conn : vSlaveConn)
			{
				BasicComponent slave = conn.getSlaveModule();						
				if((slave!=null)&&(!vHandled.contains(slave)))
				{
					vHandled.add(slave);
					if(slave.getScd().getGroup().equals("bridge"))
					{
						DTNode bridgeNode = getSlavesFor(bi, slave, slave.toDTNode(bi, conn));
						//Don't add empty bridges...
						if(!bridgeNode.getChildren().isEmpty())
						{
							masterNode.addChild(bridgeNode);
						}
					} else {
						masterNode.addChild(slave.toDTNode(bi, conn));
					}
				}
			}
		}
		return masterNode;
	}
}
