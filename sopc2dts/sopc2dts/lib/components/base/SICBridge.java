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
package sopc2dts.lib.components.base;

import java.util.Vector;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.devicetree.DTHelper;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTPropHexNumber;
import sopc2dts.lib.devicetree.DTPropNumber;
import sopc2dts.lib.devicetree.DTProperty;


public class SICBridge extends BasicComponent {

	public SICBridge(BasicComponent comp) {
		super(comp);
	}
	public SICBridge(String cName, String iName, String version, SopcComponentDescription scd) {
		super(cName, iName, version,scd);
	}
	protected Vector<Long> getDtRanges(Connection conn)
	{
		Vector<Long> vRanges = new Vector<Long>();
		for(Interface master : getInterfaces())
		{
			if(master.isMemoryMaster())
			{
				for(Connection childConn : master.getConnections())
				{
					Interface childIf=childConn.getSlaveInterface();
					long[] addr = DTHelper.longArrAdd(childConn.getConnValue(), conn.getConnValue());
					DTHelper.addAllLongs(vRanges, childConn.getConnValue());
					DTHelper.addAllLongs(vRanges, addr);
					DTHelper.addAllLongs(vRanges, childIf.getInterfaceValue());
				}
			}
		}
		return vRanges;
	}

	@Override
	public DTNode toDTNode(BoardInfo bi, Connection conn)
	{
		DTNode dtn = super.toDTNode(bi, conn);
		Vector<Long> vRanges = getDtRanges(conn);
		dtn.addProperty(new DTPropNumber("#address-cells", (long) getAddressCellCount(true)));
		dtn.addProperty(new DTPropNumber("#size-cells", (long) getSizeCellCount(true)));
		if(vRanges.isEmpty())
		{
			dtn.addProperty(new DTProperty("ranges"));
		} else {
			DTPropHexNumber p = new DTPropHexNumber("ranges",vRanges);
			p.setNumValuesPerRow(getAddressCellCount(true) + getAddressCellCount(false) + getSizeCellCount(true));
			dtn.addProperty(p);			
		}
		return dtn;
	}
	private boolean removeFromSystemMM(AvalonSystem sys)
	{
		Logger.logln("Try to eliminate " + getClassName() + 
				": " + getInstanceName(), LogLevel.INFO);
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
			Logger.logln("MasterIF " + masterIntf + " slaveIF " + slaveIntf, LogLevel.WARNING);
			return false;
		}
		Connection masterConn;
		while(slaveIntf.getConnections().size()>0)
		{
			masterConn = slaveIntf.getConnections().firstElement();
			Logger.logln("Master of bridge: " + masterConn.getMasterModule().getInstanceName() + 
					" name " + masterConn.getMasterInterface().getName(), LogLevel.DEBUG);
			Logger.logln("Slave of bridge: " + masterIntf.getName() + 
					" num slaves: " + masterIntf.getConnections().size(), LogLevel.DEBUG);
			for(Connection slaveConn : masterIntf.getConnections())
			{
				//Connect slaves to our masters
				Connection conn = new Connection(slaveConn);
				Logger.logln("Connection from " + conn.getMasterModule().getInstanceName() + 
						" to " + conn.getSlaveModule().getInstanceName(), LogLevel.DEBUG);
				conn.setMasterInterface(masterConn.getMasterInterface());
				masterConn.getMasterInterface().getConnections().add(conn);
				conn.getSlaveInterface().getConnections().add(conn);
				Logger.logln("Connection from " + conn.getMasterModule().getInstanceName() + 
						" to " + conn.getSlaveModule().getInstanceName(), LogLevel.DEBUG);
			}
			//Now remove connection to master
			slaveIntf.getConnections().remove(masterConn);
			Logger.logln("Master count: " + masterConn.getMasterInterface().getConnections().size(), LogLevel.DEBUG);
			masterConn.getMasterInterface().getConnections().remove(masterConn);
			Logger.logln("Master count: " + masterConn.getMasterInterface().getConnections().size(), LogLevel.DEBUG);
		}
		//Now remove all slaves...
		Connection slaveConn;
		while(masterIntf.getConnections().size()>0)
		{
			slaveConn = masterIntf.getConnections().firstElement();
//			System.out.println("Master of bridge: " + masterConn.getMasterModule().getInstanceName() + " name " + masterConn.getMasterInterface().getName());
			//Now remove connection to master
			masterIntf.getConnections().remove(slaveConn);
			Logger.logln("Slave count: " + slaveConn.getSlaveInterface().getConnections().size(), LogLevel.DEBUG);
			slaveConn.getSlaveInterface().getConnections().remove(slaveConn);
			Logger.logln("Slave count: " + slaveConn.getSlaveInterface().getConnections().size(), LogLevel.DEBUG);
		}
		//Finally remove ourselves
		sys.removeSystemComponent(this);
		return true;
	}
	private boolean removeFromSystemStreaming(AvalonSystem sys)
	{
		Interface masterIntf = null, slaveIntf = null;
		masterIntf = getInterfaces(SystemDataType.STREAMING, true).firstElement();
		slaveIntf = getInterfaces(SystemDataType.STREAMING, false).firstElement();
		if((masterIntf!=null)&&(slaveIntf!=null))
		{
			Connection conn = slaveIntf.getConnections().firstElement();
			Connection oldConn = masterIntf.getConnections().firstElement();
			if(conn!=null)
			{
				//Remove clocks
				for(Interface intf : getInterfaces(SystemDataType.CLOCK,false))
				{
					for(Connection clkConn : intf.getConnections())
					{
						clkConn.getMasterInterface().getConnections().remove(clkConn);
					}
				}
				
				conn.setSlaveInterface(oldConn.getSlaveInterface());
				oldConn.getSlaveInterface().getConnections().remove(oldConn);
				oldConn.getSlaveInterface().getConnections().add(conn);
				sys.removeSystemComponent(this);
				return true;				
			}
		}
		return false;
	}

	private int getAddressCellCount(boolean masterSide) {
		for(Interface intf : vInterfaces) {
			if(intf.isMemory() && intf.isMaster() == masterSide) {
				return intf.getPrimaryWidth();
			}
		}
		return 1;
	}
	private int getSizeCellCount(boolean masterSide) {
		for(Interface intf : vInterfaces) {
			if(intf.isMemory() && intf.isMaster() == masterSide) {
				return intf.getSecondaryWidth();
			}
		}
		return 1;
	}
	@Override
	public boolean removeFromSystemIfPossible(AvalonSystem sys)
	{
		if(isStreamingBridge())
		{
			return removeFromSystemStreaming(sys);
		} else {
			boolean remove = false;
			if(getScd().isSupportingClassName("altera_avalon_tri_state_bridge"))
			{
				//Always remove tristate bridges.
				remove = true;
			} else if((getScd().isSupportingClassName("altera_avalon_pipeline_bridge") ||
					getScd().isSupportingClassName("altera_avalon_clock_crossing") ||
					getScd().isSupportingClassName("altera_avalon_half_rate_bridge")) &&
					(!this.isTranslatingBridge()))
			{
				remove = true;
			}
			if(remove)
			{
				return removeFromSystemMM(sys);
			} else {
				return false;
			}
		}
	}
	protected boolean isStreamingBridge()
	{
		int numStreamMasters = 0;
		int numStreamSlaves = 0;
		for(Interface intf : vInterfaces)
		{
			if(intf.isMemorySlave())
			{
				return false;
			}
			if(intf.getType() == SystemDataType.STREAMING)
			{
				if(intf.isMaster())
				{
					numStreamMasters++;
				} else {
					numStreamSlaves++;
				}
			}
		}
		return ((numStreamMasters==1) && (numStreamSlaves==1));
	}
	protected boolean isTranslatingBridge()
	{
		for(Interface slave : vInterfaces)
		{
			if(slave.isMemorySlave())
			{
				for(Connection conn : slave.getConnections())
				{
					if(DTHelper.longArrCompare(conn.getConnValue(), 0L)!=0)
					{
						return true;
					}
				}
			}
		}
		return false;
	}
}
