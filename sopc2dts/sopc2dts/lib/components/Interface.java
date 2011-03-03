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
	int interfaceValue;
	
	public Interface(String iName, SystemDataType dt, boolean master, BasicComponent owner) {
		this.setName(iName);
		this.type = dt;
		this.owner = owner;
		this.isMaster = master;
	}

	public int getInterfaceValue()
	{
		return interfaceValue;
	}
	public void setInterfaceValue(int size)
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
