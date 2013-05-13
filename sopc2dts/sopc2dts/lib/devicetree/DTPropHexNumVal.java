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

public class DTPropHexNumVal extends DTPropNumVal {

	public DTPropHexNumVal(long v) {
		super(v);
	}

	public String toString()
	{
		if(val<0)
		{
			return String.format("0x%08X", val+0x100000000L);			
/*		} else if(val<0x100)
		{
			return String.format("0x%02X", val);
		} else if(val<0x10000)
		{
			return String.format("0x%04X", val);
*/		} else {
			return String.format("0x%08X", val);
		}
	}
}
