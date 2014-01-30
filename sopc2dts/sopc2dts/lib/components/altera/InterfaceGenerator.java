/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2014 Matthew Gerlach <mgerlach@altera.com>

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

import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.base.ClockSource;

public class InterfaceGenerator extends BasicComponent
{
	static SopcComponentDescription h2f_userclk_scd = new SopcComponentDescription("clock", "clock", "altr", "clock");
	private boolean removed = false;

	public InterfaceGenerator(String cName, String iName, String ver, SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}
	private void removeUserNClock(int num, AvalonSystem sys)
	{
		String ifName = String.format("h2f_user%d_clock", num);
		Interface intf = getInterfaceByName(ifName);
		if (intf != null) {
			getInterfaces().remove(intf);
			ClockSource newclk = new ClockSource(ifName, ifName, null, h2f_userclk_scd);
			newclk.addInterface(intf);
		}
	}
	@Override
	public boolean removeFromSystemIfPossible(AvalonSystem sys)
	{
		if (!removed) {
			removed = true;
			for (int i = 0; i < 3; i++) {
				removeUserNClock(i, sys);
			}
			return true;
		} else {
			return false;
		}
	}
}
