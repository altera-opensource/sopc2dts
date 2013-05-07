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
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;
public class VIPMixer extends BasicComponent {
	boolean connectedOthers = false;
	public VIPMixer(String cName, String iName, String ver, SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}
	public boolean removeFromSystemIfPossible(AvalonSystem sys)
	{
		if(!connectedOthers) {
			connectedOthers=true;
			Interface ifMaster = getInterfaceByName("dout");
			for(Connection conn2NewSlave : ifMaster.getConnections()) {
				for(Interface ifSlave : getInterfaces(SystemDataType.STREAMING, false))
				{
					for(Connection conn2NewMaster : ifSlave.getConnections()) {
						new Connection(conn2NewMaster.getMasterInterface(), conn2NewSlave.getSlaveInterface(), SystemDataType.STREAMING, true);
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}
}
