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
package sopc2dts.generators;

import sopc2dts.lib.Parameter;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.components.BasicComponent;

public class KernelHeadersGenerator extends AbstractSopcGenerator {
	boolean minimal = true;
	public KernelHeadersGenerator(AvalonSystem s) {
		super(s,true);
	}

	@Override
	public String getExtension() {
		return "h";
	}

	@Override
	public String getTextOutput(BoardInfo bi) {
		String res = null;
		for(BasicComponent comp : sys.getSystemComponents())
		{
			if((!minimal) || comp.getScd().getGroup().equalsIgnoreCase("cpu"))
			{
				//Only dump nios stuff if REALLY needed
				if(res==null)
				{
					res = copyRightNotice + 
						"#ifndef _ALTERA_CPU_H_\n" +
						"#define _ALTERA_CPU_H_\n\n" +
						"/*\n" +
						" * Warning:\n" +
						" * from kernel 2.6.38 onwards this is not really needed anymore\n" +
						" * Just choose \"Generic devicetree based NiosII system\" as your \n" +
						" * target board and you should be fine.\n" +
						" */\n\n";
				}
				res += "/*\n" +
						" * Dumping parameters for " + comp.getInstanceName() + " (type " + comp.getScd().getGroup() + ")\n" +
						" * This is not as clean as I hoped but FDT is just a tad late\n" +
						" */\n";
				for(Parameter ass : comp.getParams())
				{
					if(ass.getName().toUpperCase().startsWith("EMBEDDEDSW.CMACRO."))
					{
						res += "#define " + ass.getName().substring(18).toUpperCase() + "\t" + ass.getValue() + "\n";
					}
				}
			}
		}
		if(res!=null)
		{
			res += "\n#endif //_ALTERA_CPU_H_\n";
		}
		return res;
	}

}
