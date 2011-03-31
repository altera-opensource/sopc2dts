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
package sopc2dts.parsers;

import java.io.File;

import sopc2dts.Logger;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.parsers.ptf.PtfSystemLoader;
import sopc2dts.parsers.qsys.QSysSystemLoader;
import sopc2dts.parsers.sopcinfo.SopcInfoSystemLoader;

public class BasicSystemLoader  {
	public static AvalonSystem loadSystem(File source)
	{
		if(source.exists())
		{
			if(source.getName().endsWith(".sopcinfo"))
			{
				SopcInfoSystemLoader sisl = new SopcInfoSystemLoader();
				return sisl.loadSystem(source);
			} else if(source.getName().endsWith(".qsys"))
			{
				QSysSystemLoader qsl = new QSysSystemLoader();
				return qsl.loadSystem(source);
			} else if(source.getName().endsWith(".ptf"))
			{
				PtfSystemLoader pleaseUpgradeYourQuartus = new PtfSystemLoader();
				return pleaseUpgradeYourQuartus.loadSystem(source);
			} else {
				Logger.logln("Don't know how to parse " + source.getName());
			}
		}
		Logger.logln("Loading of system failed.");
		return null;
	}
}
