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

public class DTPropPHandleVal extends DTPropVal {
	String label;
	long pHandle;
	
	public DTPropPHandleVal(String lab) {
		this(lab,0);
	}
	public DTPropPHandleVal(String lab, long ph) {
		super(DTPropType.PHANDLE, "< ", " >", " ");
		label = lab;
		pHandle = ph;
	}

	@Override
	protected byte[] getValueBytes() {
		byte[] buff = new byte[4];
		DTBlob.putU32(pHandle, buff, 0);
		return buff;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String l) {
		this.label = l;
	}
	public long getpHandle() {
		return pHandle;
	}
	public void setpHandle(long pHandle) {
		this.pHandle = pHandle;
	}
	@Override
	public String toString() {
		return '&' + label;
	}

}
