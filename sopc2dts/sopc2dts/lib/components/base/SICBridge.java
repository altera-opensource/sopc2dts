package sopc2dts.lib.components.base;

import sopc2dts.Logger;
import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;


public class SICBridge extends BasicComponent {

	public SICBridge(SopcComponentDescription scd, String iName, String version) {
		super(scd, iName, version);
	}
	
	protected String getDtsRanges(int indentLevel, Connection conn)
	{
		String res = "";
		for(Interface master : getInterfaces())
		{
			if(master.isMemoryMaster())
			{
				for(Connection childConn : master.getConnections())
				{
					int size = 0;
					Interface childIf=childConn.getSlaveInterface();
					if(childIf!=null) size = childIf.getInterfaceValue();
					if(res.length()==0)
					{
						res = AbstractSopcGenerator.indent(indentLevel) + "ranges = <"; 
					} else {
						res += "\n" + AbstractSopcGenerator.indent(indentLevel) + "\t";
					}					
					res += String.format("0x%08X 0x%08X 0x%08X", childConn.getConnValue(),
							childConn.getConnValue() + conn.getConnValue(), size);
				}
			}
		}
		if(res.length() == 0)
		{
			res = AbstractSopcGenerator.indent(indentLevel) + "ranges;\n";
		} else {
			res += ">;\n";
		}
		return res;
	}

	@Override
	public String toDtsExtras(BoardInfo bi, int indentLevel, Connection conn, Boolean endComponent)
	{
		return AbstractSopcGenerator.indent(indentLevel) + "#address-cells = <1>;\n" +
				AbstractSopcGenerator.indent(indentLevel) + "#size-cells = <1>;\n" +
				getDtsRanges(indentLevel,conn);
	}
	private void removeFromSystem(AvalonSystem sys)
	{
		Logger.logln("Try to eliminate " + getScd().getClassName() + ": " + getInstanceName());
		Interface masterIntf = null, slaveIntf = null;
		for(Interface intf : getInterfaces())
		{
			if(intf.isClockSlave())
			{
				//Remove clocks
				for(Connection conn : intf.getConnections())
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
		Connection masterConn;
		while(slaveIntf.getConnections().size()>0)
		{
			masterConn = slaveIntf.getConnections().firstElement();
			Logger.logln("Master of bridge: " + masterConn.getMasterModule().getInstanceName() + " name " + masterConn.getMasterInterface().getName());
			Logger.logln("Slave of bridge: " + masterIntf.getName() + " num slaves: " + masterIntf.getConnections().size());
			for(Connection slaveConn : masterIntf.getConnections())
			{
				//Connect slaves to our masters
				Connection conn = new Connection(slaveConn);
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
		Connection slaveConn;
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
	@Override
	public void removeFromSystemIfPossible(AvalonSystem sys)
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
