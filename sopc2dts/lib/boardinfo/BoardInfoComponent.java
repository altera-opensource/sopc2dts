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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public abstract class BoardInfoComponent implements ContentHandler {
	String instanceName;
	String xmlTagName;

	protected BoardInfoComponent(String tag, String iName)
	{
		instanceName = iName;
		xmlTagName = tag;
	}
	public BoardInfoComponent(String tag, Attributes atts)
	{
		this(tag,atts.getValue("name"));
	}
	public abstract String getXml();

	public static BoardInfoComponent getBicFor(String localName, Attributes atts)
	{
		BoardInfoComponent bic = null;
		if(localName.equalsIgnoreCase(BICEthernet.TAG_NAME))
		{
			bic = new BICEthernet(localName,atts);
		} else if(localName.equalsIgnoreCase(BICSpi.TAG_NAME))
		{
			bic = new BICSpi(localName, atts);
		} else if(localName.equalsIgnoreCase(BICDTAppend.TAG_NAME))
		{
			bic = new BICDTAppend(localName, atts);
		} else if(localName.equalsIgnoreCase(BICI2C.TAG_NAME))
		{
			bic = new BICI2C(localName, atts);
		}
		return bic;
	}
	/* Define some ContentHandler functions that are usually not needed when 
	 * parsing boardinfo files, so subclasses don't need to.
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
	}
	public void characters(char[] ch, int start, int length)
			throws SAXException {
	}
	public void endElement(String uri, String localName, String qName)
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

	public String getInstanceName() {
		return instanceName;
	}

	public String getXmlTagName() {
		return xmlTagName;
	}

}
