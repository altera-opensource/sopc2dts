package sopc2dts.lib.components.base;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import sopc2dts.Logger;
import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.SopcInfoConnection;
import sopc2dts.lib.SopcInfoSystem;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.SopcInfoComponent;
import sopc2dts.lib.components.SopcInfoInterface;


public class SICBridge extends SopcInfoComponent {

	public SICBridge(ContentHandler p, XMLReader xr,
			SopcComponentDescription scd, String iName, String version) {
		super(p, xr, scd, iName, version);
	}
	protected String getDtsRanges(int indentLevel, SopcInfoConnection conn)
	{
		String res = "";
		for(SopcInfoInterface master : getInterfaces())
		{
			if(master.isMemoryMaster())
			{
				for(SopcInfoConnection childConn : master.getConnections())
				{
					int size = 0;
					SopcInfoInterface childIf=childConn.getSlaveInterface();
					if(childIf!=null) size = childIf.getAddressableSize();
					if(res == "")
					{
						res = AbstractSopcGenerator.indent(indentLevel) + "ranges = <"; 
					} else {
						res += "\n" + AbstractSopcGenerator.indent(indentLevel) + "\t";
					}					
					res += String.format("0x%08X 0x%08X 0x%08X", childConn.getBaseAddress(),
							childConn.getBaseAddress() + conn.getBaseAddress(), size);
				}
			}
		}
		if(res=="")
		{
			res = AbstractSopcGenerator.indent(indentLevel) + "ranges;\n";
		} else {
			res += ">;\n";
		}
		return res;
	}
	public String toDtsExtras(int indentLevel, SopcInfoConnection conn, Boolean endComponent)
	{
		return AbstractSopcGenerator.indent(indentLevel) + "#address-cells = <1>;\n" +
				AbstractSopcGenerator.indent(indentLevel) + "#size-cells = <1>;\n" +
				getDtsRanges(indentLevel,conn);
	}
	private void removeFromSystem(SopcInfoSystem sys)
	{
		Logger.logln("Try to eliminate " + getScd().getClassName() + ": " + getInstanceName());
		SopcInfoInterface masterIntf = null, slaveIntf = null;
		for(SopcInfoInterface intf : getInterfaces())
		{
			if(intf.isClockInput())
			{
				//Remove clocks
				for(SopcInfoConnection conn : intf.getConnections())
				{
					conn.getMasterInterface().getConnections().remove(conn);
				}
			} else if (intf.isMemoryMaster()) {
				masterIntf = intf;
			} else if (intf.isMemorySlave()) {
				slaveIntf = intf;
			}
		}
		//Now connect all our slaves to our masters and remove ourselves
		if((masterIntf==null)||(slaveIntf==null))
		{
			//That shouldn't happen
			Logger.logln("MasterIF " + masterIntf + " slaveIF " + slaveIntf);
			return;
		}
		SopcInfoConnection masterConn;
		while(slaveIntf.getConnections().size()>0)
		{
			masterConn = slaveIntf.getConnections().firstElement();
			Logger.logln("Master of bridge: " + masterConn.getMasterModule().getInstanceName() + " name " + masterConn.getMasterInterface().getName());
			for(SopcInfoConnection slaveConn : masterIntf.getConnections())
			{
				//Connect slaves to our masters
				SopcInfoConnection conn = new SopcInfoConnection(slaveConn);
				Logger.logln("Connection from " + conn.getMasterModule().getInstanceName() + " to " + conn.getSlaveModule().getInstanceName());
				conn.setMasterInterface(masterConn.getMasterInterface());
				masterConn.getMasterInterface().getConnections().add(conn);
				conn.getSlaveInterface().getConnections().add(conn);
				Logger.logln("Connection from " + conn.getMasterModule().getInstanceName() + " to " + conn.getSlaveModule().getInstanceName());
			}
			//Now remove connection to master
			slaveIntf.getConnections().remove(masterConn);
			Logger.logln("Master count: " + masterConn.getMasterInterface().getConnections().size());
			masterConn.getMasterInterface().getConnections().remove(masterConn);
			Logger.logln("Master count: " + masterConn.getMasterInterface().getConnections().size());
		}
		//Now remove all slaves...
		SopcInfoConnection slaveConn;
		while(masterIntf.getConnections().size()>0)
		{
			slaveConn = masterIntf.getConnections().firstElement();
//			System.out.println("Master of bridge: " + masterConn.getMasterModule().getInstanceName() + " name " + masterConn.getMasterInterface().getName());
			//Now remove connection to master
			masterIntf.getConnections().remove(slaveConn);
			Logger.logln("Slave count: " + slaveConn.getSlaveInterface().getConnections().size());
			slaveConn.getSlaveInterface().getConnections().remove(slaveConn);
			Logger.logln("Slave count: " + slaveConn.getSlaveInterface().getConnections().size());
		}
	}
	public void removeFromSystemIfPossible(SopcInfoSystem sys)
	{
		boolean remove = false;
		if(getScd().getClassName().equalsIgnoreCase("altera_avalon_tri_state_bridge"))
		{
			//Always remove tristate bridges.
			remove = true;
		} else if((getScd().getClassName().equalsIgnoreCase("altera_avalon_pipeline_bridge")) &&
				(getAddrFromMaster()==0))
		{
			remove = true;
		}
		if(remove)
		{
			removeFromSystem(sys);
		}
	}
}
