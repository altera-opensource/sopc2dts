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

import java.util.Vector;
@Deprecated
public class DTPropHexNumber extends DTPropNumber {

	public DTPropHexNumber(String name) {
		super(name);
	}
	public DTPropHexNumber(String name, Long val) {
		super(name, val);
	}
	public DTPropHexNumber(String name, Long val, String label) {
		super(name, val, label);
	}
	public DTPropHexNumber(String name, Long val, String label, String comment) {
		super(name, label, comment, (val == null ? null : new DTPropHexNumVal(val)));
	}
	public DTPropHexNumber(String name, Vector<Long> val) {
		this(name,val,null, null);
	}
	public DTPropHexNumber(String name, Vector<Long> val, String label) {
		this(name,val,label, null);
	}
	public DTPropHexNumber(String name, Vector<Long> vals,String label, String comment) {
		super(name, (Long)null, label, comment);
		addValues(vals);
	}
	String toStringValuesMemoryStyle(int indent) {
		String res = "";
		for(int i=0; i<vValues.size(); i++)
		{
			/* Magically format some nodes */
			if(i>0)
			{
				if((numValsPerRow>0) && (i%numValsPerRow==0))
				{
					res += "\n" + indent(indent) + "\t";
				}
			}
			if(!res.endsWith("\t"))
			{
				res += ' ';
			}
			res += String.format("0x%08X", vValues.get(i));
		}
		return res;
	}
	public void addValue(Long val) {
		vValues.add(new DTPropHexNumVal(val));
	}
	public void addValues(long[] vals) {
		for(Long val : vals) {
			vValues.add(new DTPropHexNumVal(val));
		}
	}
	public void addValues(Vector<Long> vals) {
		for(Long val : vals) {
			vValues.add(new DTPropHexNumVal(val));
		}
	}
}
