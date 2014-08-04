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
package sopc2dts.lib.components.snps;

import java.util.Vector;

import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.Parameter;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.base.GpioController;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTPropNumVal;
import sopc2dts.lib.devicetree.DTPropVal;
import sopc2dts.lib.devicetree.DTPropStringVal;
import sopc2dts.lib.devicetree.DTProperty;
import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;

public class DwGpio
    extends GpioController
{
	public DwGpio(String cName, String iName, String ver,
		SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}
	public DwGpio(BasicComponent comp) {
		super(comp);
	}
	@Override
	public DTNode toDTNode(BoardInfo bi,Connection conn)
	{
		DTNode n = super.toDTNode(bi, conn);

		DTProperty prop = n.getPropertyByName("compatible");
		Vector<DTPropVal> vals = prop.getValues();
		vals.add(0, new DTPropStringVal("snps,dw-apb-gpio"));
		n.addProperty(new DTProperty("#address-cells", new DTPropNumVal(1)));
		n.addProperty(new DTProperty("#size-cells", new DTPropNumVal(0)));
		String[] ports = {"A","B", "C", "D"};
		int i = 0;
		for (String port : ports) {
			Parameter p = getParamByName(EMBSW_DTS+".instance.GPIO_PWIDTH_"+port);
			int width = 0;
			if (p == null) {
				if (i == 0) {
				  width = 27;	
				} 
			} else {
				width = Integer.parseInt(p.getValue());
			}
			if (width <= 0){
				continue;
			}

			DTNode subNode = new DTNode("gpio-controller@"+i, getInstanceName()+"_port"+port.toLowerCase());
			subNode.addProperty(new DTProperty("compatible",new DTPropStringVal("snps,dw-apb-gpio-port")));
			subNode.addProperty(new DTProperty("gpio-controller"));
			subNode.addProperty(new DTProperty("#gpio-cells", new DTPropNumVal(2)));
			subNode.addProperty(new DTProperty("snps,nr-gpios",new DTPropNumVal(width)));
			subNode.addProperty(new DTProperty("reg", new DTPropNumVal(i)));
			if (i == 0) {
				Parameter pint = getParamByName(EMBSW_DTS+".instance.GPIO_PORTA_INTR");
				if ((null == pint) || (pint.getValueAsBoolean())) {
					subNode.addProperty(new DTProperty("interrupt-controller"));
					subNode.addProperty(new DTProperty("#interrupt-cells", new DTPropNumVal(2)));
					subNode.addProperty(n.getPropertyByName("interrupts"));
					subNode.addProperty(n.getPropertyByName("interrupt-parent"));
				}
			}
			n.addChild(subNode);
			i++;
		}
		return n;
	}
}
