/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2012 Walter Goossens <waltergoossens@home.nl>

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
package sopc2dts.lib.devicetree;

import java.util.Vector;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.Interface;

public class DTHelper {
	public static long[] parseLongString(String str, int numCells) {
		long[] res = new long[numCells];
		long val = Long.decode(str);
		switch(numCells) {
		case 1: {
			res[0] = val;
		} break;
		case 2: {
			long2longArr(val,res);
		} break;
		default: {
			Logger.logln("Unsupported cell count" + numCells, LogLevel.ERROR);
		}
		}
		return res;
	}
	public static long[] longArrAdd(long[] la, long val) {
		long[] res = new long[la.length];
		System.arraycopy(la, 0, res, 0, la.length);
		for(int i=0; ((i<res.length) &&(val>0)); i++) {
			res[i] += (val&0xFFFFFFFFL);
			val = (val >> 32) & 0xFFFFFFFFL;
			val += (res[i] >> 32) & 0xFFFFFFFFL;
		}
		return res;
	}
	public static long[] longArrAdd(long[] la, long[] la2) {
		long[] res = new long[la.length];
		long val = 0;;
		System.arraycopy(la, 0, res, 0, la.length);
		for(int i=0; (i<res.length); i++) {
			val += la2[i];
			res[i] += (val&0xFFFFFFFFL);
			val = (val >> 32) & 0xFFFFFFFFL;
			val += (res[i] >> 32) & 0xFFFFFFFFL;
		}
		return res;
	}
	public static boolean longArrCompare(long[] la1, long val) {
		long[] la2 = new long[la1.length];
		la2 = long2longArr(val, la2);
		return longArrCompare(la1, la2);
	}
	public static boolean longArrCompare(long[] la1, long[] la2) {
		if(la1.length != la2.length) {
			return false;
		}
		for(int i=0; i<la1.length; i++) {
			if(la1[i] != la2[i]) {
				return false;
			}
		}
		return true;
	}
	public static String longArrToHexString(long[] val) {
		String res = "0x";
		boolean valueFound = false;
		for(int i=(val.length-1); i>=0; i--) {
			if(valueFound) {
				res += String.format("%08X", val[i]);
			} else {
				if(val[i]!=0) {
					res += Long.toHexString(val[i]);
					valueFound = true;
				} else {
					if(i==0) {
						res += "00";
					}
				}
				
			}
		}
		return res;
	}
	public static String longArrToString(long[] val) {
		return ""+longArrToLong(val);
	}
	
	public static long longArrToLong(long[] val) {
		long res = 0;
		if(val.length>2) {
			Logger.logln("Cannot longArr 2 string is not support for values over 64 bits", LogLevel.ERROR);
		} else {
			for(int i=0; i<val.length;  i++) {
				res += (val[i]<<(i*32));
			}
		}
		return res;
	}

	public static long[] long2longArr(long val, long[] res) {
		if(res.length==1) {
			res[0] = val&0xFFFFFFFFL;
		} else if (res.length>1) {
			res[res.length-2] = (val>>32)&0xFFFFFFFFL;
			res[res.length-1] = val&0xFFFFFFFFL;
			for(int i=0; i<res.length-2; i++) {
				res[i] = 0;
			}
		}
		return res;
	}
	public static long[] parseAddress4Intf(String str, Interface intf) {
		return parseLongString(str, intf.getPrimaryWidth());
	}
	public static long[] parseAddress4Conn(String str, Connection conn) {
		return parseLongString(str, conn.getMasterInterface().getPrimaryWidth());
	}
	public static long[] parseSize4Intf(String str, Interface intf) {
		return parseLongString(str, intf.getSecondaryWidth());
	}
	public static void addAllLongs(Vector<Long> vLongs, long[] val) {
		if(val != null) {
			for(int i=0; i<val.length; i++) {
				vLongs.add(val[i]);
			}
		}
	}
}
