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
