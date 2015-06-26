/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 - 2015 Walter Goossens <waltergoossens@home.nl>

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

public class BICSpi extends BoardInfoComponent {
	public static final String TAG_NAME = "SpiMaster";
	Vector<SpiSlave> vSlaves = new Vector<SpiSlave>();
	public BICSpi(String iName) {
		super(TAG_NAME, iName);
	}
	public BICSpi(String tag, Attributes atts) {
		super(tag, atts);
	}

	@Override
	public String getXml() {
		String res = "\t<" + TAG_NAME + " name=\"" + instanceName + "\">\n";
		for(SpiSlave slave : vSlaves)
		{
			res += slave.getXml();
		}
		res += "\t</" + TAG_NAME + ">\n";
		return res;
	}

	public Vector<SpiSlave> getSlaves() {
		return vSlaves;
	}
	public void setSlaves(Vector<SpiSlave> vSlaves) {
		this.vSlaves = vSlaves;
	}
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if(localName.equals("SpiSlave"))
		{
			String className = atts.getValue("class");
			if(className!=null) {
				if(className.equals(SpiSlaveMMC.class.getCanonicalName()))
				{
					vSlaves.add(new SpiSlaveMMC(atts));
				} else {
					vSlaves.add(new SpiSlave(atts));					
				}
			} else {
				vSlaves.add(new SpiSlave(atts));
			}
		}
	}

}
