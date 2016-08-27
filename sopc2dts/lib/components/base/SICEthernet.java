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
package sopc2dts.lib.components.base;

import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTProperty;
import sopc2dts.lib.devicetree.DTPropPHandleVal;
/*
 * This class handles all default network stuff, subclasses can override 
 * defaults declared in this class, or just don't and keep 'm simple.
 * This is not really a special case (as are most classes in these packages) but
 * rather a convenience class to help the lazy people such as myself.
 */
public class SICEthernet extends BasicComponent {
	protected enum PhyMode { MII, GMII, RGMII, SGMII, NONE };
	private PhyMode phymode = PhyMode.NONE;

	public SICEthernet(String cName, String iName, String version, SopcComponentDescription scd) {
		super(cName, iName, version, scd);
	}

	public SICEthernet(BasicComponent comp) {
		super(comp);
	}
	protected void setPhyMode(PhyMode pm)
	{
		phymode = pm;
	}
	protected PhyMode getPhyMode()
	{
		return phymode;
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
	@Override
	public DTNode toDTNode(BoardInfo bi, Connection conn)
	{
		DTNode node = super.toDTNode(bi, conn);
		node.addProperty(new DTProperty("address-bits", Long.valueOf(getAddressBits())));
		node.addProperty(new DTProperty("max-frame-size", Long.valueOf(getMaxFrameSize())));
		DTProperty dtpb = new DTProperty("local-mac-address");
		dtpb.addByteValues(getMacAddress(bi));
		node.addProperty(dtpb);
		
		for (Connection iconn : getConnections(SystemDataType.CONDUIT, false)) {
			if (iconn.getSlaveModule().getClassName().equalsIgnoreCase("altera_gmii_to_sgmii_converter")) {
				node.addProperty(new DTProperty("altr,gmii-to-sgmii-converter",
						new DTPropPHandleVal(iconn.getSlaveModule().getInstanceName())));
				node.addProperty(new DTProperty("altr,gmii_to_sgmii_converter",
						new DTPropPHandleVal(iconn.getSlaveModule().getInstanceName())));
				setPhyMode(PhyMode.SGMII);

			}
		}
		if (phymode != PhyMode.NONE) {
			node.addProperty(new DTProperty("phy-mode", getPhyModeString()));
		}
		return node;
	}
	protected int[] getMacAddress(BoardInfo bi)
	{
		return bi.getEthernetForChip(getInstanceName()).getMac();
	}
	protected int getAddressBits()
	{
		return 48;
	}
	protected int getMaxFrameSize()
	{
		return 1518;
	}
}
