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

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.base.SICUnknown;
import sopc2dts.parsers.qsys.QSysSystemLoader;

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
	public boolean addSystemComponent(BasicComponent comp) {
		return vSystemComponents.add(comp);
	}
	public BasicComponent getComponentByName(String name)
	{
		for(BasicComponent c : vSystemComponents)
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
		 * Try to find hierarchy
		 */
		for(int i=0; i<vSystemComponents.size();i++)
		{
			BasicComponent comp = vSystemComponents.get(i);
			if((comp.getScd() instanceof SICUnknown) && 
					(comp.getParamValByName("AUTO_GENERATION_ID")!=null))
			{
				if(removeHierarchicalWrapperComponent(comp))
				{
					//Restart loop after modifications...
					i=0;
				}
			}
		}
		/*
		 * Now remove tristate (and other unneeded) bridges. If any.
		 * Also flatten hierarchical qsys designs.
		 */
		for(int i=0; i<vSystemComponents.size();i++)
		{
			if(vSystemComponents.get(i).removeFromSystemIfPossible(this))
			{
				//Restart loop after modifications...
				i=0;
			}
		}
		for(BasicComponent comp : vSystemComponents)
		{
			SopcComponentLib.getInstance().finalCheckOnComponent(comp);
		}
	}
	private boolean removeHierarchicalWrapperComponent(BasicComponent comp)
	{
		Logger.logln("Found possible QSys subsytem " + comp.getInstanceName() + " of type " + comp.getClassName(), LogLevel.DEBUG);
		Logger.logln("Trying to find matching QSys file", LogLevel.DEBUG);
		File qsysFile = new File(sourceFile.getAbsoluteFile().getParent()+ File.separator + comp.getClassName() + ".qsys");
		if(qsysFile.exists())
		{
			Logger.logln("Using " + qsysFile.getAbsolutePath(), LogLevel.DEBUG);
			QSysSystemLoader qsl = new QSysSystemLoader();
			qsl.reloadSubSystem(qsysFile, this, comp);
			while(comp.getInterfaces().size()>0)
			{
				Interface intf = comp.getInterfaces().firstElement();
				String internalName = intf.getParamValByName("internal");
				BasicComponent internalComponent = getComponentByName(comp.getInstanceName() + '_' + internalName.split("\\.")[0]);
				Interface internalIntf = internalComponent.getInterfaceByName(internalName.split("\\.")[1]);
				if(internalIntf == null)
				{
					//Just move the interface inwards :)
					internalComponent.getInterfaces().add(intf);
					intf.setOwner(internalComponent);
				} else {
					while(intf.getConnections().size()>0)
					{
						intf.getConnections().firstElement().connect(internalIntf);
					}
				}
				comp.getInterfaces().remove(0);
			}
			vSystemComponents.remove(comp); 
			return true;
		} else {
			Logger.logln(qsysFile.getAbsolutePath() + " does not exist");
		}
		return false;
	}
	public boolean removeSystemComponent(BasicComponent comp) {
		return vSystemComponents.remove(comp);
	}
}
