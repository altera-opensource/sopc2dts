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

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.BasicComponent;

public class SICFlash extends BasicComponent {
	private static final long serialVersionUID = -8631549827116928831L;
	public SICFlash(String cName, String iName, String version, SopcComponentDescription scd) {
		super(cName, iName, version, scd);
	}

	@Override
	public String toDtsExtrasFirst(BoardInfo bi, int indentLevel, Connection conn, Boolean endComponent)
	{
		String res = "";
		Vector<FlashPartition> vPartitions = bi.getPartitionsForChip(this.getInstanceName());
		if(vPartitions != null)
		{
			if(vPartitions.size()>0)
			{
				res += AbstractSopcGenerator.indent(indentLevel) + "#address-cells = <1>;\n" +
					AbstractSopcGenerator.indent(indentLevel) + "#size-cells = <1>;\n";
			}
		}
		return res;
	}
	
	@Override
	public String toDtsExtras(BoardInfo bi, int indentLevel, Connection conn, Boolean endComponent)
	{
		int bankw = 2;
		try {
			String sdw = getParamValByName("dataWidth");
			if(sdw!=null)
			{
				bankw = Integer.decode(sdw)/8;
			}
		}catch(Exception e) {
			//Default to 16bit on failure
			bankw = 2;
		}
		String res = AbstractSopcGenerator.indent(indentLevel) + "bank-width = <"+bankw+">;\n" +
					AbstractSopcGenerator.indent(indentLevel) + "device-width = <1>;\n" +
					partitionsForDts(bi, indentLevel);

		return res;	
	}
	protected String partitionsForDts(BoardInfo bi, int indentLevel)
	{
		String res = "";
		Vector<FlashPartition> vPartitions = bi.getPartitionsForChip(this.getInstanceName());
		if(vPartitions != null)
		{
			for(FlashPartition part : vPartitions)
			{
				res += AbstractSopcGenerator.indent(indentLevel++) + part.getName() + '@' + Integer.toHexString(part.getAddress()) + " {\n" +
						AbstractSopcGenerator.indent(indentLevel) + String.format("reg = < 0x%08X 0x%08X >;\n", part.getAddress(),part.getSize());
				if(part.isReadonly())
				{
					res += AbstractSopcGenerator.indent(indentLevel) + "read-only;\n";
				}
				res += AbstractSopcGenerator.indent(--indentLevel) + "};\n";
			}
		}
		return res;
	}
}
