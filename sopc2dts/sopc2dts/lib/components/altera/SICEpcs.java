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

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.base.SICFlash;

public class SICEpcs extends SICFlash {
	private static final long serialVersionUID = 8647857111806987880L;

	public SICEpcs(SopcComponentDescription scd, String iName, String version) {
		super(scd, iName, version);
	}
	@Override
	public String toDtsExtrasFirst(BoardInfo bi, int indentLevel, 
			Connection conn, Boolean endComponent)
	{
		return AbstractSopcGenerator.indent(indentLevel) + "#address-cells = <1>;\n"
				+ AbstractSopcGenerator.indent(indentLevel) + "#size-cells = <0>;\n";
	}
	@Override
	public String toDtsExtras(BoardInfo bi, int indentLevel, 
			Connection conn, Boolean endComponent)
	{
		String res = "\n" + AbstractSopcGenerator.indent(indentLevel++) + "m25p80@0 {\n"
					+ super.toDtsExtrasFirst(bi, indentLevel, conn, endComponent)
					+ AbstractSopcGenerator.indent(indentLevel) + "compatible = \"m25p80\";\n"
					+ AbstractSopcGenerator.indent(indentLevel) + "reg = <0>;\n"
					+ partitionsForDts(bi, indentLevel);
		
		res += AbstractSopcGenerator.indent(--indentLevel) + "};\n";
		return res;
	}

	@Override
	protected long getAddrFromConnection(Connection conn)
	{
		//Yes this is REALLY ugly. But it just might work :)
		int regOffset;
		try {
			regOffset = Integer.decode(getParamValByName("embeddedsw.CMacro.REGISTER_OFFSET"));
		} catch(Exception e)
		{
			regOffset = 0;
		}
		return (conn==null ? getAddr() : conn.getConnValue()) + regOffset;
	}
	@Override
	protected long getSizeFromInterface(Interface intf)
	{
		return 8;
	}

}
