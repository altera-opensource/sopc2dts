/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 - 2014 Walter Goossens <waltergoossens@home.nl>

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
import sopc2dts.lib.BoardInfo.RangesStyle;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.MemoryBlock;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.devicetree.DTHelper;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTProperty;


public class SICBridge extends BasicComponent {
	public enum BridgeRemovalStrategy { ALL, BALANCED, NONE };
	protected static BridgeRemovalStrategy removalStrategy = BridgeRemovalStrategy.BALANCED;
	
	public SICBridge(BasicComponent comp) {
		super(comp);
	}
	public SICBridge(String cName, String iName, String version, SopcComponentDescription scd) {
		super(cName, iName, version,scd);
	}
	public static void setRemovalStrategy(BridgeRemovalStrategy strat) {
		removalStrategy = strat;
	}
	public static void setRemovalStrategy(String stratStr) {
		removalStrategy = BridgeRemovalStrategy.valueOf(stratStr.toUpperCase());
	}
	
	protected long[] translateAddress(Connection masterConn, Connection slaveConn) {
		return translateAddress(masterConn.getConnValue(),slaveConn.getConnValue());
	}
	protected long[] translateAddress(long[] mAddr, long[] sAddr) {
		//The simple case is adding the slave address to the master's
		if(mAddr.length == sAddr.length) {
			//Easy
			return DTHelper.longArrAdd(mAddr, sAddr);
		} else if (mAddr.length > sAddr.length) {
			//Doable
			long[] nsAddr = new long[mAddr.length];
			int lDiff = mAddr.length - sAddr.length;
			for(int i=0; i<nsAddr.length; i++) {
				if(i<lDiff) {
					nsAddr[i] = 0;
				} else {
					nsAddr[i] = sAddr[i-lDiff];
				}
			}
			return DTHelper.longArrAdd(mAddr, nsAddr);
		} else {
			//Aaarrrrghhh!!!!
			String msg = "Unable to translate this address for you. Master:";
			for(long l : mAddr) {
				msg += String.format(" %08x", l);
			}
			msg += " slave:";
				for(long l : sAddr) {
					msg += String.format(" %08x", l);
				}
			Logger.logln(this,msg, LogLevel.ERROR);
		}
		return null; //mAddr;
	}
	protected Vector<Long> getDtRanges(Connection conn, RangesStyle rangesStyle)
	{
		Vector<Long> vRanges = new Vector<Long>();
		switch(rangesStyle) {
		case NONE:	
			if(getAddressCellCount(true) == getAddressCellCount(false))	{
				//Don't need translation for same-sized bridges
				break;
			}
		case FOR_BRIDGE: {
			for(int i=0; i<getAddressCellCount(true); i++) {
				vRanges.add(0L);
			}
			DTHelper.addAllLongs(vRanges, conn.getConnValue());
			DTHelper.addAllLongs(vRanges, conn.getSlaveInterface().getInterfaceValue());
		} break;
		case FOR_EACH_CHILD: {
			for(Interface master : getInterfaces())
			{
				if(master.isMemoryMaster())
				{
					for(Connection childConn : master.getConnections())
					{
						Interface childIf=childConn.getSlaveInterface();
						long[] addr = translateAddress(conn, childConn);
						DTHelper.addAllLongs(vRanges, childConn.getConnValue());
						DTHelper.addAllLongs(vRanges, addr);
						DTHelper.addAllLongs(vRanges, childIf.getInterfaceValue());
					}
				}
			}
		} break;
		}
		return vRanges;
	}

	@Override
	public DTNode toDTNode(BoardInfo bi, Connection conn)
	{
		DTNode dtn = super.toDTNode(bi, conn);
		Vector<Long> vRanges = getDtRanges(conn, bi.getRangesStyle());
		dtn.addProperty(new DTProperty("#address-cells", (long) getAddressCellCount(true)));
		dtn.addProperty(new DTProperty("#size-cells", (long) getSizeCellCount(true)));
		if(vRanges.isEmpty())
		{
			dtn.addProperty(new DTProperty("ranges"));
		} else {
			DTProperty p = new DTProperty("ranges");
			p.addHexValues(vRanges);
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
		boolean isAddrSpanExtender;
		if (getClassName().equalsIgnoreCase("altera_address_span_extender")) {
			isAddrSpanExtender = true;
		} else {
			isAddrSpanExtender = false;
		}
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
				if (isAddrSpanExtender) {
					conn.getSlaveInterface().setInterfaceValue(slaveIntf.getInterfaceValue());
				}
				conn.getSlaveInterface().getConnections().add(conn);
				Logger.logln("Connection from " + conn.getMasterModule().getInstanceName() + 
						" to " + conn.getSlaveModule().getInstanceName(), LogLevel.DEBUG);
				conn.setConnValue(translateAddress(masterConn, slaveConn));
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

	protected int getAddressCellCount(boolean masterSide) {
		for(Interface intf : vInterfaces) {
			if(intf.isMemory() && intf.isMaster() == masterSide) {
				return intf.getPrimaryWidth();
			}
		}
		return 1;
	}
	public Interface getBridgedInterface(Interface intf) {
		Vector<Interface> vIntf = getInterfaces(intf.getType(), !intf.isMaster());
		if(vIntf.size()>0) {
			if(vIntf.size()>1) {
				Logger.logln(this, "getBridgedInterface, there are " + vIntf.size() + 
						(intf.isMaster() ? " slave" : " master") + " ports. Choosing first one", LogLevel.WARNING);
			}
			return vIntf.firstElement();
		}
		return null;
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
			} else {
				switch(removalStrategy) {
				case ALL:
					remove = true; break;
				case NONE:
					remove = false; break;
				case BALANCED: 
					if((getScd().isSupportingClassName("altera_avalon_pipeline_bridge") ||
							getScd().isSupportingClassName("altera_avalon_clock_crossing") ||
							getScd().isSupportingClassName("altera_avalon_half_rate_bridge")) &&
							(!this.isTranslatingBridge()))
					{
						remove = true;
					}
				}
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

	private static SICBridge loopCheck = null;

	public Vector<MemoryBlock> getMemoryMap(Connection conn)
	{

		Vector<MemoryBlock> vBridgedMap = new Vector<MemoryBlock>();
		if (loopCheck == null) {
			loopCheck = this;
		} else if (loopCheck == this) {
			Logger.logln(this, "Warning, bridge loop detected and not reflected in device tree.", LogLevel.WARNING);
			return vBridgedMap;
		}

		for(Interface bridgeMaster : getInterfaces(SystemDataType.MEMORY_MAPPED, true))
		{
			vBridgedMap.addAll(bridgeMaster.getMemoryMap());
		}
		for(MemoryBlock mb : vBridgedMap)
		{
			//Offset with bridges base.
			mb.setStart(translateAddress(conn.getConnValue(),mb.getBase()));
		}
		if (loopCheck == this) {
			loopCheck = null;
		}
		return vBridgedMap;
	}
}
