/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2013 Walter Goossens <waltergoossens@home.nl>

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

import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.Parameter;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTPropNumber;

public class VIPFrameBuffer extends BasicComponent {
	SICSgdma dmaEngine = null;
	
	public VIPFrameBuffer(String cName, String iName, String ver, SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}
	protected void encapsulateSGDMA(SICSgdma dma, AvalonSystem sys)
	{
		sys.removeSystemComponent(dma);
		//CSR MM interface
		Interface intf = dma.getInterfaceByName("csr");
		dma.removeInterface(intf);
		vInterfaces.add(intf);
		intf.setOwner(this);
		//IRQ
		intf = dma.getInterfaceByName("csr_irq");
		dma.removeInterface(intf);
		vInterfaces.add(intf);
		intf.setOwner(this);
	}
	public boolean removeFromSystemIfPossible(AvalonSystem sys)
	{
		Interface intf = getInterfaceByName("in");
		for(Connection conn : intf.getConnections()) {
			if((dmaEngine == null) && (conn.getMasterModule() instanceof SICSgdma)) {
				dmaEngine = (SICSgdma)conn.getMasterModule();
				encapsulateSGDMA(dmaEngine, sys);
				return true;
			}
		}
		return false;
	}
	@Override 
	public DTNode toDTNode(BoardInfo bi, Connection conn)
	{
		DTNode node = super.toDTNode(bi, conn);
		Long bpp = 32L;
		Parameter symbolWidth = getParamByName("DATA_STREAM_BIT_WIDTH");
		Parameter numSymbols = getParamByName("BEATS_PER_PIXEL");
		if((symbolWidth!=null) && (numSymbols!=null)) {
			bpp = Long.decode(symbolWidth.getValue()) * Long.decode(numSymbols.getValue());
		}
		node.addProperty(new DTPropNumber("bpp", bpp));
		return node;
	}
}
