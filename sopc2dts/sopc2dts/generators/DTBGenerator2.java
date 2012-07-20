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
package sopc2dts.generators;

import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.devicetree.DTBlob;
import sopc2dts.lib.devicetree.DTNode;

/** @brief Generator for DTB files
 * 
 * This generator extends DTGenerator and uses the tree of DTElement's generated
 * by DTGenerator to create a dtb file directly without the need of an external
 * dtc.
 * 
 * @note This generator uses it's own dtb creation process, not the "official"
 * dtc. When in doubt, generate a DTS using DTSGenerator2 or even DTSGenerator,
 * pass that to dtc and compare the results.
 * 
 * @author Walter Goossens
 *
 */
public class DTBGenerator2 extends DTGenerator {

	/** @brief Constructor for the DTBGenerator2
	 * 
	 * @param s The AvalonSystem to generate for
	 */
	public DTBGenerator2(AvalonSystem s) {
		super(s, false);
	}

	@Override
	public String getTextOutput(BoardInfo bi) {
		return null;
	}

	@Override
	public byte[] getBinaryOutput(BoardInfo bi)
	{
		DTBlob dtb = new DTBlob();
		DTNode root = getDTOutput(bi);
		root.setName("");
		dtb.setRootNode(root);
		return dtb.getBytes();
	}

}
