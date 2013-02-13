/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2012 Walter Goossens <waltergoossens@home.nl>

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

import java.util.Collections;
import java.util.Vector;

import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.boardinfo.BICI2C;
import sopc2dts.lib.boardinfo.I2CSlave;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTPropNumber;

public class SICI2CMaster extends BasicComponent {

	public SICI2CMaster(String cName, String iName, String ver, SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}
	public SICI2CMaster(BasicComponent comp) {
		super(comp);
	}
	@Override
	public DTNode toDTNode(BoardInfo bi, Connection conn)
	{
		DTNode node = super.toDTNode(bi, conn);
		BICI2C bi2c = bi.getI2CForChip(this.getInstanceName());
		Vector<I2CSlave> vSlaves = bi2c.getSlaves();
		if(!vSlaves.isEmpty()) {
			node.addProperty(new DTPropNumber("#address-cells", 1L));
			node.addProperty(new DTPropNumber("#size-cells", 0L));
			Collections.sort(vSlaves);
			for(I2CSlave s : vSlaves) {
				node.addChild(s.toDTNode(bi));
			}
		}
		return node;
	}
}
