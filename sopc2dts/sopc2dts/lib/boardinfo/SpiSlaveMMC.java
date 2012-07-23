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
package sopc2dts.lib.boardinfo;

import java.util.Vector;

import org.xml.sax.Attributes;

import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTPropHexNumber;

public class SpiSlaveMMC extends SpiSlave {
	
	public SpiSlaveMMC(Attributes atts)
	{
		super(atts);
	}
	
	public SpiSlaveMMC(int reg) {
		this("mmc-slot",reg);
	}
	
	protected SpiSlaveMMC(String name, int reg) {
		super(name, reg, "mmc-spi-slot", 30000000); 
	}
	@Override
	public DTNode toDTNode(BoardInfo bi)
	{
		DTNode node = super.toDTNode(bi);
		Vector<Long> vals = new Vector<Long>();
		vals.add(3200L);
		vals.add(3400L);
		node.addProperty(new DTPropHexNumber("voltage-ranges", vals));
		return node;
	}
}
