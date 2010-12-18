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
			SopcComponentDescription scd, String iName) {
		super(p, xr, scd, iName);
	}
	protected String getDtsRanges(int indentLevel, SopcInfoConnection conn)
	{
		String res = "";
		for(SopcInfoInterface master : getInterfaces())
		{
			if(master.isMemoryMaster())
			{
				for(SopcInfoConnection childConn : master.getMasterConnections())
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
	public void removeFromSystemIfPossible(SopcInfoSystem sys)
	{
		if(getScd().getClassName().equalsIgnoreCase("altera_avalon_tri_state_bridge"))
		{
			Logger.logln("Try to eliminate tristate-bridge " + getInstanceName());
			SopcInfoInterface masterIntf = null, slaveIntf = null;
//			Vector<SopcInfoInterface> vMasters = new Vector<SopcInfoInterface>();
//			Vector<SopcInfoComponent> vSlaves = new Vector<SopcInfoComponent>();
			for(SopcInfoInterface intf : getInterfaces())
			{
				if(intf.isClockInput())
				{
					//Remove clocks
					for(SopcInfoConnection conn : intf.getSlaveConnections())
					{
						conn.getMasterInterface().getMasterConnections().remove(conn);
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
			while(slaveIntf.getSlaveConnections().size()>0)
			{
				masterConn = slaveIntf.getSlaveConnections().firstElement();
				Logger.logln("Master of bridge: " + masterConn.getMasterInterface().getOwner().getInstanceName() + " name " + masterConn.getMasterInterface().getName());
				for(SopcInfoConnection slaveConn : masterIntf.getMasterConnections())
				{
					//Connect slaves to our masters
					SopcInfoConnection conn = new SopcInfoConnection(slaveConn);
					Logger.logln("Connection from " + conn.getMasterInterface().getOwner().getInstanceName() + " to " + conn.getSlaveInterface().getOwner().getInstanceName());
					conn.setMasterInterface(masterConn.getMasterInterface());
					masterConn.getMasterInterface().getMasterConnections().add(conn);
					conn.getSlaveInterface().getSlaveConnections().add(conn);
					Logger.logln("Connection from " + conn.getMasterInterface().getOwner().getInstanceName() + " to " + conn.getSlaveInterface().getOwner().getInstanceName());
				}
				//Now remove connection to master
				slaveIntf.getSlaveConnections().remove(masterConn);
				Logger.logln("Master count: " + masterConn.getMasterInterface().getMasterConnections().size());
				masterConn.getMasterInterface().getMasterConnections().remove(masterConn);
				Logger.logln("Master count: " + masterConn.getMasterInterface().getMasterConnections().size());
			}
			//Now remove all slaves...
			SopcInfoConnection slaveConn;
			while(masterIntf.getSlaveConnections().size()>0)
			{
				slaveConn = masterIntf.getMasterConnections().firstElement();
//				System.out.println("Master of bridge: " + masterConn.getMasterInterface().getOwner().getInstanceName() + " name " + masterConn.getMasterInterface().getName());
				//Now remove connection to master
				masterIntf.getMasterConnections().remove(slaveConn);
				Logger.logln("Slave count: " + slaveConn.getSlaveInterface().getSlaveConnections().size());
				slaveConn.getSlaveInterface().getSlaveConnections().remove(slaveConn);
				Logger.logln("Slave count: " + slaveConn.getSlaveInterface().getSlaveConnections().size());
			}
		}
	}
}
