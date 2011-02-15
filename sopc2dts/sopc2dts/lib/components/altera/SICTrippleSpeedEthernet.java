package sopc2dts.lib.components.altera;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.SopcInfoConnection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.SopcInfoComponent;
import sopc2dts.lib.components.base.SICEthernet;

public class SICTrippleSpeedEthernet extends SICEthernet {

	public SICTrippleSpeedEthernet(ContentHandler p, XMLReader xr,
			SopcComponentDescription scd, String iName, String version) {
		super(p, xr, scd, iName, version);
	}

	@Override
	public String toDtsExtras(BoardInfo bi, int indentLevel, SopcInfoConnection conn, Boolean endComponent)
	{
		String res = "";
		SopcInfoComponent compTX, compRX;
		compTX = getInterfaceByName("transmit").getConnections().get(0).getMasterModule();
		compRX = getInterfaceByName("receive").getConnections().get(0).getSlaveModule();
		res += AbstractSopcGenerator.indent(indentLevel) + "ALTR,sgdma_tx = < &" + compTX.getInstanceName() + " >;\n";
		res += AbstractSopcGenerator.indent(indentLevel) + "ALTR,sgdma_rx = < &" + compRX.getInstanceName() + " >;\n";
		return res;
	}
}
