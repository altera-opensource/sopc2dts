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
package sopc2dts.parsers.qsys;

import java.util.Vector;

import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.SopcComponentDescription;

public class QSysSubSystem extends BasicComponent {
	private static final long serialVersionUID = 1961793436374250055L;
	protected Vector<BasicComponent> vSystemComponents = new Vector<BasicComponent>();

	public QSysSubSystem(String iName, String ver) {
		super(new SopcComponentDescription(iName, "QSys", "ALTR", iName), iName, ver);
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
