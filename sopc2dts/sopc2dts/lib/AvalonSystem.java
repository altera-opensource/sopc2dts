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

import java.io.File;
import java.util.Vector;

import sopc2dts.lib.components.BasicComponent;

public class AvalonSystem extends BasicElement {
	private static final long serialVersionUID = -4412823810569371574L;
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
	public File getSourceFile()
	{
		return sourceFile;
	}
	public Vector<BasicComponent> getSystemComponents() {
		return vSystemComponents;
	}
	public String getSystemName() {
		return systemName;
	}
	public void setVersion(String string) {
		versionStr = string;		
	}
	public void recheckComponents()
	{
		/*
		 * Now remove tristate (and other unneeded) bridges. If any.
		 * Also flatten hierarchical qsys designs.
		 */
		for(int i=0; i<vSystemComponents.size();)
		{
			int oldSize = vSystemComponents.size();
			BasicComponent c = vSystemComponents.get(i);
			c.removeFromSystemIfPossible(this);
			//Move back the number of components removed, or advance
			i+= (vSystemComponents.size() - oldSize) + 1;
			//Don't move back beyond the beginning.
			if(i<0) i=0;
		}
		for(BasicComponent comp : vSystemComponents)
		{
			SopcComponentLib.getInstance().finalCheckOnComponent(comp);
		}
	}
}
