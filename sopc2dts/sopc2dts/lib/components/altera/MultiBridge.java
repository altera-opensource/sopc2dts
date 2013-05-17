/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2013 Walter Goossens <waltergoossens@home.nl>

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
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.base.SICBridge;

public class MultiBridge extends BasicComponent {
	static SopcComponentDescription hpsBridgeSCD = new SopcComponentDescription("bridge", "bridge", "ALTR", "bridge");
	public MultiBridge(String cName, String iName, String ver, SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}
	public boolean removeFromSystemIfPossible(AvalonSystem sys)
	{
		String[] bridgeNames = { "h2f", "h2f_lw", "f2h" };
		for(String name : bridgeNames) {
			SICBridge br = getBridge(name);
			sys.addSystemComponent(br);
		}
		sys.removeSystemComponent(this);
		return true;
	}
	SICBridge getBridge(String baseName) {
		String[] intfNames = { "axi_%s", "%s_reset", "%s_axi_clock", "%s" };
		SICBridge bridge = new SICBridge(getClassName(), getInstanceName() + '_' + baseName, version, hpsBridgeSCD);
		for(String intfNameFormat : intfNames) {
			String ifName = String.format(intfNameFormat, baseName);
			Interface intf = getInterfaceByName(ifName);
			if(intf != null) {
				this.removeInterface(intf);
				bridge.addInterface(intf);
			} else {
				System.err.println("Failed to find " + ifName);
			}
		}
		return bridge;
	}
}
