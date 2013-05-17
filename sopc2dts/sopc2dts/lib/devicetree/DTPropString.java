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
public class DTPropString extends DTProperty {

	public DTPropString(String name) {
		this(name,new Vector<String>(),null,null);
	}
	public DTPropString(String name, String value) {
		this(name,value,null,null);
	}
	public DTPropString(String name, String value, String label) {
		this(name,value,label,null);
	}
	public DTPropString(String name, String value, String label, String comment) {
		super(name, label, comment, (value == null ? null : new DTPropStringVal(value)));
	}
	public DTPropString(String name, Vector<String> values) {
		this(name,values,null,null);
	}
	public DTPropString(String name, Vector<String> values, String label) {
		this(name,values,label,null);
	}
	public DTPropString(String name, Vector<String> values, String label, String comment) {
		super(name, label, comment);
		for(String v : values) {
			vValues.add(new DTPropStringVal(v));
		}
	}
	public void addStrings(Vector<String> vals) {
		for(String v : vals) {
			vValues.add(new DTPropStringVal(v));
		}
	}
	public void addStrings(String[] vals) {
		for (String val : vals) {
			vValues.add(new DTPropStringVal(val));
		}
	}
}
