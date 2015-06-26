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

public class BICI2C extends BoardInfoComponent {
	public static final String TAG_NAME = "I2CBus";
	Vector<I2CSlave> vSlaves = new Vector<I2CSlave>();

	public BICI2C(String iName) {
		super(TAG_NAME, iName);
	}
	public BICI2C(String tag, Attributes atts) {
		super(tag, atts);
		instanceName = atts.getValue("master");
	}
	public Vector<I2CSlave> getSlaves() {
		return vSlaves;
	}
	@Override
	public String getXml() {
		String xml = "\t<" + TAG_NAME;
		if(instanceName!=null) {
			xml += " master=\"" + instanceName + "\"";
		}
		xml += ">\n";
		for(I2CSlave s : vSlaves) {
			xml += s.getXml();
		}
		xml += "\t</" + TAG_NAME + ">\n";
		return xml;
	}
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if(localName.equals(I2CSlave.TAG_NAME))
		{
			vSlaves.add(new I2CSlave(atts));
		}
	}
	public void setSlaves(Vector<I2CSlave> vs) {
		vSlaves = vs;
	}
}
