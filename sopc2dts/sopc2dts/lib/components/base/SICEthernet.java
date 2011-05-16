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

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.BasicComponent;
/*
 * This class handles all default network stuff, subclasses can override 
 * defaults declared in this class, or just don't and keep 'm simple.
 * This is not really a special case (as are most classes in these packages) but
 * rather a convenience class to help the lazy people such as myself.
 */
public class SICEthernet extends BasicComponent {
	private static final long serialVersionUID = -5419573431636445762L;
	public SICEthernet(SopcComponentDescription scd, String iName, String version) {
		super(scd, iName, version);
	}

	@Override
	public String toDtsExtras(BoardInfo bi, int indentLevel, Connection conn, Boolean endComponent)
	{
		String res =  AbstractSopcGenerator.indent(indentLevel) + "address-bits = <" + getAddressBits() + ">;\n"
					+ AbstractSopcGenerator.indent(indentLevel) + "max-frame-size = <" + getMaxFrameSize() + ">;\n"
					+ AbstractSopcGenerator.indent(indentLevel) + "local-mac-address = [ ";
		int[] mac_address = getMacAddress();
		for(int i=0; i<mac_address.length; i++)
		{
			res += String.format("%02X ", mac_address[i]);
		}
		res += "];\n";
		return res;
	}
	protected int[] getMacAddress()
	{
		return new int[]{ 0, 0, 0, 0, 0, 0 };
	}
	protected int getAddressBits()
	{
		return 48;
	}
	protected int getMaxFrameSize()
	{
		return 1518;
	}
}
