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
import sopc2dts.lib.devicetree.DTNode;

public class DTSGenerator2 extends DTGenerator {

	public DTSGenerator2(AvalonSystem s) {
		super(s, true);
	}

	@Override
	public String getExtension() {
		return "dts";
	}
	@Override
	public String getTextOutput(BoardInfo bi) {
		String res = getSmallCopyRightNotice("devicetree")
					+ "/dts-v1/;\n";
		DTNode rootNode = getDTOutput(bi);
		res += rootNode.toString();
		return res;
	}

}
