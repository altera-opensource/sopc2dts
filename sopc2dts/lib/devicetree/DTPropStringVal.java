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

public class DTPropStringVal extends DTPropVal {
	public DTPropStringVal(String sVal) {
		super(DTPropType.STRING, "", "", ", ");
		value = sVal;
	}
	String value;
	@Override
	protected byte[] getValueBytes() {
		byte[] buff = new byte[value.length()+1];
		System.arraycopy(value.getBytes(), 0, buff, 0, value.length());
		buff[value.length()] = 0;
		return buff;
	}
	@Override
	public String toString() {
		return '"' + value + '"';
	}
}
