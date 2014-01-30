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

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTProperty;

public class SocFpgaPllClock extends VirtualClockElement {
	static SopcComponentDescription scdPLL = new SopcComponentDescription("socfpga-pll", "socfpga-pll", "altr", "socfpga-pll-clock");

	/* Constructor for discovered pll */
	public SocFpgaPllClock(String cName, String iName, String ver,
			SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}
	/* Constructor for virtual pll */
	public SocFpgaPllClock(String cName, String iName, String ver) {
		super(cName, iName, ver, scdPLL);
		Interface mmIf = new Interface(MM_MASTER_NAME, SystemDataType.MEMORY_MAPPED, true,this);
		mmIf.setSecondaryWidth(0);
		mmIf.setInterfaceValue(new long[]{});
		vInterfaces.add(mmIf);
	}
	@Override
	public DTNode toDTNode(BoardInfo bi, Connection conn) {
		DTNode node = super.toDTNode(bi, conn);
		node.addProperty(new DTProperty("#address-cells", 1L));
		node.addProperty(new DTProperty("#size-cells", 0L));
		return node;
	}

	public void addClockOutput(SocFpgaPeripClock pClk) {
		/* Reg first */
		Interface master = getRegInterface(true);
		Interface slave = pClk.getRegInterface(false);
		if((master!=null)&&(slave!=null)) {
			Connection connMM = new Connection(master, slave, true);
			connMM.setConnValue(new long[]{pClk.getRegOffset()});
			/* Then clocks */
			pClk.addClockInput(getClockInterface(true));
		} else {
			Logger.logln(this, "can't find interface to connect this register to", LogLevel.WARNING);
		}
	}
}
