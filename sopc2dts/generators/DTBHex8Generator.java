/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 - 2012 Walter Goossens <waltergoossens@home.nl>

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
import sopc2dts.lib.Bin2IHex;
import sopc2dts.lib.Bin2IHex.HexTypes;
import sopc2dts.lib.BoardInfo;

/** @brief Generate an intel-Hex8 file for a dtb
 * 
 * @author Walter Goossens
 *
 */
public class DTBHex8Generator extends DTBGenerator2 {
	public DTBHex8Generator(AvalonSystem s) {
		super(s);
		generateTextOutput = true;
	}

	@Override
	public String getTextOutput(BoardInfo bi) {
		return Bin2IHex.toHex(getBinaryOutput(bi), HexTypes.I8Hex);
	}

}
