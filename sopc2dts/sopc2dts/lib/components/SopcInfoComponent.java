package sopc2dts.lib.components;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.SopcInfoAssignment;
import sopc2dts.lib.SopcInfoConnection;
import sopc2dts.lib.SopcInfoElementWithParams;



public class SopcInfoComponent extends SopcInfoElementWithParams {
	public enum parameter_action { NONE, CMACRCO, ALL };
	private String instanceName;
	private String version;
	private int addr = 0;
	private Vector<SopcInfoInterface> vInterfaces = new Vector<SopcInfoInterface>();
	private SopcComponentDescription scd;
	public SopcInfoComponent(ContentHandler p, XMLReader xr, SopcComponentDescription scd, String iName, String ver)
	{
		super(p,xr);
		this.setScd(scd);
		this.setInstanceName(iName);
		version = ver;
	}
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if(localName.equalsIgnoreCase("interface"))
		{
			getInterfaces().add(new SopcInfoInterface(this, xmlReader, atts.getValue("name"), atts.getValue("kind"), this));
		} else {
			super.startElement(uri, localName, qName, atts);
		}
		
	}
	protected String getRegForDTS(int indentLevel, SopcInfoConnection conn, SopcInfoInterface intf)
	{
		if((conn!=null) && (intf!=null))
		{
			return AbstractSopcGenerator.indent(indentLevel) + "reg = <0x" + Integer.toHexString(conn.getBaseAddress()) + 
					" 0x" + Integer.toHexString(intf.getAddressableSize()) + ">;\n";
		} else {
			return "";
		}
	}

	protected String getInterruptsForDTS(int indentLevel)
	{
		String interrupts =AbstractSopcGenerator.indent(indentLevel) + "interrupts = <";
		SopcInfoComponent irqParent = null;
		for(SopcInfoInterface intf : getInterfaces())
		{
			if(intf.getKind().equalsIgnoreCase("interrupt_sender"))
			{
				if(irqParent==null)
				{
					irqParent = intf.getConnections().get(0).getMasterInterface().getOwner();
				}
				if(intf.getConnections().get(0).getMasterInterface().getOwner().equals(irqParent))
				{
					interrupts += " " + intf.getConnections().get(0).getParamValue("irqNumber");
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
	public String toDts(int indentLevel, SopcInfoComponent.parameter_action paramAction)
	{
		return toDts(indentLevel, paramAction, null, true);
	}
	public String toDts(int indentLevel, 
						SopcInfoComponent.parameter_action paramAction, 
						SopcInfoConnection conn, Boolean endComponent)
	{
		int tmpAddr = getAddrFromConnection(conn);
		String res = AbstractSopcGenerator.indent(indentLevel++) + getInstanceName() + ": " + getScd().getGroup() + "@0x" + Integer.toHexString(tmpAddr) + " {\n";
		res += toDtsExtrasFirst(indentLevel, conn, endComponent);
		if((getScd().getGroup().equalsIgnoreCase("cpu"))||(getScd().getGroup().equalsIgnoreCase("memory")))
		{
			res += AbstractSopcGenerator.indent(indentLevel) + "device-type = \"" + getScd().getGroup() +"\";\n";
		}
		res += AbstractSopcGenerator.indent(indentLevel) + "compatible = " + getScd().getCompatible(version);
		res += ";\n";
		if (getScd().getGroup().equalsIgnoreCase("cpu"))
		{
			res += AbstractSopcGenerator.indent(indentLevel) + "reg = <" + getAddr() + ">;\n";
		} else if(conn!=null)
		{
			res += getRegForDTS(indentLevel, conn, conn.getSlaveInterface());
		}
		res += getInterruptMasterDesc(indentLevel);
		res += getInterruptsForDTS(indentLevel);
		for(SopcComponentDescription.SICAutoParam ap : getScd().vAutoParams)
		{
			String ass = getParamValue(ap.sopcInfoName);
			if(ass!=null)
			{
				res += AbstractSopcGenerator.indent(indentLevel) + ap.dtsName + " = <" + ass + ">;\n";
			} else if(ap.dtsName.equalsIgnoreCase("clock-frequency"))
			{
				res += AbstractSopcGenerator.indent(indentLevel) + ap.dtsName + " = <" + getClockRate() + ">;\n";
			} else if(ap.dtsName.equalsIgnoreCase("regstep"))
			{
				res += AbstractSopcGenerator.indent(indentLevel) + ap.dtsName + " = <4>;\n";
			}
		}		
		if((paramAction != parameter_action.NONE)&&(getParams().size()>0))
		{
			res += AbstractSopcGenerator.indent(indentLevel) + "//Dumping SOPC parameters...\n";
			for(SopcInfoAssignment ass : getParams())
			{
				String assName = ass.getName();
				if(assName.startsWith("embeddedsw.CMacro.")) {
					assName = assName.substring(18);
				} else if(paramAction == parameter_action.CMACRCO) {
					assName = null;
				}
				if(assName!=null)
				{
					assName = assName.replace('_', '-');
					res += AbstractSopcGenerator.indent(indentLevel) + 
							scd.getVendor() + "," + assName + " = \"" + ass.getValue() + "\";\n";
				}
			}
		}
		res += toDtsExtras(indentLevel, conn, endComponent);
		if(endComponent) res += AbstractSopcGenerator.indent(--indentLevel) + "};\n";
		return res;
	}
	private String getInterruptMasterDesc(int indentLevel) {
		for(SopcInfoInterface intf : getInterfaces())
		{
			if(intf.getKind().equalsIgnoreCase("interrupt_receiver"))
			{
				return AbstractSopcGenerator.indent(indentLevel) + "interrupt-controller;\n" +
				AbstractSopcGenerator.indent(indentLevel) + "#interrupt-cells = <1>;\n";
			}
		}
		return "";
	}
	public String toDtsExtrasFirst(int indentLevel, SopcInfoConnection conn, Boolean endComponent)
	{
		return "";
	}
	public String toDtsExtras(int indentLevel, SopcInfoConnection conn, Boolean endComponent)
	{
		return "";
	}
	@Override
	public String getElementName() {
		return "module";
	}
	public SopcInfoInterface getInterfaceByName(String ifName)
	{
		for(SopcInfoInterface intf : getInterfaces())
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
	public void setInterfaces(Vector<SopcInfoInterface> vInterfaces) {
		this.vInterfaces = vInterfaces;
	}
	public Vector<SopcInfoInterface> getInterfaces() {
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
		for(SopcInfoInterface intf : vInterfaces)
		{
			if(intf.isMemorySlave())
			{
				if(intf.getConnections().size()>index)
				{
					return intf.getConnections().get(index).getBaseAddress();
				}
			}
		}
		return -1;
	}
	protected int getAddrFromConnection(SopcInfoConnection conn)
	{
		return (conn==null ? getAddr() : conn.getBaseAddress());
	}
	public int getClockRate()
	{
		int rate = 0;
		for(SopcInfoInterface intf : vInterfaces)
		{
			if(intf.isClockInput())
			{
				rate = Integer.decode(intf.getConnections().get(0).getMasterInterface().getParamValue("clockRate"));
			}
		}
		return rate;
	}
	public boolean hasMemoryMaster()
	{
		for(SopcInfoInterface intf : vInterfaces)
		{
			if(intf.isMemoryMaster()) return true;
		}
		return false;
	}
}
