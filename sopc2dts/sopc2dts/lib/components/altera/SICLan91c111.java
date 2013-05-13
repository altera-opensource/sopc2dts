/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 - 2012 Walter Goossens <waltergoossens@home.nl>

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

import sopc2dts.lib.Connection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.base.SICEthernet;
import sopc2dts.lib.devicetree.DTHelper;

public class SICLan91c111 extends SICEthernet {

	public SICLan91c111(BasicComponent comp) {
		super(comp);
	}
	
	public SICLan91c111(String cName, String iName, String ver, SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}

	@Override
	protected long[] getAddrFromConnection(Connection conn)
	{
		//Yes this is REALLY ugly. But it just might work :)
		long regOffset;
		try {
			String v = getParamValByName("registerOffset");
			if (v == null)
				v = getParamValByName("embeddedsw.CMacro.LAN91C111_REGISTERS_OFFSET");
			
			regOffset = Long.decode(v);
		} catch(Exception e)
		{
			regOffset = 0;
		}
		return DTHelper.longArrAdd(conn.getConnValue(), regOffset);
	}
}
