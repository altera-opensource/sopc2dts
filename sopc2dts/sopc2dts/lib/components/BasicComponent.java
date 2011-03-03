package sopc2dts.lib.components;

import java.util.Vector;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.BasicElement;
import sopc2dts.lib.Parameter;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;

public class BasicComponent extends BasicElement {
	public enum parameter_action { NONE, CMACRCO, ALL };
	private String instanceName;
	protected String version;
	private int addr = 0;
	protected Vector<Interface> vInterfaces = new Vector<Interface>();
	protected SopcComponentDescription scd;

	public BasicComponent(SopcComponentDescription scd, String iName, String ver)
	{
		this.setScd(scd);
		this.setInstanceName(iName);
		version = ver;
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
					res += " 0x" + Integer.toHexString(getAddrFromConnection(conn)) + 
							" 0x" + Integer.toHexString(intf.getInterfaceValue());
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
		if(irqParent!=null)
		{
			return AbstractSopcGenerator.indent(indentLevel) + "interrupt-parent = < &" + irqParent.getInstanceName() + " >;\n" +
					interrupts + " >;\n";
		} else {
			return "";
		}
	}
	public String toDts(BoardInfo bi, int indentLevel, BasicComponent.parameter_action paramAction)
	{
		return toDts(bi, indentLevel, paramAction, null, true);
	}
	public String toDts(BoardInfo bi, int indentLevel, 
						BasicComponent.parameter_action paramAction, 
						Connection conn, Boolean endComponent)
	{
		int tmpAddr = getAddrFromConnection(conn);
		String res = AbstractSopcGenerator.indent(indentLevel++) + getInstanceName() + ": " + getScd().getGroup() + "@0x" + Integer.toHexString(tmpAddr) + " {\n";
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
		if((paramAction != parameter_action.NONE)&&(vParameters.size()>0))
		{
			res += AbstractSopcGenerator.indent(indentLevel) + "//Dumping SOPC parameters...\n";
			for(Parameter bp : vParameters)
			{
				String assName = bp.getName();
				if(assName.startsWith("embeddedsw.CMacro.")) {
					assName = assName.substring(18);
				} else if(paramAction == parameter_action.CMACRCO) {
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
		this.scd = scd;
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
	public void setAddr(int addr) {
		this.addr = addr;
	}
	public int getAddr() {
		return addr;
	}
	public int getAddrFromMaster()
	{
		return getAddrFromMaster(0);
	}
	public int getAddrFromMaster(int index)
	{
		for(Interface intf : vInterfaces)
		{
			if(intf.isMemorySlave())
			{
				if(intf.getConnections().size()>index)
				{
					return intf.getConnections().get(index).getConnValue();
				}
			}
		}
		return -1;
	}
	protected int getAddrFromConnection(Connection conn)
	{
		return (conn==null ? getAddr() : conn.getConnValue());
	}
	protected int getSizeFromInterface(Interface intf)
	{
		return (intf==null ? 0 : intf.getInterfaceValue());
	}
	public int getClockRate()
	{
		int rate = 0;
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
	 */
	public void removeFromSystemIfPossible(AvalonSystem sys)
	{
		
	}
}
