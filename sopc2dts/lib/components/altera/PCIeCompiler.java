/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2016 Tien Hock Loh <thloh@altera.com>

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

import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.base.SCDSelfDescribing;

public class PCIeCompiler {

	public static BasicComponent getPCIeComponent(BasicComponent comp) {
		if(comp.getClassName().equalsIgnoreCase("altera_pcie_a10_hip") ||
				comp.getClassName().equalsIgnoreCase("altera_pcie_cv_hip_avmm") ||
				comp.getClassName().equalsIgnoreCase("altera_pcie_av_hip_avmm")) {
			if(comp.getScd().getDevice().equalsIgnoreCase("pcie-root-port")) {
				return new PCIeRootPort(comp);
			}
		}
		return comp;
	}
}
