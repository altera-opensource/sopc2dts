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
package sopc2dts.lib.components.nxp;

import java.util.Vector;

import sopc2dts.lib.Connection;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.devicetree.DTHelper;

public class USBHostControllerISP1xxx extends BasicComponent {

	public USBHostControllerISP1xxx(String cName, String iName, String ver,
			SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}
	

	/** @todo Find out regnames */
	@Override
	protected Vector<Long> getReg(BasicComponent master, Vector<String> vRegNames) 
	{
		Vector<Long> vRegs = new Vector<Long>();
		for(Interface intf : vInterfaces)
		{
			if(intf.isMemorySlave())
			{
				//Check all interfaces for a connection to master
				Connection conn = null;
				for(int i=0; (i<intf.getConnections().size()) && (conn==null); i++)
				{
					if(intf.getConnections().get(i).getMasterModule().equals(master))
					{
						conn = intf.getConnections().get(i);
					}
				}
				if((conn!=null) && (intf!=null))
				{
					long[] addr = getAddrFromConnection(conn);
					long[] size = new long[intf.getSecondaryWidth()];
					size = DTHelper.long2longArr(4L, size);
					DTHelper.addAllLongs(vRegs, addr);
					DTHelper.addAllLongs(vRegs, size);
					addr = DTHelper.longArrAdd(addr, 4L);
					DTHelper.addAllLongs(vRegs, addr);
					DTHelper.addAllLongs(vRegs, size);
				}
			}
		}
		return vRegs;
	}
}
