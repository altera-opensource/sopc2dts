/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2012 Walter Goossens <waltergoossens@home.nl>

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

import java.util.Vector;

import sopc2dts.lib.Connection;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.SopcComponentDescription;

public class CpuComponent extends BasicComponent {
	long cpuIndex = 0;
	public CpuComponent(BasicComponent comp) {
		super(comp);
	}
	public CpuComponent(String cName, String iName, String ver,
			SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}
	public void setCpuIndex(long idx) {
		cpuIndex = idx;
	}
	public long getCpuIndex() {
		return cpuIndex;
	}
	protected Vector<Long> getReg(BasicComponent master)
	{
		if(master == null) {
			Vector<Long> vRegs = new Vector<Long>();
			vRegs.add(cpuIndex);
			return vRegs;			
		} else {
			return super.getReg(master);
		}
	}
	protected long[] getAddrFromConnection(Connection conn)
	{
		if(conn == null) {
			return new long[]{ cpuIndex };			
		} else {
			return super.getAddrFromConnection(conn);
		}
	}
}
