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
package sopc2dts.lib.components.arm;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.Connection;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;

public class CortexA9GIC extends BasicComponent {
	public static final int GIC_IRQ_CLASS_SPI = 0;
	public static final int GIC_IRQ_CLASS_PPI = 1;
	public static final int GIC_IRQ_TYPE_RISING = 1;
	public static final int GIC_IRQ_TYPE_FALLING = 2;
	public static final int GIC_IRQ_TYPE_HIGH = 4;
	public static final int GIC_IRQ_TYPE_LOW = 8;
	private boolean irqsRenumbered = false;
	static SopcComponentDescription scdArmGIC = 
		new SopcComponentDescription("arm_gic", "intc", "arm", "cortex-a9-gic");

	public CortexA9GIC(String iName, String ver) {
		super("arm_gic", iName, ver, scdArmGIC);
	}
	static int getIrqClassFromIf(Interface intf) {
		int irqClass = (intf.getName().equalsIgnoreCase("arm_gic_ppi") ? 
				GIC_IRQ_CLASS_PPI : GIC_IRQ_CLASS_SPI);
		String irqClassStr= intf.getParamValByName("embeddedsw.dts.irq.rx_type");
		if(irqClassStr!=null) {
			if (irqClassStr.contentEquals("arm_gic_ppi")) {
				irqClass = GIC_IRQ_CLASS_PPI;
			} else if (irqClassStr.contentEquals("arm_gic_spi")) {
				irqClass = GIC_IRQ_CLASS_SPI;
			} else {
				Logger.logln("unknown embeddedsw.dts.irq.rx_type: " + irqClassStr,LogLevel.ERROR);
			}
		}
		return irqClass;
	}

	static int getIrqTypeFromIf(Interface intf) {
		int irqType = GIC_IRQ_TYPE_HIGH;
		String irqTypeStr = intf.getParamValByName("embeddedsw.dts.irq.tx_type");
		if (irqTypeStr!=null) {
			if(irqTypeStr.contentEquals("ACTIVE_HIGH")) {
				irqType = GIC_IRQ_TYPE_HIGH;
			} else if (irqTypeStr.contentEquals("ACTIVE_LOW")) {
				irqType = GIC_IRQ_TYPE_LOW;
			} else if (irqTypeStr.contentEquals("FALLING_EDGE")) {
				irqType = GIC_IRQ_TYPE_FALLING;
			} else if (irqTypeStr.contentEquals("RISING_EDGE")) {
				irqType = GIC_IRQ_TYPE_RISING;
			} else {
				Logger.logln("unknown embedded.dts.irq_tx_type: " + irqTypeStr,LogLevel.ERROR);
			}
		}
		String txMask = intf.getParamValByName("embeddedsw.dts.irq.tx_mask");
		if (txMask != null) {
			Logger.logln("got mask "+txMask, LogLevel.DEBUG);
			irqType |= Integer.decode(txMask);
		}
		return irqType;
	}

	@Override
	public Integer getPreferredPriWidthForIf(String iName, SystemDataType dt, boolean master) {
		if(master && dt.equals(SystemDataType.INTERRUPT)) {
			return new Integer(3);
		} else {
			return super.getPreferredPriWidthForIf(iName, dt, master);
		}
	}

	@Override
	public Integer getPreferredSecWidthForIf(String iName, SystemDataType dt, boolean master) {
		if(master && dt.equals(SystemDataType.INTERRUPT)) {
			return new Integer(0);
		} else {
			return super.getPreferredSecWidthForIf(iName, dt, master);
		}
	}

	@Override
	public boolean removeFromSystemIfPossible(AvalonSystem sys)
	{
		if(!irqsRenumbered) {
			for(Interface intf : vInterfaces) {
				if(intf.isIRQMaster())
				{
					int irqClass = getIrqClassFromIf(intf);
					for(Connection conn : intf.getConnections()) {
						int irqType = getIrqTypeFromIf(conn.getSlaveInterface());
						long[] connVal = conn.getConnValue();
						connVal[0] = irqClass;
						/* By default irqnr is stored in last value... */
						connVal[1] = connVal[2];
						connVal[2] = irqType;
					}				
				}
			}
			irqsRenumbered = true;
		}
		return false;
	}
}
