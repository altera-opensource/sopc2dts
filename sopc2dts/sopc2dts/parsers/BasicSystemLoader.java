package sopc2dts.parsers;

import java.io.File;

import sopc2dts.Logger;
import sopc2dts.lib.AvalonSystem;
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
			}
		}
		Logger.logln("Loading of system failed.");
		return null;
	}
}
