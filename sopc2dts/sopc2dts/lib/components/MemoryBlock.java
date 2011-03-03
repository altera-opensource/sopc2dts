package sopc2dts.lib.components;

public class MemoryBlock {
	int base;
	int size;
	BasicComponent owner;
	public MemoryBlock(BasicComponent owner, int base, int size)
	{
		this.base = base;
		this.size = size;
		this.owner = owner;
	}
	public int getBase() {
		return base;
	}
	public void setStart(int base) {
		this.base = base;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public BasicComponent getModule() {
		return owner;
	}
	public String getModuleName() {
		return owner.getInstanceName();
	}
}
