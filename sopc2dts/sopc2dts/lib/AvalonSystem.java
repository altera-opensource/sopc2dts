/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 - 2014 Walter Goossens <waltergoossens@home.nl>

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
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.base.SICBridge;
import sopc2dts.lib.components.base.SICUnknown;
import sopc2dts.parsers.qsys.QSysSystemLoader;

public class AvalonSystem extends BasicElement {
	public enum SystemDataType { MEMORY_MAPPED, STREAMING, INTERRUPT, CLOCK, 
		CUSTOM_INSTRUCTION, RESET, CONDUIT };
	protected String versionStr = "";
	protected int versionMajor = 0;
	protected int versionMinor = 0;
	protected File sourceFile;
	private String systemName;
	private static String sopc2dtsVer = "unknown";
	protected Vector<BasicComponent> vSystemComponents = new Vector<BasicComponent>();

	public AvalonSystem(String name, String version, File f) {
		sourceFile = f;
		systemName = name;
		setVersion(version);
	}
	public void setSopc2DtsVer(String ver)
	{
		sopc2dtsVer = ver;
	}
	public static String getSopc2DtsVer()
	{
		return sopc2dtsVer;
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

	public Vector<BasicComponent> getComponentsByClass(String cname)
	{
		Vector<BasicComponent> vRes = new Vector<BasicComponent>();
		for(BasicComponent comp : vSystemComponents)
		{
			if (comp.getClassName().equalsIgnoreCase(cname)) {
				vRes.add(comp);
			}
		}
		return vRes;
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
	public Vector<Connection> getConnectionPath(Interface start, BasicComponent end) {
		for(Interface endIf : end.getInterfaces(start.getType(), false)) {
			Vector<Connection> vPath = getConnectionPath(start, endIf);
			if(vPath.size()>0)
				return vPath;
		}
		return new Vector<Connection>();
	}
	public Vector<Connection> getConnectionPath(BasicComponent start, Interface end) {
		for(Interface startIf : start.getInterfaces(end.getType(), true)) {
			Vector<Connection> vPath = getConnectionPath(startIf, end);
			if(vPath.size()>0)
				return vPath;
		}
		return new Vector<Connection>();
	}
	public Vector<Connection> getConnectionPath(Interface start, Interface end) {
		Vector<Connection> vPath = new Vector<Connection>();
		for(Connection conn : end.getConnections()) {
			if(conn.getMasterInterface().equals(start)) {
				vPath.add(conn);
				return vPath;
			}
		}
		for(Connection conn : end.getConnections()) {
			if(conn.getMasterModule() instanceof SICBridge) {
				SICBridge br = (SICBridge)conn.getMasterModule();
				Vector<Connection> vPath2 = getConnectionPath(start, br.getBridgedInterface(conn.getMasterInterface()));
				if(vPath2.size()>0) {
					vPath.addAll(vPath2);
					vPath.add(conn);
					return vPath;
				}
			}
		}
		return vPath;
	}
	public Vector<Connection> getConnectionPath(BasicComponent start, BasicComponent end, SystemDataType type) {
		for(Interface startIf : start.getInterfaces(type, true)) {
			for(Interface endIf : end.getInterfaces(type, false)) {
				Vector<Connection> vPath = getConnectionPath(startIf, endIf);
				if(vPath.size()>0)
					return vPath;
			}
		}
		return new Vector<Connection>();
	}
	public Vector<Vector<Connection>> getAllPossiblePathsToSlave(Interface end) {
		return getAllPossiblePathsToSlave(end, new Vector<Connection>());
	}
	private Vector<Vector<Connection>> getAllPossiblePathsToSlave(Interface end, Vector<Connection> path) {
		Vector<Vector<Connection>> vvConn = new Vector<Vector<Connection>>();
		for(Connection conn : end.getConnections()) {
			Vector<Connection> newPath = new Vector<Connection>(path);
			newPath.add(conn);
			if(conn.getMasterModule() instanceof SICBridge) {
				Interface newEnd = ((SICBridge)conn.getMasterModule()).getBridgedInterface(conn.getMasterInterface());
				vvConn.addAll(getAllPossiblePathsToSlave(newEnd,newPath));
			} else {
				vvConn.add(newPath);
			}
		}
		return vvConn;
	}
	public void setVersion(String ver) {
		String[] vers = ver.split("\\.");
		versionStr = ver;
		try {
			versionMajor = Integer.decode(vers[0]);
			if(vers.length>1) {
				versionMinor = Integer.decode(vers[1]);
			}
		} catch (NumberFormatException e) {
			versionMinor = 0;
		}
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
		for(BasicComponent comp : vSystemComponents)
		{
			for(SopcComponentDescription.TransparentInterfaceBridge bridge : comp.getScd().getTransparentBridges())
			{
				Interface masterIntf = comp.getInterfaceByName(bridge.getMasterIntfName());
				Interface slaveIntf = comp.getInterfaceByName(bridge.getSlaveIntfName());
				if((masterIntf!=null)&&(slaveIntf!=null))
				{
					if(masterIntf.getType().equals(slaveIntf.getType()))
					{
						switch(masterIntf.getType())
						{
						case STREAMING: {
							Connection connToSlave = null;
							Connection connToMaster = null;
							if(!slaveIntf.getConnections().isEmpty()) {
								connToMaster = slaveIntf.getConnections().firstElement();
							}
							if(!masterIntf.getConnections().isEmpty()) {
								connToSlave = masterIntf.getConnections().firstElement();								
							}
							if((connToMaster!=null)&&(connToSlave!=null))
							{
								@SuppressWarnings("unused")
								Connection newConn = new Connection(connToMaster.getMasterInterface(), 
										connToSlave.getSlaveInterface(), masterIntf.getType(), true);
								connToMaster.getMasterInterface().getConnections().remove(connToMaster);
								connToSlave.getSlaveInterface().getConnections().remove(connToSlave);
							}
						} break;
						default: {
							Logger.logln("Transparent bridge in " + 
									comp.getInstanceName() + " of type " + comp.getClassName() + " is not yet supported.", LogLevel.WARNING);
						}
						}
					} else {
						Logger.logln("Master and slave interfaces for the Transparent bridge in " + 
								comp.getInstanceName() + " of type " + comp.getClassName() + " have different types.", LogLevel.WARNING);
					}
				} else {
					Logger.logln("Failed to find interfaces for the Transparent bridge in " + 
							comp.getInstanceName() + " of type " + comp.getClassName(), LogLevel.WARNING);
				}
			}
		}
		for(int i=0; i<vSystemComponents.size(); i++) {
			if(vSystemComponents.get(i).getScd().getGroup().equalsIgnoreCase("remove")) {
				/* First disconnect the component */
				for(Interface intf : vSystemComponents.get(i).getInterfaces()) {
					Vector<Connection> vConn = intf.getConnections();
					while(vConn.size()>0) {
						Connection conn = vConn.firstElement();
						Interface other;
						if(intf.isMaster()) {
							other = conn.getSlaveInterface();
						} else {
							other = conn.getMasterInterface();
						}
						if(other!=null) {
							other.getConnections().remove(conn);
						}
						vConn.remove(conn);
					}
				}
				/* Then remove it */
				vSystemComponents.remove(i);
				//don't advance
				i--;
			}
		}
		/*
		 * Now remove tristate (and other unneeded) bridges. If any.
		 * Also flatten hierarchical qsys designs.
		 */
		for(int i=0; i<vSystemComponents.size();i++)
		{
			BasicComponent comp = vSystemComponents.get(i);
			BasicComponent checkedComp = SopcComponentLib.getInstance().finalCheckOnComponent(comp);
			if(comp != checkedComp) {
				vSystemComponents.remove(comp);
				vSystemComponents.add(checkedComp);
				i=0;
			} else {
				if(vSystemComponents.get(i).removeFromSystemIfPossible(this))
				{
					//Restart loop after modifications...
					i=0;
				}
			}
		}
	}
	private boolean removeHierarchicalWrapperComponent(BasicComponent comp)
	{
		Logger.logln("Found possible QSys subsytem " + comp.getInstanceName() + " of type " + comp.getClassName(), LogLevel.DEBUG);
		if((versionMajor==10) || (versionMajor == 11)) {
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
		} else {
			boolean isConnected = false;
			for(Interface intf : comp.getInterfaces()) {
				if(!intf.getConnections().isEmpty()) {
					isConnected = true;
				}
			}
			if(isConnected) {
				Logger.logln(comp, "Looks like a QSys subsystem, but it's connected. Not removing.",LogLevel.INFO);
			} else {
				Logger.logln(comp, "Looks like an unconnected QSys subsystem. Removing...",LogLevel.DEBUG);
				removeSystemComponent(comp);
				return true;
			}
		}
		return false;
	}
	public boolean removeSystemComponent(BasicComponent comp) {
		return vSystemComponents.remove(comp);
	}
}
