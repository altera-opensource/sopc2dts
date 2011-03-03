package sopc2dts.lib;

import java.io.File;
import java.util.Vector;

import sopc2dts.lib.components.BasicComponent;

public class AvalonSystem extends BasicElement {
	public enum SystemDataType { MEMORY_MAPPED, STREAMING, INTERRUPT, CLOCK, 
		CUSTOM_INSTRUCTION, RESET, CONDUIT };
	protected String versionStr = "";
	protected File sourceFile;
	private String systemName;
	protected Vector<BasicComponent> vSystemComponents = new Vector<BasicComponent>();

	public AvalonSystem(String name, String version, File f) {
		versionStr = version;
		sourceFile = f;
		systemName = name;
	}
	public BasicComponent getComponentByName(String name)
	{
		for(BasicComponent c : getSystemComponents())
		{
			if(c.getInstanceName().equalsIgnoreCase(name))
			{
				return c;
			}
		}
		return null;
	}

	public Vector<BasicComponent> getMasterComponents() {
		Vector<BasicComponent> vRes = new Vector<BasicComponent>();
		for(BasicComponent comp : vSystemComponents)
		{
			if(comp.hasMemoryMaster())
			{
				vRes.add(comp);
			}
		}
		return vRes;
	}
	public Vector<BasicComponent> getSystemComponents() {
		return vSystemComponents;
	}
	public String getSystemName() {
		return systemName;
	}
}
