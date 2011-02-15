package sopc2dts.lib.components.altera;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import sopc2dts.lib.SopcInfoConnection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.SopcInfoInterface;
import sopc2dts.lib.components.base.SICEthernet;

public class SICLan91c111 extends SICEthernet {

	public SICLan91c111(ContentHandler p, XMLReader xr,
			SopcComponentDescription scd, String iName, String version) {
		super(p, xr, scd, iName, version);
	}

	@Override
	protected int getAddrFromConnection(SopcInfoConnection conn)
	{
		//Yes this is REALLY ugly. But it just might work :)
		int regOffset = 0;
		try {
			regOffset = Integer.decode(getParamValue("registerOffset"));
		} catch(Exception e)
		{
			//Ignore errors and keep regOffset at 0
		}
		return (conn==null ? getAddr() : conn.getBaseAddress()) + regOffset;
	}
	@Override
	protected int getSizeFromInterface(SopcInfoInterface intf)
	{
		return 0x100;
	}
}
