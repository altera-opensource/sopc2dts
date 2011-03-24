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
package sopc2dts.lib;

import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;


public class Connection extends BasicElement {
	protected Interface masterInterface;
	protected Interface slaveInterface;	
	protected SystemDataType type = SystemDataType.CONDUIT;
	protected long connValue;
	
	public Connection(Interface master, Interface slave, SystemDataType t)
	{
		type = t;
		masterInterface = master;
		slaveInterface = slave;
	}
	
	public Connection(Connection org) {
		type = org.type;
		masterInterface = org.masterInterface;
		slaveInterface = org.slaveInterface;
		connValue = org.connValue;
	}

	public long getConnValue()
	{
		return connValue;
	}
	public void setConnValue(long val)
	{
		connValue = val;
	}

	public Interface getMasterInterface() {
		return masterInterface;
	}
	public BasicComponent getMasterModule() {
		if(masterInterface!=null)
		{
			return masterInterface.getOwner();
		} else {
			return null;
		}
	}
	public void setMasterInterface(Interface masterInterface) {
		this.masterInterface = masterInterface;
	}

	public Interface getSlaveInterface() {
		return slaveInterface;
	}
	
	public BasicComponent getSlaveModule() {
		if(slaveInterface!=null)
		{
			return slaveInterface.getOwner();
		} else {
			return null;
		}
	}

	public void setSlaveInterface(Interface slaveInterface) {
		this.slaveInterface = slaveInterface;
	}
	public SystemDataType getType()
	{
		return type;
	}
}
