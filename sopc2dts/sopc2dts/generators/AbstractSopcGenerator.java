package sopc2dts.generators;

import sopc2dts.lib.SopcInfoSystem;

public abstract class AbstractSopcGenerator {
	protected static String copyRightNotice = "/*\n" +
	" * Copyright (C) 2010 Walter Goossens <waltergoossens@home.nl>.\n" +
	" *\n" +
	" * This program is free software; you can redistribute it and/or modify\n" +
	" * it under the terms of the GNU General Public License as published by\n" +
	" * the Free Software Foundation; either version 2 of the License, or\n" +
	" * (at your option) any later version.\n" +
	" *\n" +
	" * This program is distributed in the hope that it will be useful, but\n" +
	" * WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
	" * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE, GOOD TITLE or\n" +
	" * NON INFRINGEMENT.  See the GNU General Public License for more\n" +
	" * details.\n" +
	" *\n" +
	" * You should have received a copy of the GNU General Public License\n" +
	" * along with this program; if not, write to the Free Software\n" +
	" * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.\n" +
	" *\n" +
	" */\n";

	SopcInfoSystem sys;
	public AbstractSopcGenerator(SopcInfoSystem s)
	{
		sys = s;
	}
	public static String indent(int level)
	{
		String res = "";
		while(level-->0)
		{
			res += "\t";
		}
		return res;
	}
	protected static String definenify(String in)
	{
		return in.toUpperCase().replace("-", "_");
	}
	public abstract String getExtension();
	public abstract String getOutput(String pov);
}
