/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2014 Walter Goossens <waltergoossens@home.nl>

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

import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTProperty;

public class ClockSource extends BasicComponent {
	long clockIndex;
	public ClockSource(BasicComponent cs) {
		super(cs);
	}
	public ClockSource(String cName, String iName, String ver,
			SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}
	public void setClockIndex(long idx) {
		clockIndex = idx;
	}
	public long getClockIndex() {
		return clockIndex;
	}
	protected DTNode createClockOutputNode(Interface cm, long idx, String lbl)
	{
		DTNode node = new DTNode(lbl, lbl);
		node.addProperty(new DTProperty("compatible", "fixed-clock"));
		node.addProperty(new DTProperty("#clock-cells", 0L));
		node.addProperty(new DTProperty("clock-frequency",null,freq2String(cm.getInterfaceValue()[0]), cm.getInterfaceValue()[0]));
		node.addProperty(new DTProperty("clock-output-names",getInstanceName() + '-' + cm.getName()));
		return node;
	}
	public static String freq2String(long freq) {
		float ffreq;
		String sXhz;
		if(freq > 1000000000) {
			ffreq = freq/1000000000.0f;
			sXhz = "GHz";
		} else if(freq > 1000000) {
			ffreq = freq/1000000.0f;
			sXhz = "MHz";
		} else if(freq > 1000) {
			ffreq = freq/1000.0f;
			sXhz = "kHz";
		} else {
			ffreq = freq;
			sXhz = "Hz";
		}
		return String.format("%.2f %s", ffreq,sXhz);
	}
	public DTNode toDTNode(BoardInfo bi,Connection conn)
	{
		Vector<Interface> vClkMasters = getInterfaces(SystemDataType.CLOCK, true);
		if(vClkMasters.size()==1) {
			return createClockOutputNode(vClkMasters.get(0), clockIndex, getInstanceName());
		} else {
			DTNode node = new DTNode(getInstanceName(), getInstanceName());
			long subIdx = 0;
			node.addProperty(new DTProperty("compatible", 
					getScd().getCompatibles(version).toArray(new String[]{}))); 
			node.addProperty(new DTProperty("#clock-cells", 1L));
			for(Interface clkIf : vClkMasters) {
				node.addChild(createClockOutputNode(clkIf, subIdx++, getInstanceName() + "_" + clkIf.getName()));
			}
			return node;
		}
	}
}
