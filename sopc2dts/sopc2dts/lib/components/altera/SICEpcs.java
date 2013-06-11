/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 - 2013 Walter Goossens <waltergoossens@home.nl>

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

import java.util.Vector;

import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.SopcComponentLib;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.base.SICFlash;
import sopc2dts.lib.devicetree.DTHelper;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTProperty;

public class SICEpcs extends SICFlash {
	boolean bAddressFixed = false;
	public SICEpcs(String cName, String iName, String ver) {
		super(cName, iName, ver, SopcComponentLib.getInstance().getScdByClassName("altera_avalon_spi"));
	}
	
	@Override
	public DTNode toDTNode(BoardInfo bi, Connection conn)
	{
		DTNode node = super.toDTNode(bi, conn);
		node.addProperty(new DTProperty("#address-cells", 1L));
		node.addProperty(new DTProperty("#size-cells", 0L));
		DTNode m25p80 = new DTNode("m25p80@0");
		m25p80.addProperty(new DTProperty("compatible", "m25p80"));
		m25p80.addProperty(new DTProperty("spi-max-frequency", 25000000L));
		m25p80.addProperty(new DTProperty("reg", 0L));
		addPartitionsToDTNode(bi, m25p80);
		node.addChild(m25p80);
		return node;
	}
	
	@Override
	public boolean removeFromSystemIfPossible(AvalonSystem sys)
	{
		if(!bAddressFixed) {
			bAddressFixed = true;
			try {
				long regOffset = Long.decode(getParamValByName("embeddedsw.CMacro.REGISTER_OFFSET"));
				if(regOffset>0) {
					Vector<Interface> vintf = getInterfaces(SystemDataType.MEMORY_MAPPED, false);
					for(Interface intf : vintf) {
						for(Connection conn : intf.getConnections()) {
							conn.setConnValue(DTHelper.longArrAdd(conn.getConnValue(), regOffset));
						}
						intf.setInterfaceValue(DTHelper.longArrSubtract(intf.getInterfaceValue(), regOffset));
					}
				}
			} catch(Exception e) {
			}			
			return true;			
		} else {
			return false;
		}
	}
}
