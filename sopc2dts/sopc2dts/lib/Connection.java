package sopc2dts.lib;

import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;


public class Connection extends BasicElement {
	protected Interface masterInterface;
	protected Interface slaveInterface;	
	protected SystemDataType type = SystemDataType.CONDUIT;
	protected int connValue;
	
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

	public int getConnValue()
	{
		return connValue;
	}
	public void setConnValue(int val)
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
