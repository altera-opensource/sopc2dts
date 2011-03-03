package sopc2dts.parsers.sopcinfo;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import sopc2dts.lib.BasicElement;
import sopc2dts.lib.Parameter.DataType;


public class SopcInfoParameter extends SopcInfoAssignment {
	String type;
	Boolean derived;
	Boolean enabled;
	Boolean visible;
	Boolean valid;
	
	public SopcInfoParameter(ContentHandler p, XMLReader xr, String name, BasicElement be)
	{
		super(p,xr,be);
		this.name = name;
	}
	@Override
	public String getElementName() {
		return "parameter";
	}
	public boolean isForDts()
	{
		return (valid && enabled && ((dataType != DataType.BOOLEAN)||(value.equalsIgnoreCase("true"))));
	}
	protected String parseValue(String val)
	{
		if(this.type==null)
		{
			return super.parseValue(val);
		} else {
			if(type.equalsIgnoreCase("int")||
					type.equalsIgnoreCase("long"))
			{
				if(val.endsWith("u") && (val.length()>1))
				{
					String tmpVal = val.substring(0, val.length()-1);
					if(tmpVal.equalsIgnoreCase(Long.decode(tmpVal).toString()))
					{
						val = tmpVal; 
					}
				}
				dataType = DataType.NUMBER;
			} else if(type.equalsIgnoreCase("boolean"))
			{
				dataType = DataType.BOOLEAN;
			}
		}
		return val;
	}
	public void characters(char[] ch, int start, int length)
	throws SAXException {
		if(currTag!=null)
		{
			if(currTag.equalsIgnoreCase("type"))
			{
				type = String.copyValueOf(ch, start, length);
			} else if(currTag.equalsIgnoreCase("derived"))
			{
				derived = Boolean.parseBoolean(
						String.copyValueOf(ch, start, length));
			} else if(currTag.equalsIgnoreCase("enabled"))
			{
				enabled = Boolean.parseBoolean(
						String.copyValueOf(ch, start, length));
			} else if(currTag.equalsIgnoreCase("visible"))
			{
				visible = Boolean.parseBoolean(
						String.copyValueOf(ch, start, length));
			} else if(currTag.equalsIgnoreCase("valid"))
			{
				valid = Boolean.parseBoolean(
						String.copyValueOf(ch, start, length));
			} else {
				super.characters(ch, start, length);
			}
		}
	}

}
