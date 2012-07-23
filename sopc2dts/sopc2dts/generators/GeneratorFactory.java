/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011-2012 Walter Goossens <waltergoossens@home.nl>

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
	public enum GeneratorType { DTS, DTB, DTB_IHEX8, DTB_IHEX32, U_BOOT, KERNEL_HEADERS, DTB_CHAR_ARR };

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
		case DTB_IHEX32: {
			return "dtb-hex32";
		}
		case DTB_CHAR_ARR: {
			return "dtb-char-arr";
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
		case DTB_CHAR_ARR:
		case DTB: {
			return "<HTML>Device Tree Blob<BR>A compiled version of the dts</HTML>";
		}
		case DTB_IHEX8: {
			return "<HTML>Intel hex8 containing the dtb</HTML>";
		}
		case DTB_IHEX32: {
			return "<HTML>Intel hex32 containing the dtb<BR>compatible with Altera 32bits onchip rom</HTML>";
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
		} else if(type.equalsIgnoreCase("dtb-hex32"))
		{
			return GeneratorType.DTB_IHEX32;
		} else if(type.equalsIgnoreCase("dtb-char-arr"))
		{
			return GeneratorType.DTB_CHAR_ARR;
		} else if(type.equalsIgnoreCase("u-boot") || type.equalsIgnoreCase("uboot")) {
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
		if(genType != null)
		{
			switch(genType)
			{
			case DTS: {
				return new DTSGenerator2(sys);
			}
			case DTB: {
				return new DTBGenerator2(sys);
			}
			case DTB_IHEX8: {
				return new DTBHex8Generator(sys);
			}
			case DTB_IHEX32: {
				return new DTBHex32Generator(sys);
			}
			case DTB_CHAR_ARR: {
				return new DTBCCharArray(sys);
			}
			case U_BOOT: {
				return new UBootHeaderGenerator(sys);
			}
			case KERNEL_HEADERS: {
				return new KernelHeadersGenerator(sys);	
			}
			}
		}
		return null;
	}
}
