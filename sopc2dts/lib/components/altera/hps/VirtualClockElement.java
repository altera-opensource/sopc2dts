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

import java.util.Vector;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTProperty;

public abstract class VirtualClockElement extends BasicComponent{
	protected static final String CLOCK_INPUT_NAME = "virtual-clk-input";
	protected static final String CLOCK_OUTPUT_NAME = "virtual-clk-output";
	protected static final String MM_SLAVE_NAME = "virtual-reg";
	protected static final String MM_MASTER_NAME = "virtual-mm-master";
	Long regOffset = null;
	
	protected VirtualClockElement(BasicComponent bc) {
		super(bc);
	}

	public VirtualClockElement(String cName, String iName, String ver,
			SopcComponentDescription scd) {
		this(cName,iName,ver,scd,0L);
	}

	private void init() {
		Interface mmIf = new Interface(MM_SLAVE_NAME, SystemDataType.MEMORY_MAPPED, false,this);
		mmIf.setSecondaryWidth(0);
		mmIf.setInterfaceValue(new long[]{});
		vInterfaces.add(mmIf);
		Interface clkIf = new Interface(CLOCK_OUTPUT_NAME, SystemDataType.CLOCK, true,this);
		vInterfaces.add(clkIf);
	}
	public VirtualClockElement(String cName, String iName, String ver,
			SopcComponentDescription scd, long reg) {
		super(cName, iName, ver, scd);
		init();
		regOffset = new Long(reg);
	}
	public VirtualClockElement(String cName, String iName, String ver,
		SopcComponentDescription scd, Long reg) {
		super(cName, iName, ver, scd);
		init();
		regOffset = reg;
	}
	protected Interface getClockInterface(boolean isMaster) {
		Interface clkIf = getInterfaceByName((isMaster? CLOCK_OUTPUT_NAME : CLOCK_INPUT_NAME));
		if(clkIf == null) {
			/* Use first available input */
			Vector<Interface> vClkInp = getInterfaces(SystemDataType.CLOCK, isMaster);
			if(!vClkInp.isEmpty()) {
				clkIf = vClkInp.firstElement();
			}
		}
		return clkIf;		
	}
	protected Interface getRegInterface(boolean isMaster) {
		Interface mmIf = getInterfaceByName((isMaster? MM_MASTER_NAME : MM_SLAVE_NAME));
		if(mmIf == null) {
			/* Use first available input */
			Vector<Interface> vMmIf = getInterfaces(SystemDataType.MEMORY_MAPPED, isMaster);
			if(!vMmIf.isEmpty()) {
				mmIf = vMmIf.firstElement();
			}
		}
		return mmIf;		
	}

	public Long getRegOffset() {
		return regOffset;
	}

	public void setRegOffset(long regOffset) {
		this.regOffset = new Long(regOffset);
	}
	
	public DTNode toDTNode(BoardInfo bi, Connection conn) {
		DTNode node = super.toDTNode(bi, conn);
		/* XXX I'd like the next line removed... */
		node.setName(getInstanceName());
		node.addProperty(new DTProperty("#clock-cells", 0L));
		Interface mmMaster = getRegInterface(true);
		if(mmMaster!=null) {
			for(Connection c2 : mmMaster.getConnections()) {
				node.addChild(c2.getSlaveModule().toDTNode(bi, c2));
			}
		}
		return node;
	}
	public void addClockInput(Interface clkMaster) {
		if(clkMaster!=null) {
			Interface clkIf = new Interface(clkMaster.getOwner().getInstanceName(), SystemDataType.CLOCK, false, this);
			addInterface(clkIf);
			@SuppressWarnings("unused")
			Connection conn = new Connection(clkMaster, clkIf, true);
		} else {
			Logger.logln(this, "Trying to add null interface...", LogLevel.DEBUG);
		}
	}
	@Override
	protected long[] getAddrFromConnection(Connection conn) {
		if (conn == null) {
			if (regOffset != null) {
				return new long[] {regOffset.longValue()};	
			} else {
				return new long[]{};
			}
		} else {
			return super.getAddrFromConnection(conn);
		}
	}

}
