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
package sopc2dts.lib.components.nxp;

import java.util.Vector;

import sopc2dts.lib.Connection;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;

public class USBHostControllerISP1xxx extends BasicComponent {

	public USBHostControllerISP1xxx(String cName, String iName, String ver,
			SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}
	
	@Override
	protected Vector<Long> getReg(BasicComponent master) 
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
					long addr = getAddrFromConnection(conn);
					vRegs.add(addr);
					vRegs.add(4L);
					vRegs.add(addr+4);
					vRegs.add(4L);
				}
			}
		}
		return vRegs;
	}
}
