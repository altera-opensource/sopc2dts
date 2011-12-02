/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 Walter Goossens <waltergoossens@home.nl>

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

import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.base.SICEthernet;

public class SICTrippleSpeedEthernet extends SICEthernet {
	enum PhyMode { MII, GMII, RGMII, SGMII };
	private static final long serialVersionUID = -3828128314484790124L;

	public SICTrippleSpeedEthernet(String cName, String iName, String ver, SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}

	protected BasicComponent getDMAEngineForIntf(Interface intf)
	{
		if((intf!=null) && (intf.getConnections().size()>0))
		{
			BasicComponent comp = null;
			if(intf.isMaster())
			{
				comp = intf.getConnections().firstElement().getSlaveModule();
			} else {
				comp = intf.getConnections().firstElement().getMasterModule();
			}
			return comp;
		}
		return null;
	}
	protected PhyMode getPhyMode()
	{
		String phyModeString = getParamValByName("ifGMII");
		String phyModeStringsgmii = getParamValByName("enable_sgmii");
		PhyMode pm = PhyMode.RGMII;
		if(phyModeStringsgmii.equals("true"))
		{
			Logger.logln("enable_sgmii");	
		        pm = PhyMode.SGMII;
                } else {
			if(phyModeString.equals("MII"))
			{
				pm = PhyMode.MII;
			} else if(phyModeString.equals("MII_GMII"))
			{
				pm = PhyMode.GMII;
			} else if(phyModeString.equals("RGMII"))
			{
				pm = PhyMode.RGMII;
			}
		}
		return pm;
	}
	protected String getPhyModeString()
	{
		switch(getPhyMode())
		{
		case MII:	return "mii";
		case GMII:	return "gmii";
		case RGMII:	return "rgmii";
		case SGMII:	return "sgmii";
		default:	return "unknown";
		}
	}
}
