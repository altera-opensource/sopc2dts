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
package sopc2dts.lib.components.base;

import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.SopcComponentDescription;

public class SCDSelfDescribing extends SopcComponentDescription {
	private static final long serialVersionUID = -5986234098493565920L;

	public SCDSelfDescribing(BasicComponent comp)
	{
		super(comp.getScd().getClassName(),
				comp.getParamValByName("embeddedsw.dts.group"),
				comp.getParamValByName("embeddedsw.dts.vendor"),
				comp.getParamValByName("embeddedsw.dts.name"));
		if(device == null) {
			device = getClassName();
		}
	}
	
	/*
	 * Scans a BasicComponent for parameters given through the _hw.tcl file that
	 * describe the usage of that component in the device-tree.
	 * Those parameters are named embeddedsw.dts.* and some are mandatory and 
	 * some are optional. When there are enough parameters in the component, 
	 * those parameters will be used instead of the ones defined in the 
	 * sopc_component_*.xml files.
	 * In the long run this will remove the need for those xml-files.
	 */
	public static boolean isSelfDescribing(BasicComponent comp) {
		String pVal = comp.getParamValByName("embeddedsw.dts.vendor");
		if((pVal == null) || (pVal.length() == 0)) {
			//Vendor info is mandatory
			return false;
		}
		pVal = comp.getParamValByName("embeddedsw.dts.group");
		if((pVal == null) || (pVal.length() == 0)) {
			//group/type info is mandatory
			return false;
		}
		return true;
	}
}
