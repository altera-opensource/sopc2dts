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
package sopc2dts.lib.components;
import java.util.Vector;

import sopc2dts.lib.Connection;
import sopc2dts.lib.BasicElement;
import sopc2dts.lib.AvalonSystem.SystemDataType;


public class Interface extends BasicElement {
	private String name;
	private Vector<Connection> vConnections = new Vector<Connection>();
	private BasicComponent owner;
	protected SystemDataType type = SystemDataType.CONDUIT;
	protected boolean isMaster;
	long interfaceValue;
	
	public Interface(String iName, SystemDataType dt, boolean master, BasicComponent owner) {
		this.setName(iName);
		this.type = dt;
		this.owner = owner;
		this.isMaster = master;
	}

	public long getInterfaceValue()
	{
		return interfaceValue;
	}
	public void setInterfaceValue(long size)
	{
		interfaceValue = size;
	}
	public boolean isMemory()
	{
		return (type == SystemDataType.MEMORY_MAPPED);
	}
	public boolean isMemoryMaster()
	{
		return isMemory() && isMaster;
	}
	public boolean isMemorySlave()
	{
		return isMemory() && !isMaster;
	}
	public boolean isClock()
	{
		return (type == SystemDataType.CLOCK);
	}
	public boolean isClockMaster()
	{
		return isClock() && isMaster;
	}
	public boolean isClockSlave()
	{
		return isClock() && !isMaster;
	}
	public boolean isIRQ()
	{
		return (type == SystemDataType.INTERRUPT);
	}
	public boolean isIRQMaster()
	{
		return isIRQ() && isMaster;
	}
	public boolean isIRQSlave()
	{
		return isIRQ() && !isMaster;
	}

	public Vector<Connection> getConnections() {
		return vConnections;
	}

	public void setConnections(Vector<Connection> connections) {
		this.vConnections = connections;
	}

	public Vector<MemoryBlock> getMemoryMap()
	{
		Vector<MemoryBlock> vMemoryMap = new Vector<MemoryBlock>();
		if(isMemoryMaster())
		{
			for(Connection conn : vConnections)
			{
				MemoryBlock mem = new MemoryBlock(conn.getSlaveModule(), conn.getConnValue(), conn.getSlaveInterface().getInterfaceValue());
				vMemoryMap.add(mem);
			}
		}
		return vMemoryMap;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public BasicComponent getOwner() {
		return owner;
	}

	public void setOwner(BasicComponent owner) {
		this.owner = owner;
	}

	public SystemDataType getType() {
		return type;
	}
	public boolean isMaster()
	{
		return isMaster;
	}
}
