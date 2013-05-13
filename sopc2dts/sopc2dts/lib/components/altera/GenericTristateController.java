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
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.SopcComponentLib;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.base.SICFlash;
import sopc2dts.lib.components.base.SICUnknown;

public class GenericTristateController extends BasicComponent {

	public GenericTristateController(String cName, String iName, String ver) {
		super(cName, iName, ver, new SICUnknown(cName));
	}
	
	/**
	 * This is kinda abusing the function, because we _will_ try to remove ourselve
	 * but also add ourselves in a new form....
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
				sys.removeSystemComponent(this);
				sys.addSystemComponent(flash);
				bChanged = true;
			} else if(driverType.equalsIgnoreCase("altera_avalon_lan91c111"))
			{
				Logger.logln("GenericTristateController: " + getInstanceName()
					     + " seems to be a SMSC LAN91c111 chip.", LogLevel.INFO);
				SICLan91c111 lan = new SICLan91c111(this);
				
				lan.setScd(SopcComponentLib.getInstance().getScdByClassName("altera_avalon_lan91c111"));
				sys.removeSystemComponent(this);
				sys.addSystemComponent(lan);
				bChanged = true;
			} else {
				Logger.logln("GenericTristateController: " + getInstanceName() 
						+ " is of unsupported type: " + driverType, LogLevel.WARNING);
			}
		}
		return bChanged;
	}
}
