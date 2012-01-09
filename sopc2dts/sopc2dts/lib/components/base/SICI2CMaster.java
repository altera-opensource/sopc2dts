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
package sopc2dts.lib.components.base;

import java.util.Arrays;
import java.util.HashMap;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTPropHexNumber;
import sopc2dts.lib.devicetree.DTPropNumber;
import sopc2dts.lib.devicetree.DTPropString;

public class SICI2CMaster extends BasicComponent {
	private static final long serialVersionUID = 8745845145232836596L;

	public SICI2CMaster(String cName, String iName, String ver, SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}
	@Override
	public DTNode toDTNode(BoardInfo bi, Connection conn)
	{
		DTNode node = super.toDTNode(bi, conn);
		HashMap<Integer, String> mI2C = bi.getI2CChipsForMaster(this.getInstanceName());
		if(mI2C != null)
		{
			if(!mI2C.isEmpty())
			{
				node.addProperty(new DTPropNumber("#address-cells", 1L));
				node.addProperty(new DTPropNumber("#size-cells", 0L));
				Integer[] keys = new Integer[mI2C.size()];
				keys = mI2C.keySet().toArray(keys);
				Arrays.sort(keys);
				for(Integer key : keys)
				{
					DTNode slave = new DTNode(mI2C.get(key) + '@' + Integer.toHexString(key));
					slave.addProperty(new DTPropString("compatible", mI2C.get(key)));
					slave.addProperty(new DTPropHexNumber("reg", Long.valueOf(key)));
					node.addChild(slave);
				}
			}
		}

		return node;
	}
	@Override
	public String toDtsExtras(BoardInfo bi, int indentLevel, Connection conn, Boolean endComponent)
	{
		String res = "";
		HashMap<Integer, String> mI2C = bi.getI2CChipsForMaster(this.getInstanceName());
		if(mI2C != null)
		{
			if(!mI2C.isEmpty())
			{
				res += AbstractSopcGenerator.indent(indentLevel) + "#address-cells = <1>;\n" +
					AbstractSopcGenerator.indent(indentLevel) + "#size-cells = <0>;\n";
				Integer[] keys = new Integer[mI2C.size()];
				keys = mI2C.keySet().toArray(keys);
				Arrays.sort(keys);
				for(Integer key : keys)
				{
					res += AbstractSopcGenerator.indent(indentLevel++) + mI2C.get(key) + '@' + Integer.toHexString(key) + " {\n" +
							AbstractSopcGenerator.indent(indentLevel) + "compatible = \"" + mI2C.get(key) + "\";\n" +
							AbstractSopcGenerator.indent(indentLevel) + "reg = <0x" + Integer.toHexString(key) + ">;\n" +
							AbstractSopcGenerator.indent(--indentLevel) + "};\n";
				}
			}
		}
		return res;
	}

}
