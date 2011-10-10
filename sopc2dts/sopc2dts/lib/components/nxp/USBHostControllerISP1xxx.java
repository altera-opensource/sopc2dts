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

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;

public class USBHostControllerISP1xxx extends BasicComponent {
	private static final long serialVersionUID = 6744716322519288192L;

	public USBHostControllerISP1xxx(String cName, String iName, String ver,
			SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}
	protected String getRegForDTS(int indentLevel, BasicComponent master)
	{
		String res = "";
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
					if(res.length()==0)
					{
						res = AbstractSopcGenerator.indent(indentLevel) + "reg = <";
					}
					long addr = getAddrFromConnection(conn);
					res += " 0x" + Long.toHexString(addr) + " 0x04 " +
							" 0x" + Long.toHexString(addr + 4) + " 0x04";
				}
			}
		}
		if(res.length()>0)
		{
			res += ">;\n";
		}
		return res;
	}

}
