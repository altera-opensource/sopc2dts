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
				"  node [shape=rect]\n";
		for(BasicComponent comp : sys.getSystemComponents()) {
			res += "  " + comp.getInstanceName() + " [label=<<table border=\"0\">\n" +
					"\t<tr><td port=\"name\" bgcolor=\"lightgray\">" + comp.getInstanceName() + "</td></tr>\n" +
					"\t<tr><td port=\"class\">Class: " + comp.getClassName() + "</td></tr>\n";
			for(Interface intf : comp.getInterfaces()) {
				String color = getColorForInterface(intf, bi);
				if(color!=null) {
					res+="\t<tr><td port=\"" + undashify(intf.getName()) + "\" bgcolor=\"light" + color + "\"> " + intf.getName() + "</td></tr>\n";	
				}
			}
			res += "\t</table>>]\n";
		}
		for(BasicComponent comp : sys.getSystemComponents()) {
			for(Interface masterIf : comp.getInterfaces()) {
				String color = getColorForInterface(masterIf,bi);
				if(masterIf.isMaster() && color !=null) {
					for(Connection conn : masterIf.getConnections()) {
						Interface slaveIf = conn.getSlaveInterface();
						res += comp.getInstanceName() + ":" + undashify(masterIf.getName()) + " -> " + 
								slaveIf.getOwner().getInstanceName() + ":" + undashify(slaveIf.getName());
						res +=" [color=\"" + color + "\"";
						if(conn.getType() == SystemDataType.MEMORY_MAPPED) {
							res += " label=\"" + DTHelper.longArrToHexString(conn.getConnValue()) + "\"]\n";
						} else if (conn.getType() == SystemDataType.INTERRUPT) {
							res += " label=\"" + DTHelper.longArrToLong(conn.getConnValue()) + "\"]\n";
						} else {
							res += "]\n";
						}
					}
				}
			}
		}
		res += "}\n";
		return res;
	}
	String getColorForInterface(Interface intf, BoardInfo bi) {
		switch(intf.getType()) {
		case CLOCK:			return (bi.isShowClockTree() 	? "green"	: null);
		case CONDUIT:		return (bi.isShowConduits()		? "salmon"	: null);
		case MEMORY_MAPPED:	return 							  "blue";
		case RESET:			return (bi.isShowResets()		? "gray"	: null);
		case STREAMING:		return (bi.isShowStreaming()	? "yellow"	: null);
		case INTERRUPT:		return 							  "pink";
		case CUSTOM_INSTRUCTION: return						  "seagreen";
		default:			System.err.println("Not drawing type: " + intf.getType());return null;
		}
	}
	static String undashify(String in) {
		/* HTML Version seems to choke on both _ and - so just use a letter... */
		return in.replace('-','d').replace('_', 'u');
	}
}
