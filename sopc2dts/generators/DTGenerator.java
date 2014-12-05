/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2012 - 2013 Walter Goossens <waltergoossens@home.nl>

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

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.BoardInfo.SortType;
import sopc2dts.lib.Connection;
import sopc2dts.lib.Parameter;
import sopc2dts.lib.BoardInfo.PovType;
import sopc2dts.lib.boardinfo.BICDTAppend;
import sopc2dts.lib.boardinfo.BICDTAppend.DTAppendAction;
import sopc2dts.lib.boardinfo.BICDTAppend.DTAppendType;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.MemoryBlock;
import sopc2dts.lib.components.base.ClockSource;
import sopc2dts.lib.components.base.CpuComponent;
import sopc2dts.lib.devicetree.DTHelper;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTPropByteVal;
import sopc2dts.lib.devicetree.DTPropHexNumVal;
import sopc2dts.lib.devicetree.DTPropNumVal;
import sopc2dts.lib.devicetree.DTPropPHandleVal;
import sopc2dts.lib.devicetree.DTPropStringVal;
import sopc2dts.lib.devicetree.DTProperty;

public abstract class DTGenerator extends AbstractSopcGenerator {
	static final private String boardInfoComment = "appended from boardinfo";
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
		DTNode clocksNode = null;
		if(povComponent!=null)
		{
			int addrCells = povComponent.getInterfaces(SystemDataType.MEMORY_MAPPED, true).firstElement().getPrimaryWidth();
			int sizeCells = povComponent.getInterfaces(SystemDataType.MEMORY_MAPPED, true).firstElement().getSecondaryWidth();
			if(bi.getPovType().equals(PovType.CPU))
			{
				DTNode cpuNode = getCpuNodes(bi, povComponent);
				DTNode memNode = getMemoryNode(bi, povComponent, addrCells, sizeCells);
				DTNode aliasNode = null;
				sopcNode = new DTNode("sopc@0", "sopc0");
				chosenNode = getChosenNode(bi);
				clocksNode = getClocksNode(bi);
				rootNode.addProperty(new DTProperty("model","ALTR," + sys.getSystemName()));
				rootNode.addProperty(new DTProperty("compatible","ALTR," + sys.getSystemName()));
				rootNode.addProperty(new DTProperty("#address-cells", (long)addrCells));
				rootNode.addProperty(new DTProperty("#size-cells",(long)sizeCells));
				Vector<Parameter> vAliases = bi.getAliases();
				if (vAliases.size() > 0) {
					aliasNode = new DTNode("aliases");
					for (Parameter p : vAliases) {
						aliasNode.addProperty(new DTProperty(p.getName(),p.getValue()));
					}
				}
				vAliases = bi.getAliasRefs();
				if (vAliases.size() > 0) {
					if(aliasNode==null) {
						aliasNode = new DTNode("aliases");
					}
					for (Parameter p : vAliases) {
						BasicComponent slave = sys.getComponentByName(p.getValue());
						if(slave!=null) {
							Vector<Connection> vConn = sys.getConnectionPath(povComponent, slave,SystemDataType.MEMORY_MAPPED);
							String path="/" + sopcNode.getName();
							for(Connection c : vConn) {
								path += "/" + c.getSlaveModule().getScd().getGroup() + "@" + DTHelper.longArrToHexString(c.getConnValue());
							}
							if(path.length()>0) {
								aliasNode.addProperty(new DTProperty(p.getName(),path));								
							} else {
								Logger.logln(this, "Failed to find component '" + p.getValue() +"' path for alias: " + p.getName(), LogLevel.WARNING);
							}
						} else {
							Logger.logln(this, "Failed to find component '" + p.getValue() +"' for alias: " + p.getName(), LogLevel.WARNING);
						}
					}
				}
				
				if(aliasNode!=null) {
				    rootNode.addChild(aliasNode);
				}
				rootNode.addChild(cpuNode);
				rootNode.addChild(memNode);
				sopcNode.addProperty(new DTProperty("device_type", "soc"));
			} else {
				sopcNode = rootNode;
				chosenNode = null;
			}
			sopcNode = getSlavesFor(bi, povComponent, sopcNode);
			sopcNode.addProperty(new DTProperty("ranges"));
			sopcNode.addProperty(new DTProperty("#address-cells",(long)addrCells));
			sopcNode.addProperty(new DTProperty("#size-cells",(long)sizeCells));
			Vector<String> vCompat = new Vector<String>();
			vCompat.add("ALTR,avalon");
			vCompat.add("simple-bus");
			sopcNode.addProperty(new DTProperty("compatible", vCompat.toArray(new String[]{})));
			sopcNode.addProperty(new DTProperty("bus-frequency", povComponent.getClockRate()));
			if(bi.getPovType().equals(PovType.CPU))
			{
				if(clocksNode!=null) {
					rootNode.addChild(clocksNode);
				}
				rootNode.addChild(sopcNode);
				rootNode.addChild(chosenNode);
			}
		}
		doDTAppend(rootNode,bi);
		return rootNode;
	}
	private void doDTAppend(DTNode rootNode, BoardInfo bi) {
		Vector<BICDTAppend> appends = bi.getDTAppends();
		for(BICDTAppend dta : appends) {
			DTNode parent = null;
			if(dta.getParentLabel()!=null) {
				if (dta.getParentLabel().equalsIgnoreCase("")) {
					parent = rootNode;
				} else {
					parent = DTHelper.getChildByLabel(rootNode, dta.getParentLabel());
				}
			}
			if((parent==null) && (dta.getParentPath()!=null)) {
				parent = rootNode;
				for(String nodeStr : dta.getParentPath()) {
					DTNode n = parent;
					parent = null;
					for(DTNode child : n.getChildren()) {
						if(child.getName().equalsIgnoreCase(nodeStr))
						{
							parent = child;
						}
					}
				}
			}
			if(parent==null) {
				String pLabel;
				if (dta.getParentLabel()!=null) {
					pLabel = "null";
				} else {
					pLabel = dta.getParentLabel();
				}
				Logger.logln("DTAppend: Unable to find parent, "+pLabel+", for " + dta.getInstanceName() + ". Adding to root", LogLevel.WARNING);
				parent = rootNode;
			}
			Vector<DTAppendType> vTypes = dta.getTypes();
			if ((vTypes.size() > 0) && (vTypes.get(0) == DTAppendType.NODE)) {
				parent.addChild(new DTNode(dta.getInstanceName(),dta.getLabel()));
			} else {
				Vector<String> vValues = dta.getValues();
				if (vValues.size() != vTypes.size()){
					Logger.logln("doDTAppend size of vValues not equal to size of vTypes: " +
					vValues.size() + " != " + vTypes.size() + " for instance "+
					dta.getInstanceName(), LogLevel.ERROR);
					return;
				}
				DTProperty prop = parent.getPropertyByName(dta.getInstanceName());
				if(dta.getAction() == DTAppendAction.REMOVE) {
					parent.getProperties().remove(prop);
				} else {
					if((dta.getAction() == DTAppendAction.REPLACE) || (prop == null))
					{
						prop = new DTProperty(dta.getInstanceName(), dta.getLabel(), boardInfoComment);
					}
					for (int i = 0; i < vTypes.size(); i++) {
						switch(vTypes.get(i)) {
						case PROP_NUMBER: {
							prop.addValue(new DTPropNumVal(Long.decode(vValues.get(i))));
						} break;
						case PROP_HEX: {
							prop.addValue(new DTPropHexNumVal(Long.decode(vValues.get(i))));
						} break;
						case PROP_BYTE: {
							prop.addValue(new DTPropByteVal(Integer.decode(vValues.get(i))));
						} break;
						case PROP_STRING: {
							prop.addValue(new DTPropStringVal(vValues.get(i)));
						} break;
						case PROP_PHANDLE: {
							prop.addValue(new DTPropPHandleVal(vValues.get(i),0));
						} break;
						default: /* Bools don't have values */
						}
					}
					parent.addProperty(prop, true);
				}
			}
		}
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
		chosenNode.addProperty(new DTProperty("bootargs", bi.getBootArgs()));
		return chosenNode;
	}
	DTNode getClocksNode(BoardInfo bi)
	{
		DTNode cn = null;
		if(bi.isShowClockTree()) {
			cn = new DTNode("clocks");
			for(BasicComponent comp : sys.getSystemComponents()) {
				if(comp instanceof ClockSource) {
					if(cn.getProperties().size()==0) {
						cn.addProperty(new DTProperty("#address-cells",1L));
						cn.addProperty(new DTProperty("#size-cells",1L));
					}
					cn.addChild(comp.toDTNode(bi, null));
					vHandled.add(comp);
				}
			}
		}
		return cn;
	}
	DTNode getCpuNodes(BoardInfo bi, BasicComponent povComp)
	{
		int numCPUs = 0;
		DTNode cpuNode = new DTNode("cpus");
		if(bi.getPovType() == PovType.CPU)
		{
			CpuComponent firstCpu = null;
			if(povComp instanceof CpuComponent)
			{
				firstCpu = (CpuComponent)povComp;
			}
			cpuNode.addProperty(new DTProperty("#address-cells",1L));
			cpuNode.addProperty(new DTProperty("#size-cells",0L));
			for(BasicComponent comp : sys.getSystemComponents())
			{
				if(comp instanceof CpuComponent)
				{
					CpuComponent cpu = (CpuComponent)comp;
					if((firstCpu == null) || (firstCpu == cpu) || (firstCpu.isSmpCapableWith(cpu)))
					{
						if(bi.getPov()==null) {
							bi.setPov(comp.getInstanceName());
						}
						if(firstCpu == null) {
							firstCpu = cpu;
						}
						cpu.setCpuIndex(numCPUs);
						cpuNode.addChild(comp.toDTNode(bi, null));
						numCPUs++;
					}
					vHandled.add(comp);
				}
			}
		}
		if(cpuNode.getChildren().isEmpty())
		{
			cpuNode = null;
		}
		return cpuNode;
	}
	DTNode getMemoryNode(BoardInfo bi, BasicComponent master, int addrCells, int sizeCells)
	{
		DTNode memNode = new DTNode("memory");
		DTProperty dtpReg = new DTProperty("reg");
		dtpReg.getValues().clear();
		dtpReg.setNumValuesPerRow(addrCells + sizeCells);
		memNode.addProperty(new DTProperty("device_type", "memory"));
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
									dtpReg.addHexValues(mem.getBase());
									dtpReg.addHexValues(mem.getSize());
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
										dtpReg.addHexValues(mem.getBase());
										dtpReg.addHexValues(mem.getSize());
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
			sortSlaves(vSlaveConn, bi.getSortType());
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
	protected static void sortSlaves(Vector<Connection> vConn, final SortType sort) {
		if(!sort.equals(SortType.NONE)) {
			Collections.sort(vConn, new Comparator<Connection>() {

				public int compare(Connection c1, Connection c2) {
					switch(sort) {
					case ADDRESS:
						return DTHelper.longArrCompare(c1.getConnValue(), c2.getConnValue());
					case NAME:
						int cmp = c1.getSlaveModule().getScd().getGroup().compareToIgnoreCase(c2.getSlaveModule().getScd().getGroup());
						if(cmp!=0) {
							return cmp;
						}
						/* Fallthrough and decide by label */
					case LABEL:
						return c1.getSlaveModule().getInstanceName().compareToIgnoreCase(c2.getSlaveModule().getInstanceName());
					default: return 0;
					}
				}
			});
		}
	}
}
