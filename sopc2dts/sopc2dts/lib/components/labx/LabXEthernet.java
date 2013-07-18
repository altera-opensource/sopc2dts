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
package sopc2dts.lib.components.labx;

import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.base.SICEthernet;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTProperty;

public class LabXEthernet extends SICEthernet {

	private static final String SUPPORTED_MACS[] = { 
		"labx_eth_mac",
		"labx_10g_eth_mac",
	};

	public LabXEthernet(String cName, String iName, String version, SopcComponentDescription sdc) {
		super(cName, iName, version, sdc);
	}
	
	@Override
	public DTNode toDTNode(BoardInfo bi,Connection conn)
	{
		DTNode res = super.toDTNode(bi, conn);
		// Add our custom properties
		res.addProperty(new DTProperty("phy-mode", getPhyModeString()));
		return res;
	}

	private BasicComponent findLabXEthMac(Interface intf)
	{
		BasicComponent comp = null;
		if(intf!=null)
		{
			if(!intf.getConnections().isEmpty())
			{
				comp = intf.getConnections().firstElement().getSlaveModule();
				if(intf.getOwner().equals(comp)) {
					//We've found ourselves...
					comp = intf.getConnections().firstElement().getMasterModule();
				}

        // Traverse the slave connections until we find one of our
        // supported MAC components
        boolean foundMac = false;
        for(String typeString : SUPPORTED_MACS) {
          if(comp.getClassName().equalsIgnoreCase(typeString)) {
            foundMac = true;
            break;
          }
        }

				if(foundMac == false) {
					if(comp.getClassName().equalsIgnoreCase("labx_eth_tx_arbiter"))
					{
						comp = findLabXEthMac(comp.getInterfaceByName("mac_tx"));
					} else {
						// Fail...
						comp = null;
					}
				}
			}
		}
		return comp;
	}
	
	protected String getPhyModeString()
	{
		String res = "UNKNOWN";
		BasicComponent labx_eth_mac = findLabXEthMac(getInterfaceByName("mac_tx"));
		if(labx_eth_mac != null) {
			res = labx_eth_mac.getParamValByName("MII_TYPE");
		}
		return res;
	}

	// Protected overrides from base class SICEthernet
	@Override
	protected int getMaxFrameSize()
	{
    // Not that this should matter, but we support VLAN tags
		return 1522;
	}
}
