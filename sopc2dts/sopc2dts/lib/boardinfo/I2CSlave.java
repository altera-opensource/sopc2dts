/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2013 Walter Goossens <waltergoossens@home.nl>

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
package sopc2dts.lib.boardinfo;

import org.xml.sax.Attributes;

import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTPropHexNumber;
import sopc2dts.lib.devicetree.DTProperty;

public class I2CSlave implements Comparable<I2CSlave> {
	public static final String TAG_NAME = "I2CChip";
	int addr;
	String label;
	String name;
	
	public I2CSlave(Attributes atts) {
		this(Integer.decode(atts.getValue("addr")), atts.getValue("name"), atts.getValue("label"));
	}
	public I2CSlave(int a, String n, String l) {
		addr = a;
		name = n;
		label = l;
	}
	public String getXml() {
		String xml= "\t\t<" + TAG_NAME + " addr=\"" + addr + "\" name=\"" + name + "\"";
		if((label != null) && (!label.isEmpty())) {
			xml += " label=\"" + label + '"';
		}
		xml += "/>\n";
		return xml;
	}
	public DTNode toDTNode(BoardInfo bi)
	{
		DTNode node = new DTNode(name + "@0x" + Integer.toHexString(addr),label);
		node.addProperty(new DTProperty("compatible", name));
		node.addProperty(new DTPropHexNumber("reg", Long.valueOf(addr)));
		return node;
	}
	public int compareTo(I2CSlave o) {
		return this.addr - o.addr;
	}
	public int getAddr() {
		return addr;
	}
	public void setAddr(int addr) {
		this.addr = addr;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
