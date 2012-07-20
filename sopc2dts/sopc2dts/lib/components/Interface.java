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
import java.util.Vector;

import sopc2dts.lib.Connection;
import sopc2dts.lib.BasicElement;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.components.base.SICBridge;

/** @brief An interface of a BasicComponent
 * 
 * This class represents an interface (port) of a module in an AvalonSystem. 
 * Interfaces are the points where a Connection is made to another 
 * BasicComponent. The connections are the basic types (defined in 
 * SystemDataType) used in SOPCBuilder and QSys.  
 * 
 * @see Connection
 * @see BasicComponent
 * @author Walter Goossens
 *
 */
public class Interface extends BasicElement {
	/** @brief The name of the Interface */
	private String name;
	/** @brief List of connections to/from this Interface
	 * 
	 * This is a list of connections made to/from this Interface. For most types
	 * of interfaces (RESET, CLOCK, IRQ, STREAMING) only one connection will
	 * exist but for the MEMORY_MAPPED type there can be many different 
	 * connections made to/from the same Interface
	 * 
	 * @note Only one type of Connection can be made to each Interface  and 
	 * it's type has to match the type of the Interface.
	 */
	private Vector<Connection> vConnections = new Vector<Connection>();
	/** @brief The BasicComponent this Interface belongs to */
	private BasicComponent owner;
	/** @brief The SystemDataType of this Interface */
	protected SystemDataType type = SystemDataType.CONDUIT;
	/** @brief Whether or not this is a master Interface */
	protected boolean isMaster;
	/** @brief The type specific value for this Interface
	 * 
	 * 
	 */
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
				if(conn.getSlaveModule() instanceof SICBridge)
				{
					/* Get bridged components as well. */
					Vector<MemoryBlock> vBridgedMap = new Vector<MemoryBlock>();
					for(Interface bridgeMaster : conn.getSlaveModule().getInterfaces(SystemDataType.MEMORY_MAPPED, true))
					{
						vBridgedMap.addAll(bridgeMaster.getMemoryMap());
					}
					for(MemoryBlock mb : vBridgedMap)
					{
						//Offset with bridges base.
						mb.base += conn.getConnValue();
					}
					vMemoryMap.addAll(vBridgedMap);
				} else {
					MemoryBlock mem = new MemoryBlock(conn.getSlaveModule(), conn.getConnValue(), conn.getSlaveInterface().getInterfaceValue());
					vMemoryMap.add(mem);
				}
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
