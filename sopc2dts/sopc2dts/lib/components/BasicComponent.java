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
package sopc2dts.lib.components;

import java.util.NoSuchElementException;
import java.util.Vector;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.BasicElement;
import sopc2dts.lib.Parameter;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.base.SICUnknown;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTPropBool;
import sopc2dts.lib.devicetree.DTPropHexNumber;
import sopc2dts.lib.devicetree.DTPropNumber;
import sopc2dts.lib.devicetree.DTPropPHandle;
import sopc2dts.lib.devicetree.DTPropString;

public class BasicComponent extends BasicElement {
	public enum parameter_action { NONE, CMACRCO, ALL };
	private String instanceName;
	private String className;
	protected String version;
	private int addr = 0;
	protected Vector<Interface> vInterfaces = new Vector<Interface>();
	protected SopcComponentDescription scd;
	
	public BasicComponent(String cName, String iName, String ver,SopcComponentDescription scd)
	{
		this.className = cName;
		setScd(scd);
		this.instanceName = iName;
		this.version = ver;
	}
	protected BasicComponent(BasicComponent bc)
	{
		super(bc);
		this.instanceName = bc.instanceName;
		this.className = bc.className;
		this.version = bc.className;
		this.scd = bc.scd;
		this.vInterfaces = bc.vInterfaces;
		for(Interface intf : vInterfaces)
		{
			intf.setOwner(this);
		}
	}
	protected Vector<Long> getReg(BasicComponent master)
	{
		Vector<Long> vRegs = new Vector<Long>();		
		for(Interface intf : vInterfaces)
		{
			if(intf.isMemorySlave())
			{
				//Check all interfaces for a connection to master
				Connection conn = null;
				for(int i=0; (i<intf.getConnections().size()) && (conn==null); i++)
				{
					if(intf.getConnections().get(i).getMasterModule().equals(master))
					{
						conn = intf.getConnections().get(i);
					}
				}
				if((conn!=null) && (intf!=null))
				{
					vRegs.add(getAddrFromConnection(conn));
					vRegs.add(intf.getInterfaceValue());
				}
			}
		}
		return vRegs;
	}

	protected String getRegForDTS(int indentLevel, BasicComponent master)
	{
		String res = "";
		Vector<Long> vRegs = getReg(master);
		if(vRegs.size()>0)
		{
			res = AbstractSopcGenerator.indent(indentLevel) + "reg = <";
			for(Long regV : vRegs)
			{
				res += " 0x" + Long.toHexString(regV);
			}
			res += ">;\n";
		}
		return res;
	}
	protected BasicComponent getInterrupts(Vector<Long> vIrqs)
	{
		BasicComponent irqParent = null;
		for(Interface intf : getInterfaces())
		{
			if(intf.isIRQSlave())
			{
				if(intf.getConnections().size()>0)
				{
					if(irqParent==null)
					{
						irqParent = intf.getConnections().get(0).getMasterModule();
					} else if(!intf.getConnections().get(0).getMasterModule().equals(irqParent)) {
						Logger.logln(instanceName +": Multiple interrupt parents are (currently) not supported!", LogLevel.WARNING);
					}
					if(intf.getConnections().get(0).getMasterModule().equals(irqParent))
					{
						vIrqs.add(intf.getConnections().get(0).getConnValue());
					}
				}
			}
		}
		return irqParent;
	}
	protected String getInterruptsForDTS(int indentLevel)
	{
		String interrupts = "";
		Vector<Long> vIrqs = new Vector<Long>();
		BasicComponent irqParent = getInterrupts(vIrqs);
		if(irqParent!=null)
		{
			interrupts = AbstractSopcGenerator.indent(indentLevel) + "interrupt-parent = < &" + irqParent.getInstanceName() + " >;\n" +
					AbstractSopcGenerator.indent(indentLevel) + "interrupts = <";
			for(Long irq : vIrqs)
			{
				interrupts += " " + irq;
			}
			interrupts += " >;\n";
		}
		return interrupts;
	}
	public String toDts(BoardInfo bi, int indentLevel)
	{
		return toDts(bi, indentLevel, null, true);
	}
	public String toDts(BoardInfo bi, int indentLevel, 
						Connection conn, Boolean endComponent)
	{
		long tmpAddr = getAddrFromConnection(conn);
		String res = AbstractSopcGenerator.indent(indentLevel++) + getInstanceName() + ": " + getScd().getGroup() + "@0x" + Long.toHexString(tmpAddr) + " {\n";
		res += toDtsExtrasFirst(bi, indentLevel, conn, endComponent);
		if((getScd().getGroup().equalsIgnoreCase("cpu"))||(getScd().getGroup().equalsIgnoreCase("memory")))
		{
			res += AbstractSopcGenerator.indent(indentLevel) + "device_type = \"" + getScd().getGroup() +"\";\n";
		}
		res += AbstractSopcGenerator.indent(indentLevel) + "compatible = " + getScd().getCompatible(version);
		res += ";\n";
		if (getScd().getGroup().equalsIgnoreCase("cpu"))
		{
			res += AbstractSopcGenerator.indent(indentLevel) + "reg = <" + getAddr() + ">;\n";
		} else if(conn!=null)
		{
			res += getRegForDTS(indentLevel, conn.getMasterModule());
		}
		if(isInterruptMaster())
		{
			res += AbstractSopcGenerator.indent(indentLevel) + "interrupt-controller;\n" +
					AbstractSopcGenerator.indent(indentLevel) + "#interrupt-cells = <1>;\n";
		}
		res += getInterruptsForDTS(indentLevel);
		for(SopcComponentDescription.SICAutoParam ap : getScd().getAutoParams())
		{
			Parameter bp = getParamByName(ap.getSopcInfoName());
			if(bp!=null)
			{
				res += bp.toDts(indentLevel, ap.getDtsName(), 
						Parameter.getDataTypeByName(ap.getForceType()));
			} else if(ap.getDtsName().equalsIgnoreCase("clock-frequency"))
			{
				res += AbstractSopcGenerator.indent(indentLevel) + ap.getDtsName() + " = <" + getClockRate() + ">;\n";
			} else if(ap.getDtsName().equalsIgnoreCase("regstep"))
			{
				res += AbstractSopcGenerator.indent(indentLevel) + ap.getDtsName() + " = <4>;\n";
			}
		}		
		if((bi.getDumpParameters() != parameter_action.NONE)&&(vParameters.size()>0))
		{
			res += AbstractSopcGenerator.indent(indentLevel) + "//Dumping SOPC parameters...\n";
			for(Parameter bp : vParameters)
			{
				String assName = new String(bp.getName());
				if(assName.startsWith("embeddedsw.CMacro.")) {
					assName = assName.substring(18);
				} else if(bi.getDumpParameters() == parameter_action.CMACRCO) {
					assName = null;
				}
				if(assName!=null)
				{
					assName = assName.replace('_', '-');
					res += bp.toDts(indentLevel, 
							scd.getVendor() + ',' + assName, null);
				}
			}
		}
		res += toDtsExtras(bi, indentLevel, conn, endComponent);
		if(endComponent) res += AbstractSopcGenerator.indent(--indentLevel) + "};\n";
		return res;
	}

	public DTNode toDTNode(BoardInfo bi,Connection conn)
	{
		DTNode node = new DTNode(getScd().getGroup() + "@0x" + Long.toHexString(getAddrFromConnection(conn)), instanceName);
		if((getScd().getGroup().equalsIgnoreCase("cpu"))||(getScd().getGroup().equalsIgnoreCase("memory")))
		{
			node.addProperty(new DTPropString("device_type",getScd().getGroup()));
		}
		node.addProperty(new DTPropString("compatible", getScd().getCompatibles(version)));
		
		//Registers
		if (getScd().getGroup().equalsIgnoreCase("cpu"))
		{
			node.addProperty(new DTPropHexNumber("reg",new Long(getAddr())));
		} else if(conn!=null)
		{
			Vector<Long> vRegs = getReg(conn.getMasterModule());
			if(vRegs.size()>0)
			{
				node.addProperty(new DTPropHexNumber("reg",vRegs));
			}
		}

		//Interrupts
		Vector<Long> vIrqs = new Vector<Long>();
		BasicComponent irqParent = getInterrupts(vIrqs);
		if(irqParent!=null)
		{
			node.addProperty(new DTPropPHandle("interrupt-parent", irqParent.getInstanceName()));
			node.addProperty(new DTPropNumber("interrupts",vIrqs));
		}
		if(isInterruptMaster())
		{
			node.addProperty(new DTPropBool("interrupt-controller"));
			node.addProperty(new DTPropNumber("#interrupt-cells", 1L));
		}

		Vector<Parameter> vParamTodo = new Vector<Parameter>(vParameters);
		for(SopcComponentDescription.SICAutoParam ap : getScd().getAutoParams())
		{
			Parameter bp = getParamByName(ap.getSopcInfoName());
			if(bp!=null)
			{
				node.addProperty(bp.toDTProperty(ap.getDtsName(), 
						Parameter.getDataTypeByName(ap.getForceType())));
				vParamTodo.remove(bp);
			} else if(ap.getDtsName().equalsIgnoreCase("clock-frequency"))
			{
				node.addProperty(new DTPropNumber(ap.getDtsName(), getClockRate()));
			} else if(ap.getDtsName().equalsIgnoreCase("regstep"))
			{
				node.addProperty(new DTPropNumber(ap.getDtsName(), 4L));
			}
		}		
		if((bi.getDumpParameters() != parameter_action.NONE)&&(vParamTodo.size()>0))
		{
			for(Parameter bp : vParamTodo)
			{
				String assName = bp.getName();
				if(assName.startsWith("embeddedsw.CMacro.")) {
					assName = assName.substring(18);
				} else if(bi.getDumpParameters() == parameter_action.CMACRCO) {
					assName = null;
				}
				if(assName!=null)
				{
					assName = assName.replace('_', '-');
					node.addProperty(bp.toDTProperty(scd.getVendor() + ',' + assName));
				}
			}
		}
		return node;
	}
	public String toDtsExtrasFirst(BoardInfo bi, int indentLevel, Connection conn, Boolean endComponent)
	{
		return "";
	}
	public String toDtsExtras(BoardInfo bi, int indentLevel, Connection conn, Boolean endComponent)
	{
		return "";
	}
	public Interface getInterfaceByName(String ifName)
	{
		for(Interface intf : getInterfaces())
		{
			if(intf.getName().equalsIgnoreCase(ifName))
			{
				return intf;
			}
		}
		return null;
	}
	public boolean isInterruptMaster()
	{
		for(Interface intf : getInterfaces())
		{
			if(intf.isIRQMaster())
			{
				return true;
			}
		}
		return false;
	}
	public Boolean isUsefullForDTS()
	{
		return true;
	}
	public void setScd(SopcComponentDescription scd) {
		if(scd!=null)
		{
			this.scd = scd;
		} else {
			this.scd = new SICUnknown(className);
		}
	}
	public SopcComponentDescription getScd() {
		return scd;
	}
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}
	public String getInstanceName() {
		return instanceName;
	}
	public void addInterface(Interface intf) {
		intf.setOwner(this);
		vInterfaces.add(intf);
	}
	public Vector<Interface> getInterfaces() {
		return vInterfaces;
	}
	public Vector<Interface> getInterfaces(SystemDataType ofType, Boolean isMaster)
	{
		Vector<Interface> vRes = new Vector<Interface>();
		for(Interface intf: vInterfaces)
		{
			if(((ofType == null) || (intf.getType().equals(ofType))) &&
					((isMaster == null) || (intf.isMaster == isMaster)))
			{
				vRes.add(intf);
			}
		}
		return vRes;
	}
	public void removeInterface(Interface intf) {
		vInterfaces.remove(intf);
	}
	public void setAddr(int addr) {
		this.addr = addr;
	}
	public int getAddr() {
		return addr;
	}

	public Vector<Connection> getConnections(SystemDataType ofType, Boolean isMaster)
	{
		return getConnections(ofType, isMaster, null);
	}
	public Vector<Connection> getConnections(SystemDataType ofType, Boolean isMaster, BasicComponent toComponent)
	{
		Vector<Connection> conns = new Vector<Connection>();
		Vector<Interface> vInterf = getInterfaces(ofType, isMaster);
		for(Interface intf : vInterf)
		{
			for(Connection conn : intf.getConnections())
			{
				if((toComponent == null) || 
						(intf.isMaster && conn.getSlaveModule().equals(toComponent)) ||
						(!intf.isMaster && conn.getMasterModule().equals(toComponent)))
				{
					conns.add(conn);
				}
			}
		}
		return conns;
	}
	protected long getAddrFromConnection(Connection conn)
	{
		return (conn==null ? getAddr() : conn.getConnValue());
	}
	protected long getSizeFromInterface(Interface intf)
	{
		return (intf==null ? 0 : intf.getInterfaceValue());
	}
	public long getClockRate()
	{
		long rate = 0;
		for(Interface intf : vInterfaces)
		{
			if(intf.isClockSlave())
			{
				try {
					rate = intf.getConnections().firstElement().getConnValue();
				} catch(ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				} catch(NoSuchElementException e) {
					e.printStackTrace();
				}
			}
		}
		return rate;
	}
	/** @brief Whether or not this BasicComponent has a Memory master interface.
	 * 
	 * @return true when a memory-mapped master interface exists
	 */
	public boolean hasMemoryMaster()
	{
		for(Interface intf : vInterfaces)
		{
			if(intf.isMemoryMaster()) return true;
		}
		return false;
	}
	/** @brief Removes the BasicComponent from a given AvalonSystem if possible.
	 * 
	 * Subclasses can implement this to optimize systems and/or flatten 
	 * otherwise needless complex systems.
	 * 
	 * @return True when the system is modified
	 */
	public boolean removeFromSystemIfPossible(AvalonSystem sys)
	{
		return false;
	}
	public String getClassName() {
		return className;
	}
}
