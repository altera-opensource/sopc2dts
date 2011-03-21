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
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


public abstract class SopcInfoElement implements ContentHandler {
	protected ContentHandler parentElement = null;
	protected XMLReader xmlReader = null;
	
	public SopcInfoElement(ContentHandler p, XMLReader xr)
	{
		parentElement = p;
		xmlReader = xr;
		xmlReader.setContentHandler(this);
	}
	public abstract String getElementName();
	
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		System.out.println("Unhandled SopcInfo element: " + localName);
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(localName.equalsIgnoreCase(getElementName()))
		{
			if((xmlReader!=null)&&(parentElement!=null))
			{
				xmlReader.setContentHandler(parentElement);
			}
		}
	}


	public void characters(char[] ch, int start, int length)
			throws SAXException {
	}

	public void endDocument() throws SAXException {
	}

	public void endPrefixMapping(String prefix) throws SAXException {
	}

	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
	}

	public void processingInstruction(String target, String data)
			throws SAXException {
	}

	public void setDocumentLocator(Locator locator) {
	}

	public void skippedEntity(String name) throws SAXException {
	}

	public void startDocument() throws SAXException {
	}

	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
	}

}
