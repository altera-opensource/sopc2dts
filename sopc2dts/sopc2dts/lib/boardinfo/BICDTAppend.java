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

public class BICDTAppend extends BoardInfoComponent {
	public enum DTAppendType { NODE, PROP_BOOL, PROP_NUMBER, PROP_STRING, PROP_HEX, PROP_BYTE };
	public static final String TAG_NAME = "DTAppend";
	String[] parentPath;
	String parentLabel;
	String value;
	DTAppendType type;
	String label;
	
	public BICDTAppend(String iName) {
		super(iName);
	}
	public BICDTAppend(String tag, Attributes atts) {
		super(tag, atts);
		setValue(atts.getValue("val"));
		setType(atts.getValue("type"));
		setPath(atts.getValue("parentpath"));
		setParentLabel(atts.getValue("parentlabel"));
		setLabel(atts.getValue("newlabel"));
	}
	public void setLabel(String l) {
		label = l;
	}
	public void setParentLabel(String pl) {
		parentLabel = pl;
	}
	private void setPath(String path) {
		if(path!=null) {
			parentPath = path.split("/");
		} else {
			parentPath = null;
		}
	}
	public void setValue(String val) {
		value = val;
	}
	public void setType(String t) {
		if(t != null) {
			if(t.equalsIgnoreCase("node")) {
				type = DTAppendType.NODE;
			} else if(t.equalsIgnoreCase("bool")) {
				type = DTAppendType.PROP_BOOL;
			} else if(t.equalsIgnoreCase("number")) {
				type = DTAppendType.PROP_NUMBER;
			} else if(t.equalsIgnoreCase("string")) {
				type = DTAppendType.PROP_STRING;
			} else if(t.equalsIgnoreCase("hex")) {
				type = DTAppendType.PROP_HEX;
			} else if(t.equalsIgnoreCase("byte")) {
				type = DTAppendType.PROP_BYTE;
			} else {
				type = null;
			}
		} else {
			type = null;
		}
	}
	@Override
	public String getXml() {
		String xml = '<' + TAG_NAME + " name=\"" + instanceName + "\" type=\"";
		if(type==null) {
			return "";
		}
		switch(type) {
		case NODE: 		xml += "node"; 	break;
		case PROP_BOOL:	xml += "bool"; 	break;
		case PROP_HEX:	xml += "hex";	break;
		case PROP_BYTE:	xml += "byte";	break;
		case PROP_NUMBER:	xml += "number";	break;
		case PROP_STRING:	xml += "string";	break;
		}
		xml += '\"';
		if(parentLabel!=null) {
			xml += " parentlabel=\"" + parentLabel + "\"";
		}
		if(parentPath!=null) {
			xml += " parentpath=\"" + parentPath[0];
			for(int i=1; i<parentPath.length; i++) {
				xml += '/' + parentPath[i];
			}
			xml += '\"';
		}
		if(label!=null) {
			xml += " newlabel=\"" + label + "\"";
		}
		if(value!=null) {
			xml += " val=\"" + value + "\"";
		}
		return xml + "/>\n";
	}
	public String[] getParentPath() {
		return parentPath;
	}
	public String getParentLabel() {
		return parentLabel;
	}
	public String getValue() {
		return value;
	}
	public DTAppendType getType() {
		return type;
	}
	public String getLabel() {
		return label;
	}

}
