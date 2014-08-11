/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2014 Walter Goossens <waltergoossens@home.nl>

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
package sopc2dts.lib.components.altera.hps;

import java.util.Vector;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.components.BasicComponent;

public class ClockManagerA10 extends ClockManager {

	public ClockManagerA10(BasicComponent bc) {
		super(bc);
	}
	@Override
	protected String getFirstSupportedVersion() {
		/* Other classes can overload this function when needed in the future */
		return "14.0";
	}
	@Override
	protected boolean preRemovalChecks(AvalonSystem sys) {
		String hpsName = null;
		Vector<BasicComponent> vHPS = sys.getComponentsByClass("altera_arria10_hps");
		for(int i=0; i<vHPS.size() && (hpsName == null); i++) {
			if(getInstanceName().startsWith(vHPS.get(i).getInstanceName() + '_')) {
				hpsName = vHPS.get(i).getInstanceName();
			}
		}
		if(hpsName != null) {
			return true;
		} else {
			Logger.logln(this, "Failed to determine the HPS_A10 we belong to", LogLevel.WARNING);
			return false;
		}
	}
}
