package sopc2dts.lib;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;


public class SopcInfoParameter extends SopcInfoAssignment {
	String type;
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
}
