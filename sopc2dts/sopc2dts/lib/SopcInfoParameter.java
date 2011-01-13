package sopc2dts.lib;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;


public class SopcInfoParameter extends SopcInfoAssignment {
	String type = null;
	Boolean derived;
	Boolean enabled;
	Boolean visible;
	Boolean valid;
	
	public SopcInfoParameter(ContentHandler p, XMLReader xr, String name)
	{
		super(p,xr);
		this.setName(name);
	}
	@Override
	public String getElementName() {
		return "parameter";
	}
	public void setValue(String val) {
		if(this.type!=null)
		{
			if(type.equalsIgnoreCase("int")||
					type.equalsIgnoreCase("long"))
			{
				if(getValue().endsWith("u") && (getValue().length()>1))
				{
					String tmpVal = getValue().substring(0, getValue().length()-1);
					if(tmpVal.equalsIgnoreCase(Long.decode(tmpVal).toString()))
					{
						val = tmpVal; 
					}
				}			
			}
		}
		super.setValue(val);
	}
}
