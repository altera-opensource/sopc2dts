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

import sopc2dts.generators.AbstractSopcGenerator;

public class DTPropPHandle extends DTProperty {
	String value;
	long pHandle;
	public DTPropPHandle(String name, String value) {
		this(name,value,null,null);
	}
	public DTPropPHandle(String name, String value, String label) {
		this(name,value,label,null);
	}
	public DTPropPHandle(String name, String value, String label, String comment) {
		super(name, label, comment, DTPropType.STRING);
		this.value = value;
	}

	@Override
	public String toString(int indent) {
		return AbstractSopcGenerator.indent(indent) + (label != null ? label + ": " : "" ) 
		+ name + " = < &" + value + " >" + (comment != null ? ";\t/* " + comment + "*/\n": ";\n");
	}
	@Override
	protected byte[] getValueBytes() {
		byte[] buff = new byte[4];
		DTBlob.putU32(pHandle, buff, 0);
		return buff;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public long getpHandle() {
		return pHandle;
	}
	public void setpHandle(long pHandle) {
		this.pHandle = pHandle;
	}

}
