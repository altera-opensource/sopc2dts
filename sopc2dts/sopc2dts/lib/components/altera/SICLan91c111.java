package sopc2dts.lib.components.altera;

import sopc2dts.lib.Connection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.base.SICEthernet;

public class SICLan91c111 extends SICEthernet {

	public SICLan91c111(SopcComponentDescription scd, String iName, String version) {
		super(scd, iName, version);
	}

	@Override
	protected int getAddrFromConnection(Connection conn)
	{
		//Yes this is REALLY ugly. But it just might work :)
		int regOffset;
		try {
			regOffset = Integer.decode(getParamValByName("registerOffset"));
		} catch(Exception e)
		{
			regOffset = 0;
		}
		return (conn==null ? getAddr() : conn.getConnValue()) + regOffset;
	}
	@Override
	protected int getSizeFromInterface(Interface intf)
	{
		return 0x100;
	}
}
