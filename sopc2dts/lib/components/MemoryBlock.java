/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 - 2012 Walter Goossens <waltergoossens@home.nl>

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
package sopc2dts.lib.components;

public class MemoryBlock {
	long base[];
	long size[];
	BasicComponent owner;
	public MemoryBlock(BasicComponent owner, long base[], long size[])
	{
		this.base = base;
		this.size = size;
		this.owner = owner;
	}
	public long[] getBase() {
		return base;
	}
	public void setStart(long[] base) {
		this.base = base;
	}
	public long[] getSize() {
		return size;
	}
	public void setSize(long[] size) {
		this.size = size;
	}
	public BasicComponent getModule() {
		return owner;
	}
	public String getModuleName() {
		return owner.getInstanceName();
	}
}
