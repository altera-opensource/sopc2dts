package sopc2dts.parsers.qsys;

import java.util.Vector;

import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.base.SCDQSys;

public class QSysSubSystem extends BasicComponent {
	protected Vector<BasicComponent> vSystemComponents = new Vector<BasicComponent>();

	public QSysSubSystem(String iName, String ver) {
		super(new SCDQSys(iName), iName, ver);
	}

	public void addModule(BasicComponent bc) {
		vSystemComponents.add(bc);
	}

	public BasicComponent getComponentByName(String name) {
		for(BasicComponent bc : vSystemComponents)
		{
			if(bc.getInstanceName().equalsIgnoreCase(name))
			{
				return bc;
			}
		}
		return null;
	}
}
