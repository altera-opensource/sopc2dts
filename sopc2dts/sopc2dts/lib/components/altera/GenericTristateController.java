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
package sopc2dts.lib.components.altera;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.Parameter;
import sopc2dts.lib.SopcComponentLib;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.base.SICFlash;
import sopc2dts.lib.components.base.SICUnknown;

public class GenericTristateController extends BasicComponent {
	private static final long serialVersionUID = -8170741481169652008L;

	public GenericTristateController(String cName, String iName, String ver) {
		super(cName, iName, ver, new SICUnknown(cName));
	}
	
	@Override
	public String toDtsExtras(BoardInfo bi, int indentLevel, Connection conn, Boolean endComponent)
	{
		String res = AbstractSopcGenerator.indent(indentLevel) + "//Dumping SOPC parameters...\n";
		for(Parameter bp : vParameters)
		{
			String assName = bp.getName();
			if(assName.startsWith("embeddedsw.CMacro.")) {
				assName = assName.substring(18);
			}
			if(assName!=null)
			{
				assName = assName.replace('_', '-');
				res += bp.toDts(indentLevel, 
						scd.getVendor() + ',' + assName, null);
			}
		}
		return res;
	}
	/**
	 * This is kinda abusing the function, because we _will_ try to remove ourselve
	 * but also add ourselve in a new form....
	 */
	@Override
	public boolean removeFromSystemIfPossible(AvalonSystem sys)
	{
		boolean bChanged = false;
		String driverType = getParamValByName("embeddedsw.configuration.softwareDriver");
		if(driverType==null)
		{
			driverType = getParamValByName("embeddedsw.configuration.hwClassnameDriverSupportDefault");
		}
		if(driverType==null)
		{
			Logger.logln("GenericTristateController: " + getInstanceName()
					+ " failed to detect driver type.", LogLevel.WARNING);
		} else {
			if(driverType.equalsIgnoreCase("altera_avalon_cfi_flash_driver") ||
					driverType.equalsIgnoreCase("altera_avalon_cfi_flash"))
			{
				Logger.logln("GenericTristateController: " + getInstanceName() 
						+ " seems to be a CFI-Flash chip.", LogLevel.INFO);
				SICFlash flash = new SICFlash(this);
				
				flash.setScd(SopcComponentLib.getInstance().getScdByClassName("altera_avalon_cfi_flash"));
				sys.getSystemComponents().remove(this);
				flash.getInterfaces().addAll(vInterfaces);
				for(Interface intf : flash.getInterfaces())
				{
					intf.setOwner(flash);
				}
				sys.getSystemComponents().add(flash);
				bChanged = true;
			} else {
				Logger.logln("GenericTristateController: " + getInstanceName() 
						+ " is of unsupported type: " + driverType, LogLevel.WARNING);
			}
		}
		return bChanged;
	}
}
