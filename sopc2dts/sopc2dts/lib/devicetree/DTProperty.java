/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2012 - 2013 Walter Goossens <waltergoossens@home.nl>

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;

import sopc2dts.Logger;

public class DTProperty extends DTElement {
	public static final int OF_DT_PROP = 0x03;
	int numValsPerRow = 0;
	Vector<DTPropVal> vValues = new Vector<DTPropVal>();
	public DTProperty(String name) {
		this(name,null,null);
	}
	public DTProperty(String name,String val) {
		this(name,null,null,val);
	}
	public DTProperty(String name,String[] vals) {
		this(name,null,null, vals);
	}
	public DTProperty(String name, String label, String comment) {
		super(name,label,comment);
	}
	public DTProperty(String name, String label, String comment, String val) {
		this(name,label,comment,new DTPropStringVal(val));
	}
	public DTProperty(String name, String label, String comment, String[] vals) {
		this(name,label,comment);
		addStringValues(vals);
	}
	public DTProperty(String name, String label, String comment, DTPropVal val)
	{
		this(name,label,comment);
		if(val!=null) {
			vValues.add(val);
		}
	}
	public void addValue(DTPropVal val) {
		vValues.add(val);
	}
	public void addStringValues(String[] vals) {
		for(String v : vals) {
			vValues.add(new DTPropStringVal(v));
		}
	}
	public byte[] getBytes(DTBlob dtb)
	{
		byte[] valBytes = getAllValueBytes();
		byte[] buff = new byte[12+((valBytes.length +3)& ~3)];
		DTBlob.putU32(OF_DT_PROP, buff, 0);
		DTBlob.putU32(valBytes.length, buff, 4);
		DTBlob.putU32(dtb.registerString(name), buff, 8);
		if(valBytes.length>0)
		{
			System.arraycopy(valBytes, 0, buff, 12, valBytes.length);
		}
		return buff;
	}
	public Vector<DTPropVal> getValues() {
		return vValues;
	}
	
	byte[] getAllValueBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for(DTPropVal val : vValues) {
			try {
				baos.write(val.getValueBytes());
			} catch (IOException e) {
				Logger.logException(e);
			}
		}
		return baos.toByteArray();
	}
	@Override
	public String toString(int indent) {
		String res = indent(indent) + (label != null ? label + ": " : "" ) + name;
		DTPropVal prevVal = null;
		int valNum = 0;
		String nlStr = "";
		for(DTPropVal val : vValues) {
			if((numValsPerRow>0) && ((valNum%numValsPerRow)==0)) {
				nlStr = "\n" + indent(indent+1);
			} else {
				nlStr = "";
			}
			if(prevVal == null) {
				res += " = " + val.opening;
			} else if (!val.isTypeCompatible(prevVal.type)) {
				res += prevVal.closing + "," + nlStr + val.opening;
			} else {
				res += val.seperator + nlStr;
			}
			res += val.toString();
			prevVal = val;
			valNum++;
		}
		if(prevVal != null) {
			res += prevVal.closing;
		}
		return res + (comment != null ? ";\t/* " + comment + " */\n": ";\n");
	}

	public String toString()
	{
		return toString(0);
	}
	public void setNumValuesPerRow(int n) 
	{
		numValsPerRow = n;
	}
}
