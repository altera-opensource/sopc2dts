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
package sopc2dts.lib.components;

import java.util.Vector;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.BasicElement;
import sopc2dts.lib.Parameter;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.base.SICUnknown;

public class BasicComponent extends BasicElement {
	private static final long serialVersionUID = -4790737466253508122L;
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
	protected String getRegForDTS(int indentLevel, BasicComponent master)
	{
		String res = "";
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
					if(res.length()==0)
					{
						res = AbstractSopcGenerator.indent(indentLevel) + "reg = <";
					}
					res += " 0x" + Long.toHexString(getAddrFromConnection(conn)) + 
							" 0x" + Long.toHexString(intf.getInterfaceValue());
				}
			}
		}
		if(res.length()>0)
		{
			res += ">;\n";
		}
		return res;
	}

	protected String getInterruptsForDTS(int indentLevel)
	{
		String interrupts =AbstractSopcGenerator.indent(indentLevel) + "interrupts = <";
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
					}
					if(intf.getConnections().get(0).getMasterModule().equals(irqParent))
					{
						interrupts += " " + intf.getConnections().get(0).getConnValue();
					}
				}
			}
		}
		if(irqParent!=null)
		{
			return AbstractSopcGenerator.indent(indentLevel) + "interrupt-parent = < &" + irqParent.getInstanceName() + " >;\n" +
					interrupts + " >;\n";
		} else {
			return "";
		}
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
		res += getInterruptMasterDesc(indentLevel);
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
				String assName = bp.getName();
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
	private String getInterruptMasterDesc(int indentLevel) {
		for(Interface intf : getInterfaces())
		{
			if(intf.isIRQMaster())
			{
				return AbstractSopcGenerator.indent(indentLevel) + "interrupt-controller;\n" +
				AbstractSopcGenerator.indent(indentLevel) + "#interrupt-cells = <1>;\n";
			}
		}
		return "";
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
	public void setAddr(int addr) {
		this.addr = addr;
	}
	public int getAddr() {
		return addr;
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
				}
			}
		}
		return rate;
	}
	
	public boolean hasMemoryMaster()
	{
		for(Interface intf : vInterfaces)
		{
			if(intf.isMemoryMaster()) return true;
		}
		return false;
	}
	/*
	 * Subclasses can implement this to optimize systems and/or flatten 
	 * otherwise needless complex systems
	 * returns true when the system is modified
	 */
	public boolean removeFromSystemIfPossible(AvalonSystem sys)
	{
		return false;
	}
	public String getClassName() {
		return className;
	}
}
