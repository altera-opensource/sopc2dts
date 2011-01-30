package sopc2dts.lib;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


public abstract class SopcInfoElementWithParams extends SopcInfoElement {
	Vector<SopcInfoAssignment> vParams = new Vector<SopcInfoAssignment>();

	public SopcInfoElementWithParams(ContentHandler p, XMLReader xr) {
		super(p, xr);
		// TODO Auto-generated constructor stub
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
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if(localName.equalsIgnoreCase("assignment"))
		{
			vParams.add(new SopcInfoAssignment(this, xmlReader));
		} else if(localName.equalsIgnoreCase("parameter"))
		{
			vParams.add(new SopcInfoParameter(this, xmlReader, atts.getValue("name")));
		}
	}

}
