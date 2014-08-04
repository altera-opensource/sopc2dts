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
import sopc2dts.lib.components.altera.hps.ClockManager.ClockManagerGateClk;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTProperty;

public class SocFpgaGateClock extends VirtualClockElement {
	static SopcComponentDescription scdGClk = new SopcComponentDescription("socfpga-gate-clk", "socfpga-gate-clk", "altr", "socfpga-gate-clk");
	long[] gateReg;
	long[] divReg;
	Long fixedDivider;

	public SocFpgaGateClock(ClockManagerGateClk cmGClk, String version) {
		super(cmGClk.name, cmGClk.name,version,scdGClk);
		gateReg = null;
		divReg = cmGClk.divReg;
		fixedDivider = cmGClk.fixedDivider;
	}
	@Override
	public DTNode toDTNode(BoardInfo bi, Connection conn) {
		DTNode node = super.toDTNode(bi, conn);
		if(gateReg!=null) {
			DTProperty prop = new DTProperty("clk-gate");
			prop.addHexValues(gateReg);
			node.addProperty(prop);
		}
		if(divReg!=null) {
			DTProperty prop = new DTProperty("div-reg");
			prop.addHexValues(divReg);
			node.addProperty(prop);
		}
		if(fixedDivider!=null) {
			node.addProperty(new DTProperty("fixed-divider", fixedDivider.longValue()));
		}
		return node;
	}
	@Override
	protected String getAddrFromConnectionStr(Connection conn) {
		if(conn == null) {
			if(gateReg!=null) {
				return Long.toHexString(gateReg[0]) + "." + gateReg[1];
			} else if(divReg!=null) {
				return Long.toHexString(divReg[0]) + "." + divReg[1];				
			} else {
				return "00";
			}
		} else {
			return super.getAddrFromConnectionStr(conn);
		}
	}
}
