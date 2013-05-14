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

public class DTPropNumber extends DTProperty{
	
	public DTPropNumber(String name) {
		this(name,0L, null, null);
	}
	public DTPropNumber(String name, Long val) {
		this(name,val,null, null);
	}
	public DTPropNumber(String name, Long val, String label) {
		this(name,val,label, null);
	}
	public DTPropNumber(String name, Long val, String label, String comment) {
		super(name,label,comment,(val == null ? null : new DTPropNumVal(val)));
	}
	public DTPropNumber(String name, String label, String comment, DTPropNumVal val) {
		super(name,label,comment,val);
	}
	public DTPropNumber(String name, Vector<Long> val) {
		this(name,val,null, null);
	}
	public DTPropNumber(String name, Vector<Long> val, String label) {
		this(name,val,label, null);
	}
	public DTPropNumber(String name, Vector<Long> vals,String label, String comment) {
		super(name, label, comment);
		for(Long val : vals) {
			vValues.add(new DTPropNumVal(val));
		}
	}
	public void addValue(Long val) {
		vValues.add(new DTPropNumVal(val));
	}
	public void addValues(long[] vals) {
		for(Long val : vals) {
			vValues.add(new DTPropNumVal(val));
		}
	}
}
