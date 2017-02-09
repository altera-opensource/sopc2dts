/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 - 2014 Walter Goossens <waltergoossens@home.nl>

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

import java.util.Vector;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.Parameter;
import sopc2dts.lib.boardinfo.BICEthernet;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTProperty;

public class TSEMonolithic extends SICTrippleSpeedEthernet {
	SopcComponentDescription scdMSGDMA;
	enum TSEDmaType { UNKNOWN, SGDMA, mSGDMA, COMPOSED_mSGDMA };
	BasicComponent rx_dma, tx_dma;
	BasicComponent desc_mem;
	TSEDmaType dmaType = TSEDmaType.UNKNOWN;
	
	public TSEMonolithic(String cName, String iName, String ver, SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
		scdMSGDMA = new SopcComponentDescription(scd.getClassNames()[0], scd.getGroup(), "altr", "tse-msgdma");
		scdMSGDMA.addCompatibleVersion("1.0");
	}
	private DTNode toSGDMANode(DTNode node, BICEthernet be)
	{
		if(be.getMiiID()==null)
		{
			//Always needed for this driver! (atm)
			node.addProperty(new DTProperty("ALTR,mii-id", 0L));
		} else {
			node.addProperty(new DTProperty("ALTR,mii-id", Long.valueOf(be.getMiiID())));
		}

		return node;
	}

	private void doFifoDepth(String fifo, int fifo_width)
	{
		Parameter param = getParamByName(EMBSW_DTS_PARAMS+"ALTR,"+fifo);
		if (param != null) {
			Logger.logln(fifo +" is "+param.getValue() + " "+fifo_width, LogLevel.DEBUG);

			String val = Integer.toString((Integer.parseInt(param.getValue()) * fifo_width));
			addParam(new Parameter(EMBSW_DTS_PARAMS+fifo, val, param.getType()));
		}	
	}
	@Override 
	public DTNode toDTNode(BoardInfo bi, Connection conn)
	{
		int fifo_width = 1;
		Parameter param = getParamByName(EMBSW_CMACRO+".FIFO_WIDTH");
		if (param != null) {
			fifo_width = Integer.parseInt(param.getValue())/8;
		}
		
		doFifoDepth("rx-fifo-depth", fifo_width);
		doFifoDepth("tx-fifo-depth", fifo_width);

		DTNode node = super.toDTNode(bi, conn);		
		
		param = getParamByName("enable_sup_addr");
		if ((param != null) && (param.getValueAsBoolean())) {
			node.addProperty(new DTProperty("altr,has-supplementary-unicast"));	
			node.addProperty(new DTProperty("altr,enable-sup-addr",1));
		} else {
			node.addProperty(new DTProperty("altr,enable-sup-addr",0));
		}
		
		param = getParamByName("ena_hash");
		if ((param != null) && (param.getValueAsBoolean())) {
			node.addProperty(new DTProperty("altr,has-hash-multicast-filter"));	
			node.addProperty(new DTProperty("altr,enable-hash",1));
		} else {
			node.addProperty(new DTProperty("altr,enable-hash",0));
		}
		

		BICEthernet be = bi.getEthernetForChip(getInstanceName());
		
		if(be.getPhyID()!=null)
		{
			node.addProperty(new DTProperty("phy-addr", Long.valueOf(be.getPhyID())));
			/* the following binding is supported by old sgdma driver and v1 of msgdma */
			node.addProperty(new DTProperty("ALTR,phy-addr", Long.valueOf(be.getPhyID())));
		}
		
		if (dmaType == TSEDmaType.SGDMA) {
			toSGDMANode(node, be);
		}
		DTNode mdio_node = this.getMdioNode(be);
		if(mdio_node != null) {
			node.addChild(mdio_node);
		}
		
		return node;
	}
	protected boolean encapsulateDMAEngine(AvalonSystem sys, BasicComponent dma, String name) {
		boolean changed = false;
		if(dma!=null) {
			if(dma instanceof SICSgdma) {
				changed = encapsulateSGDMA(sys,(SICSgdma)dma, name);
			} else if (dmaType == TSEDmaType.COMPOSED_mSGDMA) {
				changed = encapsulateSharedMSGDMA(sys, dma,name, "response");
			} else {
				changed = encapsulateMSGDMA(sys, dma,name);
			}

			if(changed) {
				sys.removeSystemComponent(dma);
			}
		}
		return changed;
	}
	protected boolean encapsulateSGDMA(AvalonSystem sys, SICSgdma dma, String name)
	{
		//CSR MM interface
		Interface intf = dma.getInterfaceByName("csr");
		dma.removeInterface(intf);
		intf.setName(name + "_csr");
		vInterfaces.add(intf);
		intf.setOwner(this);
		//IRQ
		intf = dma.getInterfaceByName("csr_irq");
		dma.removeInterface(intf);
		intf.setName(name + "_irq");
		vInterfaces.add(intf);
		intf.setOwner(this);
		return true;
	}
	protected boolean encapsulateSharedMSGDMA(AvalonSystem sys, BasicComponent dispatcher, String name, String response)
	{
		//CSR MM interface
		Interface intf = dispatcher.getInterfaceByName("CSR");
		dispatcher.removeInterface(intf);
		intf.setName(name + "_csr");
		vInterfaces.add(intf);
		intf.setOwner(this);

		//Descriptor_Slave MM interface
		intf = dispatcher.getInterfaceByName("Descriptor_Slave");
		dispatcher.removeInterface(intf);
		intf.setName(name + "_desc");
		vInterfaces.add(intf);
		intf.setOwner(this);
		//IRQ
		intf = dispatcher.getInterfaceByName("csr_irq");
		dispatcher.removeInterface(intf);
		intf.setName(name + "_irq");
		vInterfaces.add(intf);
		intf.setOwner(this);

		intf = dispatcher.getInterfaceByName(response);
		if (intf != null) {
			dispatcher.removeInterface(intf);
			intf.setName("rx_resp");
			vInterfaces.add(intf);
			intf.setOwner(this);
		}
		return true;
	}
	protected boolean encapsulateMSGDMA(AvalonSystem sys, BasicComponent dma, String name)
	{
		boolean changed = false;
		/* mSGDMA consists of a reader/write (the one we've already found)
		 * and a dispatcher we're going to look for next
		 */
		Interface cmdSink = dma.getInterfaceByName("Command_Sink");
		BasicComponent dispatcher = null;
		if(cmdSink != null) {
			dispatcher = getDMAEngineForIntf(cmdSink);
		}
		if(dispatcher == null) {
			Logger.logln(this, "Failed to find dispatcher connected to " + dma.getInstanceName(), LogLevel.WARNING);
		} else if(sys.getComponentByName(dispatcher.getInstanceName()) == null) {
			Logger.logln(this, "mSGDMA dispatcher " + dispatcher.getInstanceName() + " seems to be in use. TSE needs twe separate engines for rx and tx", LogLevel.WARNING);
		} else if (dispatcher.getClassName().equalsIgnoreCase("modular_sgdma_dispatcher")) {
			Logger.logln(this, "Found dispatcher " + dispatcher.getInstanceName() + " connected to " + dma.getInstanceName(), LogLevel.INFO);

			encapsulateSharedMSGDMA(sys, dispatcher, name, "Response_Slave");
			
			sys.removeSystemComponent(dispatcher); /* dma will be removed by caller */

			changed = true;
		} else {
			Logger.logln(this, "Found unsupported dispatcher" + dispatcher.getInstanceName() + " (class: " + dispatcher.getClassName() + " connected to " + dma.getInstanceName(), LogLevel.WARNING);			
		}
		return changed;
	}
	protected BasicComponent findDMAEngine(boolean receiver)
	{
		BasicComponent dma = null;
		String sRxTx = (receiver ? "RX" : "TX");
		Interface intf = getInterfaceByName((receiver ? "receive" : "transmit"));
		if(intf==null)
		{
			intf = getInterfaceByName((receiver ? "receive_0" : "transmit_0"));
		}
		if(intf==null)
		{
			intf = getInterfaces(SystemDataType.STREAMING, receiver).firstElement();
			Logger.logln(this, " TSE does not have a port named '" + (receiver ? "receive" : "transmit") +"'." +
					" Randomly trying first streaming " + sRxTx + " port (" + intf.getName() + ')', LogLevel.WARNING);
		}
		BasicComponent comp = getDMAEngineForIntf(intf);
		if((comp == null))
		{
			Logger.logln(this,"Failed to find SGDMA " + sRxTx + " engine", LogLevel.WARNING);
			rx_dma = null;
			
		} else if((comp.getClassName().equalsIgnoreCase("dma_write_master") || 
				comp.getClassName().equalsIgnoreCase("dma_read_master")) 
				&& (dmaType != TSEDmaType.SGDMA))
		{
			dmaType = TSEDmaType.mSGDMA;
			dma = comp;
			Logger.logln(this, "Found " + sRxTx + " mSGDMA engine", LogLevel.INFO);
		} else if (comp.getClassName().equalsIgnoreCase("altera_msgdma")
					&& (dmaType != TSEDmaType.mSGDMA) && (dmaType != TSEDmaType.SGDMA)) {
			dmaType = TSEDmaType.COMPOSED_mSGDMA;
			dma = comp;
			Logger.logln(this, "Found " + sRxTx + " Composed mSGDMA engine", LogLevel.INFO);
		} else if((comp instanceof SICSgdma) && (dmaType != TSEDmaType.mSGDMA) && (dmaType != TSEDmaType.COMPOSED_mSGDMA))
		{
			dmaType = TSEDmaType.SGDMA;
			dma = comp;
			Logger.logln(this, "Found " + sRxTx + " SGDMA engine", LogLevel.INFO);
		} else {
			Logger.logln(this,"Failed to find (m)SGDMA " + sRxTx + " engine", LogLevel.WARNING);
			Logger.logln(this, "Found " + comp.getInstanceName() + " of class " + comp.getClassName() + " instead.", LogLevel.DEBUG);
		}
		return dma;
	}
	@Override
	public SopcComponentDescription getScd() {
		if ((dmaType == TSEDmaType.mSGDMA) || (dmaType == TSEDmaType.COMPOSED_mSGDMA)) {
			return scdMSGDMA;
		} else {
			return super.getScd();
		}
	}
	public boolean removeFromSystemIfPossible(AvalonSystem sys)
	{
		boolean bChanged = false;

		if(rx_dma == null)
		{
			rx_dma = findDMAEngine(true);
			bChanged |= encapsulateDMAEngine(sys,rx_dma, "rx");
		}
		if(tx_dma == null)
		{
			tx_dma = findDMAEngine(false);
			bChanged |= encapsulateDMAEngine(sys, tx_dma, "tx");
		}
		if((desc_mem == null)&&(dmaType == TSEDmaType.SGDMA))
		{
			if(rx_dma == null)
			{
				Logger.logln(this, "No RX-DMA engine. Cannot find descriptor memory", LogLevel.WARNING);
			} else {
				Interface intfDescr = rx_dma.getInterfaceByName("descriptor_read");
				desc_mem = findSlaveComponent(intfDescr, "memory", "onchipmem");
				if(desc_mem == null)
				{
					Logger.logln(this, "Failed to find onchip descriptor memory. " +
							"Trying other memories", LogLevel.WARNING);
					desc_mem = findSlaveComponent(intfDescr, "memory", null);
				}
				if(desc_mem!=null)
				{
					sys.removeSystemComponent(desc_mem);
					bChanged = true;
					Interface slave = desc_mem.getInterfaceByName("s1");
					desc_mem.removeInterface(slave);
					slave.setOwner(this);
					vInterfaces.add(slave);
					/* Steal both interface from a dualport ram */
					slave = desc_mem.getInterfaceByName("s2");
					if(slave!=null) {
						desc_mem.removeInterface(slave);
						slave.setOwner(this);
						vInterfaces.add(slave);
					}
				} else {
					Logger.logln(this, "Failed to find descriptor memory.", LogLevel.WARNING);
				}
			}
		}
		return bChanged;
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
					Logger.logln("Warning descriptor memory connected through a bridge. " +
							"I'll probably mess things up trying to guess what memory I'm connected to...",
							LogLevel.WARNING);
					Vector<Interface> vIntf = res.getInterfaces(SystemDataType.MEMORY_MAPPED, true);
					res = null;
					for(int i=0; (i<vIntf.size()) && (res==null); i++)
					{
						res = findSlaveComponent(vIntf.get(i), group, device);
					}
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
