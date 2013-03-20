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

public class DTPropByte extends DTProperty{
	Vector<Integer> vValues = new Vector<Integer>();
	
	public DTPropByte(String name) {
		this(name,(Integer)null, null,null);
	}
	public DTPropByte(String name, Integer value) {
		this(name,value, null,null);
	}
	public DTPropByte(String name, Integer value, String label) {
		this(name,value, label,null);
	}
	public DTPropByte(String name, Integer value, String label, String comment) {
		super(name, label, comment, DTPropType.NUMBER);
		if(value != null)
		{
			vValues.add(value);
		}
	}
	public DTPropByte(String name, Vector<Integer> vVals, String label, String comment) {
		super(name, label, comment, DTPropType.NUMBER);
		if(vVals != null)
		{
			vValues.addAll(vVals);
		}
	}
	public void addValue(Integer val) {
		vValues.add(val);
	}
	public Vector<Integer> getValues() {
		return vValues;
	}

	@Override
	public String toString(int indent) {
		String res = indent(indent) + (label != null ? label + ": " : "" ) 
						+ name + " = [";
		for(Integer val : vValues)
		{
			res += String.format(" %02X", val);
		}
		res += " ];" + (comment != null ? "\t/* " + comment + " */\n": '\n');
		return res;
	}
	@Override
	protected byte[] getValueBytes() {
		byte[] val = new byte[vValues.size()];
		for(int i=0; i<val.length; i++)
		{
			val[i] = (byte)(vValues.get(i)&0xFF);
		}
		return val;
	}
}
