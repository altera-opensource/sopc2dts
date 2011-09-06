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

import sopc2dts.lib.AvalonSystem;

public class GeneratorFactory {
	public enum GeneratorType { DTS, DTB, DTB_IHEX8, U_BOOT, KERNEL_HEADERS };

	public static String getGeneratorNameByType(GeneratorType genType)
	{
		switch(genType)
		{
		case DTS: {
			return "dts"; 
		}
		case DTB: {
			return "dtb";
		}
		case DTB_IHEX8: {
			return "dtb-hex8";
		}
		case U_BOOT: {
			return "u-boot";
		}
		case KERNEL_HEADERS: {
			return "kernel";
			
		}
		}
		return null;
	}
	public static String getGeneratorDescriptionByType(GeneratorType genType)
	{
		switch(genType)
		{
		case DTS: {
			return "<HTML>Device Tree Source<BR>A textual representation of the system.</HTML>"; 
		}
		case DTB: {
			return "<HTML>Device Tree Blob<BR>A compiled version of the dts</HTML>";
		}
		case DTB_IHEX8: {
			return "<HTML>Intel hex8 containing the dtb</HTML>";
		}
		case U_BOOT: {
			return "<HTML>U-Boot headers</HTML>";
		}
		case KERNEL_HEADERS: {
			return "<HTML>Linux Kernel headers.<BR>Not really needed anymore</HTML>";
			
		}
		}
		return null;
	}
	public static GeneratorType getGeneratorTypeByName(String type)
	{
		if(type.equalsIgnoreCase("dts"))
		{
			return GeneratorType.DTS;
		} else if(type.equalsIgnoreCase("dtb"))
		{
			return GeneratorType.DTB;
		} else if(type.equalsIgnoreCase("dtb-hex8"))
		{
			return GeneratorType.DTB_IHEX8;
		} else if(type.equalsIgnoreCase("u-boot")) {
			return GeneratorType.U_BOOT;
		} else if(type.equalsIgnoreCase("kernel"))
		{
			return GeneratorType.KERNEL_HEADERS;
		}
		return null;
	}
	public static AbstractSopcGenerator createGeneratorFor(AvalonSystem sys, String type)
	{
		return createGeneratorFor(sys, getGeneratorTypeByName(type));
	}
	public static AbstractSopcGenerator createGeneratorFor(AvalonSystem sys, GeneratorType genType)
	{
		switch(genType)
		{
		case DTS: {
			return new DTSGenerator(sys);			
		}
		case DTB: {
			return new DTBGenerator(sys);
		}
		case DTB_IHEX8: {
			return new DTBHex8Generator(sys);
		}
		case U_BOOT: {
			return new UBootHeaderGenerator(sys);
		}
		case KERNEL_HEADERS: {
			return new KernelHeadersGenerator(sys);	
		}
		}
		return null;
	}
}
