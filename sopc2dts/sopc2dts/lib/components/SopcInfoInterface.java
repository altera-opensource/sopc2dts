package sopc2dts.lib.components;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import sopc2dts.lib.SopcInfoAssignment;
import sopc2dts.lib.SopcInfoConnection;
import sopc2dts.lib.SopcInfoElementWithParams;


public class SopcInfoInterface extends SopcInfoElementWithParams {
	private String name;
	private String kind;
	private Vector<SopcInfoConnection> vMasterConnections = new Vector<SopcInfoConnection>();
	private Vector<SopcInfoConnection> vSlaveConnections = new Vector<SopcInfoConnection>();
	private Vector<SopcInfoMemoryBlock> vMemoryMap = new Vector<SopcInfoMemoryBlock>();

	public SopcInfoInterface(ContentHandler p, XMLReader xr, String iName, String kind) {
		super(p, xr);
		this.setName(iName);
		this.kind = kind;
	}

	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if(localName.equalsIgnoreCase("memoryBlock"))
		{
			getMemoryMap().add(new SopcInfoMemoryBlock(this, xmlReader));
		} else {
			super.startElement(uri, localName, qName, atts);
		}
		
	}

	public int getAddressableSize()
	{
		int size = -1;
		SopcInfoAssignment ass = getParamValue("addressSpan");
		if(ass!=null)
		{
			if(ass.getValue()!=null) 
			{
				size = Integer.decode(ass.getValue());
			}
		}
		return size;
	}
	public Boolean isMemory()
	{
		return isMemoryMaster() || isMemorySlave();
	}
	public Boolean isMemoryMaster()
	{
		return getKind().equalsIgnoreCase("avalon_master")|| getKind().equalsIgnoreCase("avalon_tristate_master");
	}
	public Boolean isMemorySlave()
	{
		return getKind().equalsIgnoreCase("avalon_slave")|| getKind().equalsIgnoreCase("avalon_tristate_slave");
	}
	public Boolean isClockInput()
	{
		return kind.equalsIgnoreCase("clock_sink");
	}
	@Override
	public String getElementName() {
		return "interface";
	}

	public Vector<SopcInfoConnection> getMasterConnections() {
		return vMasterConnections;
	}

	public String getKind() {
		return kind;
	}

	public void setvSlaveConnections(Vector<SopcInfoConnection> vSlaveConnections) {
		this.vSlaveConnections = vSlaveConnections;
	}

	public Vector<SopcInfoConnection> getvSlaveConnections() {
		return vSlaveConnections;
	}

	public void setMemoryMap(Vector<SopcInfoMemoryBlock> vMemoryMap) {
		this.vMemoryMap = vMemoryMap;
	}

	public Vector<SopcInfoMemoryBlock> getMemoryMap() {
		return vMemoryMap;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
