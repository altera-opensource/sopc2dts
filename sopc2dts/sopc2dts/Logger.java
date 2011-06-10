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
package sopc2dts;

import java.util.Vector;

public class Logger {
	public enum LogLevel { ERROR, WARNING, INFO, DEBUG };
	private static LogLevel verbosity = LogLevel.WARNING;
	private static boolean useStdOutErr = true;
	private static Vector<LogListener> vListeners = new Vector<LogListener>();
	
	public static void increaseVerbosity()
	{
		switch (verbosity) {
		case ERROR: 	verbosity = LogLevel.WARNING; break;
		case WARNING:	verbosity = LogLevel.INFO; break;
		case INFO:		verbosity = LogLevel.DEBUG; break;
		default:
			break;
		}
	}
	public static void decreaseVerbosity()
	{
		switch (verbosity) {
		case WARNING:	verbosity = LogLevel.ERROR; break;
		case INFO: 		verbosity = LogLevel.WARNING; break;
		case DEBUG:		verbosity = LogLevel.INFO; break;
		default:
			break;
		}
	}
	public static void setVerbosity(LogLevel v)
	{
		verbosity = v;
	}
	public static void log(String log)
	{
		log(log,LogLevel.INFO);
	}
	public static void log(String log, LogLevel ll)
	{
		if(ll.compareTo(verbosity)<=0)
		{
			for(LogListener l: vListeners)
			{
				l.messageLogged(log);
			}
			if(useStdOutErr)
			{
				if(ll.equals(LogLevel.ERROR))
				{
					System.err.print(log);
				} else {
					System.out.print(log);
				}
			}
		}
	}
	public static void logln(String log)
	{
		logln(log,LogLevel.INFO);
	}
	public static void logln(String log, LogLevel ll)
	{
		log(log+'\n',ll);
	}
	public static void logException(Exception e)
	{
		logln(e.getMessage(), LogLevel.INFO);
		if(verbosity == LogLevel.DEBUG)
		{
			e.printStackTrace();
		}
	}
	public static void setUseStdOutErr(boolean use) {
		useStdOutErr = use;
	}
	public static boolean isUseStdOutErr() {
		return useStdOutErr;
	}
	public static void addLogListener(LogListener ll)
	{
		vListeners.add(ll);
	}
	public static void removeLogListener(LogListener ll)
	{
		vListeners.remove(ll);
	}
}
