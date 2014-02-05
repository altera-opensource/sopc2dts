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
package sopc2dts.generators;

import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.devicetree.DTHelper;

public class GraphGenerator extends AbstractSopcGenerator {

	public GraphGenerator(AvalonSystem s) {
		super(s,true);
	}

	@Override
	public String getTextOutput(BoardInfo bi) {
		String res = "digraph sopc2dot {\n" +
				"  node [shape=Mrecord]\n";
		for(BasicComponent comp : sys.getSystemComponents()) {
			res += "  " + comp.getInstanceName() + "[label=\"{" + 
					comp.getInstanceName() + "|Class: " + comp.getClassName();
			for(Interface intf : comp.getInterfaces()) {
				if(showInterface(intf,bi)) {
					res+="|<" + intf.getName() + "> " + intf.getName();
				}
			}
			res += "}\"]\n";
		}
		for(BasicComponent comp : sys.getSystemComponents()) {
			for(Interface masterIf : comp.getInterfaces()) {
				if(masterIf.isMaster() && showInterface(masterIf,bi)) {
					for(Connection conn : masterIf.getConnections()) {
						Interface slaveIf = conn.getSlaveInterface();
						res += comp.getInstanceName() + ":" + masterIf.getName() + " -> " + 
								slaveIf.getOwner().getInstanceName() + ":" + slaveIf.getName();
						if(conn.getType() == SystemDataType.MEMORY_MAPPED) {
							res += " [label=\"" + DTHelper.longArrToHexString(conn.getConnValue()) + "\"]\n";
						} else {
							res += "\n";
						}
					}
				}
			}
		}
		res += "}\n";
		return res;
	}
	boolean showInterface(Interface intf, BoardInfo bi) {
		switch(intf.getType()) {
		case CLOCK:			return bi.isShowClockTree();
		case CONDUIT:		return bi.isShowConduits();
		case MEMORY_MAPPED:	return true;
		case RESET:			return bi.isShowResets();
		case STREAMING:		return bi.isShowStreaming();
		default:			return false;
		}
	}
}
