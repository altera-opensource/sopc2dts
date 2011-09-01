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
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;

public class TSEMonolithic extends SICTrippleSpeedEthernet {
	private static final long serialVersionUID = -599101440215652950L;
	SICSgdma rx_dma, tx_dma;
	BasicComponent desc_mem;
	
	public TSEMonolithic(String cName, String iName, String ver, SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
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
				desc_mem = findSlaveComponent(intfDescr, "memory", "onchipmem");
				if(desc_mem == null)
				{
					Logger.logln("Failed to find onchip descriptor memory. " +
							"Trying other memories", LogLevel.WARNING);
					desc_mem = findSlaveComponent(intfDescr, "memory", null);
				}
				if(desc_mem!=null)
				{
					sys.getSystemComponents().remove(desc_mem);
					Interface s1 = desc_mem.getInterfaceByName("s1");
					desc_mem.getInterfaces().remove(s1);
					s1.setOwner(this);
					vInterfaces.add(s1);
				} else {
					Logger.logln("Failed to find descriptor memory.", LogLevel.WARNING);
				}
			}
		}
	}
	BasicComponent findSlaveComponent(Interface intf, String group, String device)
	{
		BasicComponent res = null;
		for(Connection conn : intf.getConnections())
		{
			res = conn.getSlaveModule();
			if(res != null)
			{
				if(res.getScd().getGroup().equals("bridge"))
				{
					Logger.logln("Warning decriptor memory connected through a bridge. " +
							"I'll probably mess things up trying to guess what memory I'm connected to...",
							LogLevel.WARNING);
					res = findSlaveComponent(res.getInterfaceByName("m1"), group, device);
				} else {
					if(group!=null)
					{
						if(!res.getScd().getGroup().equals(group))
						{
							res = null;
						}
					}
					if((res!=null)&&(device!=null))
					{
						if(!res.getScd().getDevice().equals(device))
						{
							res = null;
						}
					}
				}
			}
			if(res!=null)
			{
				return res;
			}
		}
		return res;
	}
}
