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
package sopc2dts.lib.devicetree;

public abstract class DTProperty extends DTElement {
	public static final int OF_DT_PROP = 0x03;
	enum DTPropType { STRING, NUMBER, BYTE, BOOL };
	DTPropType type;
	int numValsPerRow = 0;

	protected DTProperty(String name, String label, String comment, DTPropType type)
	{
		super(name,label,comment);
		this.type = type;
	}
	public byte[] getBytes(DTBlob dtb)
	{
		byte[] valBytes = getValueBytes();
		byte[] buff = new byte[12+((valBytes.length +3)& ~3)];
		DTBlob.putU32(OF_DT_PROP, buff, 0);
		DTBlob.putU32(valBytes.length, buff, 4);
		DTBlob.putU32(dtb.registerString(name), buff, 8);
		if(valBytes.length>0)
		{
			System.arraycopy(valBytes, 0, buff, 12, valBytes.length);
		}
		return buff;
	}
	protected abstract byte[] getValueBytes();

	public abstract String toString(int indent);

	public String toString()
	{
		return toString(0);
	}
	public void setNumValuesPerRow(int n) 
	{
		numValsPerRow = n;
	}
}
