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
package sopc2dts.generators;

import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;

public class DTBCCharArray extends AbstractSopcGenerator {
	static final int ENTRIES_PER_LINE	= 12;
	private DTBGenerator dtbGen;
	public DTBCCharArray(AvalonSystem s) {
		super(s, true);
		dtbGen = new DTBGenerator(s);
	}

	@Override
	public String getTextOutput(BoardInfo bi) {
		byte[] dtb = dtbGen.getBinaryOutput(bi);
		String res = "unsigned char dtbData[] = {\n";
		for(int i=0; i<dtb.length; i++) {
			if(i%ENTRIES_PER_LINE == 0) {
				res += "\t";
			} else {
				res += " ";
			}
			res += String.format("0x%02X,", dtb[i]);
			if(i%ENTRIES_PER_LINE==(ENTRIES_PER_LINE-1))
			{
				res += "\n";
			}
		}
		if(dtb.length%ENTRIES_PER_LINE != 0)
		{
			res += "\n";
		}
		res += "};\n";
		return res;
	}
}
