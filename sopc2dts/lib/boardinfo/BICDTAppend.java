/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2013 - 2015 Walter Goossens <waltergoossens@home.nl>

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

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;

public class BICDTAppend extends BoardInfoComponent {
	public enum DTAppendType { NODE, PROP_BOOL, PROP_NUMBER, PROP_STRING, PROP_HEX, PROP_BYTE, PROP_PHANDLE };
	public enum DTAppendAction { ADD, REPLACE, REMOVE };
	public static final String TAG_NAME = "DTAppend";
	private static final String VAL_TAG = "val";
	String[] parentPath;
	String parentLabel;
	Vector<String> vValues = new Vector<String>();
	Vector<DTAppendType> vTypes = new Vector<DTAppendType>();
	
	DTAppendAction action = DTAppendAction.REPLACE;
	String label;
	String valBuf;
	private Boolean valSeen = false;
	
	public BICDTAppend(String iName) {
		super(TAG_NAME, iName);
	}
	public BICDTAppend(String tag, Attributes atts) {
		super(tag, atts);
		addValue(atts.getValue("val"));
		addType(atts.getValue("type"));
		setPath(atts.getValue("parentpath"));
		setParentLabel(atts.getValue("parentlabel"));
		setLabel(atts.getValue("newlabel"));
		setAction(atts.getValue("action"));
	}
	public void setLabel(String l) {
		label = l;
	}
	public void setParentLabel(String pl) {
		parentLabel = pl;
	}
	private void setPath(String path) {
		if(path!=null) {
			if(path.startsWith("/")) {
				path = path.substring(1);
			}
			parentPath = path.split("/");
		} else {
			parentPath = null;
		}
	}
	public void addValue(String val) {
		if (null != val) {
			vValues.add(val);
		}
	}
	private void addType(String t) {
		if(t != null) {
			if(t.equalsIgnoreCase("node")) {
				vTypes.add(DTAppendType.NODE);
			} else if(t.equalsIgnoreCase("number")) {
				vTypes.add(DTAppendType.PROP_NUMBER);
			} else if(t.equalsIgnoreCase("string")) {
				vTypes.add(DTAppendType.PROP_STRING);
			} else if(t.equalsIgnoreCase("hex")) {
				vTypes.add(DTAppendType.PROP_HEX);
			} else if(t.equalsIgnoreCase("byte")) {
				vTypes.add(DTAppendType.PROP_BYTE);
			} else if (t.equalsIgnoreCase("phandle")) {
				vTypes.add(DTAppendType.PROP_PHANDLE);
			} else if(!t.equalsIgnoreCase("bool")) {
				Logger.logln("BICDTAppend.setType unknown type: "+t, LogLevel.ERROR);
			}
		}
	}
	private String actionToString(DTAppendAction a) {
		return a.toString().toLowerCase();
	}
	private String typeToString(DTAppendType t) {
		switch(t) {
		case NODE:              return "node";
		case PROP_BOOL: return "bool";
		case PROP_HEX:  return "hex";
		case PROP_BYTE: return "byte";
		case PROP_NUMBER:       return "number";
		case PROP_STRING:       return "string";
		case PROP_PHANDLE:  return "phandle";
		default: return "";
		}
	}
	@Override
	public String getXml() {
		String xml = '<' + TAG_NAME + " name=\"" + instanceName + "\" ";
		if (vTypes.size() == 1) {
			xml += "type=\"";

			xml += typeToString(vTypes.get(0));

			xml += '\"';
		}
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
		xml += " action=\"" + actionToString(action) + "\"";
		switch(vValues.size()) {
		case 1:
			String value = vValues.get(0);
			if(value!=null) {
				xml += " val=\"" + value + "\"";
			}
			xml += "/>\n";
			break;
			
		case 0: 
			xml += "/>\n";
			break;
			
		default:
			xml += ">\n";
			for (int i = 0; i < vValues.size(); i++) {
				if (i < vTypes.size()) {
					xml += "    <val type=\"" + typeToString(vTypes.get(i))+"\">"+vValues.get(i)+"</val>\n";
				} else {
					xml += "    <val>"+vValues.get(i)+"</val>\n";
				}
				
			}
			xml += "</" + TAG_NAME + ">\n";
			break;
		}
		return xml;
	}
	public DTAppendAction getAction() {
		return action;
	}
	public String[] getParentPath() {
		return parentPath;
	}
	public String getParentLabel() {
		return parentLabel;
	}
	public Vector<String> getValues() {
		return vValues;
	}
	public Vector<DTAppendType> getTypes() {
		return vTypes;
	}
	public String getLabel() {
		return label;
	}
	public void setAction(DTAppendAction action) {
		this.action = action;
	}
	public void setAction(String actionStr) {
		if(actionStr != null) {
			if(DTAppendAction.ADD.toString().equalsIgnoreCase(actionStr)) {
				setAction(DTAppendAction.ADD);
			} else if(DTAppendAction.REPLACE.toString().equalsIgnoreCase(actionStr)) {
				setAction(DTAppendAction.REPLACE);
			} else if(DTAppendAction.REMOVE.toString().equalsIgnoreCase(actionStr)) {
				setAction(DTAppendAction.REMOVE);
			} else {
				Logger.logln(this, "Unsupported DTAppend action '" + actionStr +"'.", LogLevel.ERROR);
			}
		}
	}
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if(localName.equals(VAL_TAG)) {
			valBuf = "";
			valSeen = true;
			String t = atts.getValue("type");
			if (t != null) {
				addType(t);
			} else if (vTypes.size() == 0) {
				Logger.logln("unspecified type for val tag found by BICDTAppend", LogLevel.ERROR);
			}
			else {
				vTypes.add(vTypes.lastElement());
			}
		} else {
			Logger.logln("unexpected start tag, "+localName+" found by BICDTAppend", LogLevel.ERROR);
		}
	}
	public void characters(char[] ch, int start, int length)
	throws SAXException {
		if (valSeen) {
			valBuf += String.copyValueOf(ch, start, length);
		}
	}
	public void endElement(String uri, String localName, String qName)
	throws SAXException {
		if (localName.equals(VAL_TAG)) {
			addValue(valBuf);
			valSeen = false;
		} else if (!localName.equals(TAG_NAME)){
			Logger.logln("unexpected end tag, "+localName+" found by BICDTAppend", LogLevel.ERROR);
		} else if (vTypes.size() == 0) {
			vValues.removeAllElements(); // No types implies a boolean which has no data either
		}
	}
}
