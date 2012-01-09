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

import sopc2dts.generators.AbstractSopcGenerator;

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
		super(name, val, label, comment);
	}
	public DTPropHexNumber(String name, Vector<Long> val) {
		this(name,val,null, null);
	}
	public DTPropHexNumber(String name, Vector<Long> val, String label) {
		this(name,val,label, null);
	}
	protected DTPropHexNumber(String name, Vector<Long> vals,String label, String comment) {
		super(name, vals, label, comment);
	}
	String toStringValuesMemoryStyle(int indent) {
		String res = "";
		for(int i=0; i<vValues.size(); i++)
		{
			/* Magically format some nodes */
			if(i>0)
			{
				if(i%2==0)	//addr+size cells
				{
					if(name.equals("reg")) {
						res += "\n" + AbstractSopcGenerator.indent(indent) + "\t";
					}
				}
				if(i%3==0)	//paddr+addr+size cells
				{
					if(name.equals("ranges")) {
						res += "\n" + AbstractSopcGenerator.indent(indent) + "\t";
					}
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
	protected String toStringValues(int indent) {
		if(name.equals("reg") || name.equals("ranges"))
		{
			return toStringValuesMemoryStyle(indent);
		} else {
			return super.toStringValues(indent);
		}
	}
	

	String formattedLong(Long val)
	{
		if(val<0)
		{
			return String.format("0x%08x", val+0x100000000L);			
		} else if(val<0x100)
		{
			return String.format("0x%02x", val);
		} else if(val<0x10000)
		{
			return String.format("0x%04x", val);
		} else {
			return String.format("0x%08x", val);
		}
	}
}
