package sopc2dts.lib.components.base;

import sopc2dts.lib.components.SopcComponentDescription;

public class SCDQSys extends SopcComponentDescription {
	public SCDQSys(String cName)
	{
		setClassName(cName);
		setDevice(cName);
		setGroup("QSys");
		setVendor("ALTR");
	}
}
