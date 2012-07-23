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

public abstract class DTElement {
	String label;
	String name;
	String comment;

	public DTElement(String name)
	{
		this(name,null,null);
	}
	public DTElement(String name, String label)
	{
		this(name,label,null);
	}
	public DTElement(String name, String label, String comment)
	{
		this.name = name;
		this.label = label;
		this.comment = comment;
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
	public abstract byte[] getBytes(DTBlob dtb);
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public abstract String toString(int indent);
	public String toString() {
		return toString(0);
	}
	/** @brief Helper function to do indentation
	 * 
	 * This is a helper function for generating formatted text. It generates a 
	 * string that can be prepended to a source string consisting of level
	 * indentation characters.
	 * 
	 * @param level The indentation depth
	 * @return A String containing indentation characters
	 */
	protected static String indent(int level)
	{
		String res = "";
		while(level-->0)
		{
			res += "\t";
		}
		return res;
	}
}
