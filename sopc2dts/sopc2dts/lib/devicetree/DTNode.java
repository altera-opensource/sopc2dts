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

public class DTNode extends DTElement {
	public static final int OF_DT_BEGIN_NODE = 0x01;
	public static final int OF_DT_END_NODE = 0x02;
	Vector<DTNode> vChildren = new Vector<DTNode>();
	Vector<DTProperty> vProps = new Vector<DTProperty>();
	
	public DTNode(String name) {
		super(name);
	}
	public DTNode(String name, String label) {
		super(name, label);
	}
	public DTNode(String name, String label, String comment) {
		super(name, label, comment);
	}
	public void addChild(DTNode child)
	{
		if(child!=null)
		{
			vChildren.add(child);
		}
	}
	public void addProperty(DTProperty prop)
	{
		if(prop!=null)
		{
			vProps.add(prop);
		}
	}
	public Vector<DTNode> getChildren()
	{
		return vChildren;
	}
	public Vector<DTProperty> getProperties()
	{
		return vProps;
	}
	public DTProperty getPropertyByName(String name)
	{
		for(DTProperty prop : vProps)
		{
			if(prop.getName().equals(name))
			{
				return prop;
			}
		}
		return null;
	}
	@Override
	public String toString(int indent) {
		String res = "\n" + (comment !=null ? indent(indent) + "/*" + comment + "*/\n" : "") +
			indent(indent) + (label != null ? label + ": " : "") + name + " {\n";
		indent++;
		for(DTProperty prop : vProps)
		{
			res += prop.toString(indent);
		}
		for(DTNode child : vChildren)
		{
			res += child.toString(indent);
		}
		indent--;
		res += indent(indent) + "}; //end " + name + (label != null ? " (" + label + ")\n" : "\n");
		return res;
	}
	@Override
	public byte[] getBytes(DTBlob dtb) {
		byte[] dt;
		int size = 0;
		int pos = 0;
		Vector<byte[]> vBytes = new Vector<byte[]>();
		String tmpName = (name == null ? "" : name);
		byte[] header = new byte[4 + ((tmpName.length() + 4) & ~3)];
		byte[] footer = new byte[4];
		DTBlob.putU32(OF_DT_BEGIN_NODE, header, 0);
		DTBlob.putStringAligned(tmpName, header, 4);
		DTBlob.putU32(OF_DT_END_NODE, footer, 0);
		vBytes.add(header);
		size += header.length;
		for(DTProperty prop : vProps) {
			byte b[] = prop.getBytes(dtb);
			vBytes.add(b);
			size+=b.length;
		}
		for(DTNode child : vChildren) {
			byte b[] = child.getBytes(dtb);
			vBytes.add(b);
			size+=b.length;
		}
		vBytes.add(footer);
		size += footer.length;
		dt = new byte[size];
		for(byte[] b : vBytes)
		{
			System.arraycopy(b, 0, dt, pos, b.length);
			pos += b.length;
		}
		return dt;
	}
}
