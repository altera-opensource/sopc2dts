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
package sopc2dts;

import java.util.Date;

import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;

public class LogEntry {
	StackTraceElement[] trace;
	Date timestamp;
	Object source;
	String message;
	LogLevel level;
	
	public LogEntry(Object src, String msg, LogLevel ll) {
		timestamp = new Date();
		trace = new Throwable().getStackTrace();
		source = src;
		message = msg;
		level = ll;
	}

	public Object getSource() {
		return source;
	}
	public String getSourceStr() {
		if(source == null) {
			return "";
		} else if(source instanceof BasicComponent)
		{
			BasicComponent bc = (BasicComponent)source;
			return "Component: " + bc.getInstanceName() + '(' + bc.getClassName() + ')';
		} else if(source instanceof Interface) {
			Interface intf = (Interface)source;
			return "Interface: " + intf.getName() + " of " + intf.getOwner().getInstanceName() + '(' + intf.getOwner().getClassName() + ')';
		} else {
			return source.toString();
		}
	}
	public StackTraceElement[] getTrace() {
		return trace;
	}
	public String getMessage() {
		return message;
	}

	public LogLevel getLevel() {
		return level;
	}

	public Date getTimestamp() {
		return timestamp;
	}
	@Override
	public String toString() {
		if(source != null) {
			return getSourceStr() + ": " + message;
		} else {
			return message;
		}
	}
}
