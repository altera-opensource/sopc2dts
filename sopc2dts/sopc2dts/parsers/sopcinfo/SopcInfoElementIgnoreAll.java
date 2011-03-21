/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 Walter Goossens <waltergoossens@home.nl>

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
package sopc2dts.parsers.sopcinfo;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;



public class SopcInfoElementIgnoreAll extends SopcInfoElement {
	String elementName;
	public SopcInfoElementIgnoreAll(ContentHandler p, XMLReader xr, String name) {
		super(p, xr);
		elementName = name;
	}
	@Override
	public String getElementName() {
		// TODO Auto-generated method stub
		return elementName;
	}
	
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
	}

}
