package sopc2dts.lib.components;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.SopcInfoAssignment;
import sopc2dts.lib.SopcInfoConnection;
import sopc2dts.lib.SopcInfoElement;
import sopc2dts.lib.SopcInfoParameter;



public class SopcInfoComponent extends SopcInfoElement {
	private String instanceName;
	private String version;
	private int addr = 0;
	public static Boolean verboseParams = false;
	private Vector<SopcInfoAssignment> vParams = new Vector<SopcInfoAssignment>();
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
		if(localName.equalsIgnoreCase("assignment"))
		{
			getParams().add(new SopcInfoAssignment(this, xmlReader));
		} else if(localName.equalsIgnoreCase("parameter"))
		{
			getParams().add(new SopcInfoParameter(this, xmlReader, atts.getValue("name")));
		} else if(localName.equalsIgnoreCase("interface"))
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
		String res="";
		for(SopcInfoInterface intf : getInterfaces())
		{
			if(intf.getKind().equalsIgnoreCase("interrupt_sender"))
			{
				res = AbstractSopcGenerator.indent(indentLevel) + "interrupt-parent = < &" + intf.getConnections().get(0).getMasterInterface().getOwner().getInstanceName() + " >;\n"
					+ AbstractSopcGenerator.indent(indentLevel) + "interrupts = <" + intf.getConnections().get(0).getParamValue("irqNumber").getValue() + ">;\n";
			}
		}
		return res;
	}
	public String toDts(int indentLevel)
	{
		return toDts(indentLevel, null, true);
	}
	public String toDts(int indentLevel, SopcInfoConnection conn, Boolean endComponent)
	{
		int tmpAddr = (conn==null ? getAddr() : conn.getBaseAddress());
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
			SopcInfoAssignment ass = getParamValue(ap.sopcInfoName);
			if(ass!=null)
			{
				res += AbstractSopcGenerator.indent(indentLevel) + ap.dtsName + " = <" + ass.getValue() + ">;\n";
			} else if(ap.dtsName.equalsIgnoreCase("clock-frequency"))
			{
				res += AbstractSopcGenerator.indent(indentLevel) + ap.dtsName + " = <" + getClockRate() + ">;\n";
			} else if(ap.dtsName.equalsIgnoreCase("regstep"))
			{
				res += AbstractSopcGenerator.indent(indentLevel) + ap.dtsName + " = <4>;\n";
			}
		}		
		if(verboseParams)
		{
			res += AbstractSopcGenerator.indent(indentLevel) + "//Dumping all SOPC parameters...\n";
			for(SopcInfoAssignment ass : getParams())
			{
				res += AbstractSopcGenerator.indent(indentLevel) + "altera," + ass.getName() + " = \"" + ass.getValue() + "\";\n";
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
	public SopcInfoAssignment getParamValue(String paramName)
	{
		for(SopcInfoAssignment ass : getParams())
		{
			if(ass.getName().equalsIgnoreCase(paramName))
			{
				return ass;
			}
		}
		return null;
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
	public void setParams(Vector<SopcInfoAssignment> vParams) {
		this.vParams = vParams;
	}
	public Vector<SopcInfoAssignment> getParams() {
		return vParams;
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
	public int getClockRate()
	{
		int rate = 0;
		for(SopcInfoInterface intf : vInterfaces)
		{
			if(intf.isClockInput())
			{
				rate = Integer.decode(intf.getConnections().get(0).getMasterInterface().getParamValue("clockRate").getValue());
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
