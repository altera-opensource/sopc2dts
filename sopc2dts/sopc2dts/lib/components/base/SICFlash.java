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
package sopc2dts.lib.components.base;

import java.util.Vector;

import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTPropBool;
import sopc2dts.lib.devicetree.DTPropHexNumber;
import sopc2dts.lib.devicetree.DTPropNumber;
import sopc2dts.lib.devicetree.DTProperty;

public class SICFlash extends BasicComponent {
	
	public SICFlash(BasicComponent bc)
	{
		super(bc);
	}
	public SICFlash(String cName, String iName, String version, SopcComponentDescription scd) {
		super(cName, iName, version, scd);
	}

	protected DTNode addPartitionsToDTNode(BoardInfo bi, DTNode node)
	{
		Vector<FlashPartition> vPartitions = bi.getPartitionsForChip(this.getInstanceName());
		if(vPartitions != null)
		{
			if(vPartitions.size()>0)
			{
				node.addProperty(new DTPropNumber("#address-cells", 1L));
				node.addProperty(new DTPropNumber("#size-cells", 1L));
			}
			for(FlashPartition part : vPartitions)
			{
				DTNode dtPart = new DTNode(part.getName() + '@' + Integer.toHexString(part.getAddress()));
				DTProperty p;
				Vector<Long> vReg = new Vector<Long>();
				vReg.add(Long.valueOf(part.getAddress()));
				vReg.add(Long.valueOf(part.getSize()));
				p = new DTPropHexNumber("reg", vReg);
				p.setNumValuesPerRow(2); //Addr + size
				dtPart.addProperty(p);
				if(part.isReadonly())
				{
					dtPart.addProperty(new DTPropBool("read-only"));
				}
				node.addChild(dtPart);
			}
		}
		return node;
	}
	@Override
	public DTNode toDTNode(BoardInfo bi, Connection conn)
	{
		//XXX Refactor to get cfi stuff to seperate class
		DTNode node = super.toDTNode(bi, conn);
		node.addProperty(new DTPropNumber("bank-width", Long.valueOf(getBankWidth())));
		node.addProperty(new DTPropNumber("device-width", 1L));
		node = addPartitionsToDTNode(bi, node);
		return node;
	}
	
	protected int getBankWidth()
	{
		int bankw = 2;
		try {
			String sdw = getParamValByName("dataWidth");
			//Try other param name if dataWidth did not exist
			if(sdw==null)
			{
				sdw = getParamValByName("TCM_DATA_W");
			}
			if(sdw!=null)
			{
				bankw = Integer.decode(sdw)/8;
			}
		}catch(Exception e) {
			//Default to 16bit on failure
			bankw = 2;
		}
		return bankw;
	}
}
