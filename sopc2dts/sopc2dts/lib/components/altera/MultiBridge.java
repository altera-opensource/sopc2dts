/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2013 Walter Goossens <waltergoossens@home.nl>

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
package sopc2dts.lib.components.altera;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.base.SICBridge;

public class MultiBridge extends SICBridge {
	String[] hps2fpgaBridgeNames = new String[]{ "h2f", "h2f_lw" }; 
	static SopcComponentDescription hpsBridgeSCD = new SopcComponentDescription("bridge", "bridge", "ALTR", "bridge");
	SICBridge f2hBridge = null;
	
	public MultiBridge(String cName, String iName, String ver, SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}
	public boolean removeFromSystemIfPossible(AvalonSystem sys)
	{
		if(f2hBridge == null) {
			f2hBridge = getBridge("f2h",null);			
			sys.addSystemComponent(f2hBridge);
			for(int i=0; i<hps2fpgaBridgeNames.length; i++) {
				Interface intf = getInterfaceByName(hps2fpgaBridgeNames[i]);
				if (intf != null) {
					createVirtualAddresses(intf, i);
				}
			}
			return true;
		} else {
			return false;
		}
	}
	SICBridge getBridge(String baseName, SICBridge bridge) {
		String[] intfNames = { "axi_%s", "%s_reset", "%s_axi_clock", "%s" };
		if(bridge == null) {
			bridge = new SICBridge(getClassName(), getInstanceName() + '_' + baseName, version, hpsBridgeSCD);	
		}
		for(String intfNameFormat : intfNames) {
			String ifName = String.format(intfNameFormat, baseName);
			Interface intf = getInterfaceByName(ifName);
			if(intf != null) {
				this.removeInterface(intf);
				bridge.addInterface(intf);
			} else {
				Logger.logln("Failed to find " + ifName, LogLevel.DEBUG);
			}
		}
		return bridge;
	}
	private void createVirtualAddresses(Interface master, long cs) {
		int width = master.getPrimaryWidth()+1;
		long[] connVal;

		master.setPrimaryWidth(width);
		for(Connection conn : master.getConnections()) {
			long[] newConnVal = new long[width];
			newConnVal[0] = cs;
			connVal = conn.getConnValue();
			System.arraycopy(connVal, 0, newConnVal, 1, connVal.length);
			conn.setConnValue(newConnVal);
		}
	}
	@Override
	protected long[] translateAddress(Connection masterConn, Connection slaveConn) {
		Interface upstreamSlave = getInterfaceByName("axi_" + slaveConn.getMasterInterface().getName());
		if(upstreamSlave != masterConn.getSlaveInterface()) {
			if(upstreamSlave.getConnections().size()>0) {
				masterConn = upstreamSlave.getConnections().firstElement();
			}
		}
		return translateAddress(masterConn.getConnValue(),slaveConn.getConnValue());
	}
	@Override
	protected long[] translateAddress(long[] mAddr, long[] sAddr) {
		long[] nsAddr;
		if(mAddr.length == (sAddr.length-1))
		{
			//Strip CS field. Pray to the FSM that mAddr is ok...
			nsAddr = new long[mAddr.length];
			System.arraycopy(sAddr, 1, nsAddr, 0, nsAddr.length);
		} else {
			nsAddr = sAddr;
		}
		return super.translateAddress(mAddr, nsAddr);
	}
}
