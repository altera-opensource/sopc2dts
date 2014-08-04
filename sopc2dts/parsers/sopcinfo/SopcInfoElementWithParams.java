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

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import sopc2dts.lib.BasicElement;



public abstract class SopcInfoElementWithParams extends SopcInfoElement {
	Vector<SopcInfoAssignment> vParams = new Vector<SopcInfoAssignment>();
	BasicElement basicElement;
	public SopcInfoElementWithParams(ContentHandler p, XMLReader xr, BasicElement be) {
		super(p, xr);
		basicElement = be;
	}
	public String getParamValue(String paramName)
	{
		for(SopcInfoAssignment ass : vParams)
		{
			if(ass.getName().equalsIgnoreCase(paramName))
			{
				return ass.getValue();
			}
		}
		return null;
	}

	public SopcInfoAssignment getParam(String paramName)
	{
		for(SopcInfoAssignment ass : vParams)
		{
			if(ass.getName().equalsIgnoreCase(paramName))
			{
				return ass;
			}
		}
		return null;
	}
	public Vector<SopcInfoAssignment> getParams()
	{
		return vParams;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if(localName.equalsIgnoreCase("assignment"))
		{
			vParams.add(new SopcInfoAssignment(this, xmlReader,basicElement));
		} else if(localName.equalsIgnoreCase("parameter"))
		{
			vParams.add(new SopcInfoParameter(this, xmlReader, atts.getValue("name"), basicElement));
		}
	}

}
