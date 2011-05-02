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
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;

public class TSEMonolithic extends SICTrippleSpeedEthernet {
	SICSgdma rx_dma, tx_dma;
	BasicComponent desc_mem;
	
	public TSEMonolithic(SopcComponentDescription scd, String iName,
			String version) {
		super(scd, iName, version);
	}
	protected void encapsulateSGDMA(SICSgdma dma, String name)
	{
		//CSR MM interface
		Interface intf = dma.getInterfaceByName("csr");
		dma.getInterfaces().remove(intf);
		intf.setName(name + "_csr");
		vInterfaces.add(intf);
		intf.setOwner(this);
		//IRQ
		intf = dma.getInterfaceByName("csr_irq");
		dma.getInterfaces().remove(intf);
		intf.setName(name + "_irq");
		vInterfaces.add(intf);
		intf.setOwner(this);
	}
	public void removeFromSystemIfPossible(AvalonSystem sys)
	{
		BasicComponent comp;
		if(rx_dma == null)
		{
			comp = getDMAEngineForIntf(getInterfaceByName("receive"));
			if((comp == null) || !(comp instanceof SICSgdma))
			{
				Logger.logln("TSEMonolithic: Failed to find SGDMA RX engine", LogLevel.WARNING);
				rx_dma = null;
			} else {
				rx_dma = (SICSgdma)comp;
				sys.getSystemComponents().remove(comp);
				encapsulateSGDMA(rx_dma, "rx");
			}
		}
		if(tx_dma == null)
		{
			comp = getDMAEngineForIntf(getInterfaceByName("transmit"));
			if((comp == null) || !(comp instanceof SICSgdma))
			{
				Logger.logln("TSEMonolithic: Failed to find SGDMA TX engine", LogLevel.WARNING);
				tx_dma = null;
			} else {
				tx_dma = (SICSgdma)comp;
				sys.getSystemComponents().remove(comp);
				encapsulateSGDMA(tx_dma, "tx");
			}
		}
		if(desc_mem == null)
		{
			if(rx_dma == null)
			{
				Logger.logln("TSEMonolithic: No RX-DMA engine. Cannot find descriptor memory");
			} else {
				Interface intfDescr = rx_dma.getInterfaceByName("descriptor_read");
				desc_mem = intfDescr.getConnections().get(0).getSlaveModule();
				sys.getSystemComponents().remove(desc_mem);
				Interface s1 = desc_mem.getInterfaceByName("s1");
				desc_mem.getInterfaces().remove(s1);
				s1.setOwner(this);
				vInterfaces.add(s1);
			}
		}
	}
}
