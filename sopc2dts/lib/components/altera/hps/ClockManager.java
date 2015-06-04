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
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTProperty;

public abstract class ClockManager extends BasicComponent {
	boolean virtualComponentsCreated = false;
	ClockManagerPll[] cmPLLs;
	ClockManagerGateGroup[] cmGGroups;
	ClockManagerPClk[] cmPeripheralClks;
	Interface virtualMaster;
	Vector<SocFpgaGateClock> vGateClocks = new Vector<SocFpgaGateClock>();
	Vector<SocFpgaPeripClock> vPeripheralClocks = new Vector<SocFpgaPeripClock>();	
	
	public ClockManager(BasicComponent bc) {
		super(bc);
		virtualMaster = new Interface(VirtualClockElement.MM_MASTER_NAME, SystemDataType.MEMORY_MAPPED, true, this);
		virtualMaster.setSecondaryWidth(0);
		this.addInterface(virtualMaster);
	}

	protected abstract String getFirstSupportedVersion();
	protected abstract boolean preRemovalChecks(AvalonSystem sys);
	protected abstract SocFpgaPllClock getSocFpgaPllClock(String cName, String iName, String ver);
	protected abstract SocFpgaPeripClock getSocFpgaPeripClock(String cName, String iName, String ver, Long reg, Long div, long[]divreg);
	protected abstract SocFpgaGateClock getSocFpgaGateClock(ClockManagerGateClk cmgClk, String ver);

	
	protected void transferClockConnections(Interface newClockMaster) {
		Interface oldClockMaster = getInterfaceByName(newClockMaster.getOwner().getInstanceName());
		if(oldClockMaster != null) {
			Logger.logln(this, "Transferring clock connections to " + newClockMaster.getOwner().getInstanceName(), LogLevel.DEBUG);
			while(!oldClockMaster.getConnections().isEmpty()) {
				Connection conn = oldClockMaster.getConnections().firstElement();
				conn.setMasterInterface(newClockMaster);
				oldClockMaster.getConnections().remove(conn);
				newClockMaster.getConnections().add(conn);
			}
			this.removeInterface(oldClockMaster);
		}
	}
	protected Interface getClockParentIntfByName(String clkParentName, AvalonSystem sys) {
		Interface clkPIntf = null;
		/* It might be one of our dedicated inputs */
		Interface clkInp = getInterfaceByName(clkParentName);
		if(clkInp!=null) {
			if(clkInp.isClockSlave() && !clkInp.getConnections().isEmpty()) {
				clkPIntf = clkInp.getConnections().firstElement().getMasterInterface();
			}
		}
		if(clkPIntf==null) {
			BasicComponent clkParent = sys.getComponentByName(clkParentName);
			if(clkParent!=null) {
				if(clkParent instanceof VirtualClockElement) {
					/* This is (for now) always the case */
					clkPIntf = ((VirtualClockElement)clkParent).getClockInterface(true);
				} else {
					/* Choose the first one. Fingers crossed. */
					Vector<Interface> vIntf = clkParent.getInterfaces(SystemDataType.CLOCK,true);
					if(!vIntf.isEmpty()) {
						clkPIntf = vIntf.firstElement();
					}
				}
			}
		}
		return clkPIntf;
	}
	@Override
	public synchronized boolean removeFromSystemIfPossible(AvalonSystem sys) {
		if(!virtualComponentsCreated) {
			virtualComponentsCreated = true;
			if(SopcComponentDescription.compareVersions(getFirstSupportedVersion(),this.version)<0) {
				Logger.logln(this, "ClockManager support class is not supported on version '" + version + "'. " +
						"First supported version is '" + getFirstSupportedVersion() + "'", LogLevel.INFO);
				return false;
			}
			if(!preRemovalChecks(sys)) {
				Logger.logln(this, "Pre-removal checks failed. Not removing ourselves.", LogLevel.WARNING);
				return false;
			}
			if(cmPLLs!=null) {
				for(ClockManagerPll pll : cmPLLs) {
					SocFpgaPllClock sfPll = getSocFpgaPllClock(pll.name, pll.name, null);
					Interface reg = sfPll.getRegInterface(false);
					Connection conn = new Connection(virtualMaster, reg, true);
					conn.setConnValue(new long[]{ pll.addr });
					sys.addSystemComponent(sfPll);
					for(String clkParentName : pll.clkParents) {
						sfPll.addClockInput(getClockParentIntfByName(clkParentName, sys));
					}
					for(ClockManagerPClk pClk : pll.pclks) {
						SocFpgaPeripClock sfpClk = 
							getSocFpgaPeripClock(pClk.name, pClk.name, null, pll.addr + pClk.addr,pClk.fixedDivider, pClk.divReg);
						sfPll.addClockOutput(sfpClk);
						if (pClk.clkParents != null) {
							for (String parent : pClk.clkParents) {
								sfpClk.addClockInput(getClockParentIntfByName(parent,sys));
							}
						}
						sys.addSystemComponent(sfpClk);
						transferClockConnections(sfpClk.getClockInterface(true));
					}
				}
			}
			if(cmPeripheralClks!=null){
				for (ClockManagerPClk fpClk : cmPeripheralClks) {
					SocFpgaPeripClock sfpClk = getSocFpgaPeripClock(fpClk.name, fpClk.name, null, fpClk.addr, fpClk.fixedDivider,fpClk.divReg);
					sys.addSystemComponent(sfpClk);
					transferClockConnections(sfpClk.getClockInterface(true));
					vPeripheralClocks.add(sfpClk);
					for(String parent : fpClk.clkParents) {
						sfpClk.addClockInput(getClockParentIntfByName(parent,sys));
					}
				}
			}
			if(cmGGroups!=null) {
				for(ClockManagerGateGroup grp : cmGGroups) {
					long gateReg = grp.reg;
					long gateRegBit = 0;
					for(ClockManagerGateClk cmgClk : grp.clks) {
						SocFpgaGateClock sfgClk = getSocFpgaGateClock(cmgClk,null);
						if(cmgClk.hasGate) {
							sfgClk.gateReg = new long[]{gateReg, gateRegBit};
							gateRegBit++;
						}
						for(String clkParentName : cmgClk.clkParents) {
							sfgClk.addClockInput(getClockParentIntfByName(clkParentName, sys));
						}
						transferClockConnections(sfgClk.getClockInterface(true));
						vGateClocks.add(sfgClk);
						sys.addSystemComponent(sfgClk);
					}
				}
			}

			return true;
		} else {
			return false;
		}
	}
	public DTNode toDTNode(BoardInfo bi, Connection conn) {
		DTNode node = super.toDTNode(bi, conn);
		if(!(virtualMaster.getConnections().isEmpty() && vGateClocks.isEmpty() && vPeripheralClocks.isEmpty())) {
			DTNode clockNode = new DTNode("clock_tree");
			clockNode.addProperty(new DTProperty("#size-cells",0L));
			clockNode.addProperty(new DTProperty("#address-cells",1L));
			for(Connection vconn : virtualMaster.getConnections()) {
				clockNode.addChild(vconn.getSlaveModule().toDTNode(bi, vconn));
			}
			node.addChild(clockNode);
			for(SocFpgaGateClock gClk : vGateClocks) {
				clockNode.addChild(gClk.toDTNode(bi, null));
			}
			for(SocFpgaPeripClock pClk : vPeripheralClocks) {
				clockNode.addChild(pClk.toDTNode(bi, null));
			}
		}
		return node;
	}

	protected class ClockManagerGateClk {
		String name;
		boolean hasGate;
		long[] divReg;
		Long fixedDivider;
		String[] clkParents;
		
		protected ClockManagerGateClk(String n, boolean gate, long[] div, Long fDiv, String[] parents)
		{
			name = n;
			hasGate = gate;
			divReg = div;
			fixedDivider = fDiv;
			clkParents = parents;
		}
	}
	
	protected class ClockManagerGateGroup {
		long reg;
		ClockManagerGateClk[] clks;
		
		ClockManagerGateGroup(long r, ClockManagerGateClk[] c) {
			reg = r;
			clks = c;
		}
	}
	protected class ClockManagerPClk {
		String name;
		Long addr;
		Long fixedDivider;
		long[] divReg;
		String[] clkParents;

		protected ClockManagerPClk(String n, long a, Long fixed, String[] parents) {
			this(n,a,fixed, null, parents);
		}

		protected ClockManagerPClk(String n, long a, Long fixed, long[] divreg, String[] parents) {
			name = n;
			addr = new Long(a);
			fixedDivider = fixed;
			divReg = divreg;
			clkParents = parents;
		}
		protected ClockManagerPClk(String n, Long a, Long fixed, long[] divreg, String[] parents) {
			name = n;
			addr = a;
			fixedDivider = fixed;
			divReg = divreg;
			clkParents = parents;
		}
	}
	protected class ClockManagerPll {
		String name;
		long addr;
		ClockManagerPClk[] pclks;
		String[] clkParents;

		protected ClockManagerPll(String n, long a, String[] parents, ClockManagerPClk[] pc) {
			name = n;
			addr = a;
			pclks = pc;
			clkParents = parents;
		}
	}
}
