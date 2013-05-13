/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2013 Walter Goossens <waltergoossens@home.nl>

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
package sopc2dts.lib.devicetree;

public class DTPropByteVal extends DTPropVal {
	int val;
	public DTPropByteVal(int v) {
		super(DTPropType.BYTE, "[ ", " ]", " ");
		val = v;
	}
	@Override
	protected byte[] getValueBytes() {
		byte[] res = new byte[1];
		res[0] = (byte)(val&0xFF);
		return res;
	}
	@Override
	public String toString() {
		return String.format("%02X", val&0xFF);
	}
}
