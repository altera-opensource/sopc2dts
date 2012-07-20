/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 - 2012 Walter Goossens <waltergoossens@home.nl>

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

import java.io.IOException;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;

/** @brief Old Generator for DTB files.
 * 
 * This generator runs an external dtc (devicetree compiler). But since this 
 * requires an external program in your PATH which might not be trivial on 
 * certain operating systems, this class is deprecated in favor of DTBGenerator2
 * which directly generates a DTB internally.
 * 
 * @see DTBGenerator2
 * @author Walter Goossens
 *
 */
@Deprecated
public class DTBGenerator extends DTSGenerator {

	/** @brief Constructor for the DTBGenerator
	 * 
	 * @param s The AvalonSystem to generate for
	 */
	public DTBGenerator(AvalonSystem s) {
		super(s);
		generateTextOutput = false;
	}

	@Override
	public byte[] getBinaryOutput(BoardInfo bi)
	{
		byte[] res = null;
		try {
			Process proc = Runtime.getRuntime().exec(new String[] {
					"dtc","-I","dts","-O","dtb"});
			proc.getOutputStream().write(getTextOutput(bi).getBytes());
			proc.getOutputStream().close();
			Logger.logln("dtc returned " + proc.waitFor(), LogLevel.DEBUG);
			res = new byte[proc.getInputStream().available()];
			proc.getInputStream().read(res);
		} catch (IOException e) {
			Logger.logException(e);
		} catch (InterruptedException e) {
			Logger.logException(e);
		}
		return res;
	}
}
