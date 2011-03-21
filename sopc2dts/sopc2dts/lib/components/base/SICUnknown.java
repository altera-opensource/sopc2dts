package sopc2dts.lib.components.base;

import sopc2dts.lib.components.SopcComponentDescription;

public class SICUnknown extends SopcComponentDescription {
	public SICUnknown(String sopcClassName)
	{
		setClassName(sopcClassName);
		setDevice("unknown");
		setGroup("unknown");
		setVendor("unknown");
	}
}
