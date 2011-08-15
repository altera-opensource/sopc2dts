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

import java.io.Serializable;

public class FlashPartition implements Serializable {
	private static final long serialVersionUID = -4893851504958583588L;
	private String name;
	private int address;
	private int size;
	private boolean readonly = false;

	public FlashPartition() {
	}

	public FlashPartition(FlashPartition fp) {
		this.name = fp.name;
		this.address = fp.address;
		this.size = fp.size;
		this.readonly = fp.readonly;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAddress() {
		return address;
	}

	public void setAddress(int address) {
		this.address = address;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof FlashPartition))
			return false;
		
		FlashPartition fp = (FlashPartition)o;
		
		if(readonly != fp.isReadonly())
			return false;
		
		if(address != fp.getAddress())
			return false;
		
		if(size != fp.getSize())
			return false;
		
		return name.equals(fp.getName());
	}
}
