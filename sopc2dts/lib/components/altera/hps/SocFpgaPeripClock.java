/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2014 Walter Goossens <waltergoossens@home.nl>

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
package sopc2dts.lib.components.altera.hps;

import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTPropHexNumVal;
import sopc2dts.lib.devicetree.DTProperty;

public class SocFpgaPeripClock extends VirtualClockElement {
	
	Long fixedDivider;
	long[] divReg;
	/* Constructor for discovered pll */
	public SocFpgaPeripClock(String cName, String iName, String ver,
			SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}
	/* Constructor for virtual pll */
	public SocFpgaPeripClock(String cName, String iName, String ver, Long reg, Long div, long[]divreg, SopcComponentDescription scdPClk) {
		super(cName, iName, ver, scdPClk, reg);
		fixedDivider = div;
		divReg = divreg;
	}
	public DTNode toDTNode(BoardInfo bi, Connection conn) {
		DTNode node = super.toDTNode(bi, conn);
		Long reg = getRegOffset();
		if (reg != null) {
			node.addProperty(new DTProperty("reg", new DTPropHexNumVal(reg.longValue())), true);
		}
		if(fixedDivider!=null) {
			node.addProperty(new DTProperty("fixed-divider", fixedDivider.longValue()));
		}
		if (divReg != null) {
			DTProperty prop = new DTProperty("div-reg");
			prop.addHexValues(divReg);
			node.addProperty(prop);
		}
		return node;
	}
}
