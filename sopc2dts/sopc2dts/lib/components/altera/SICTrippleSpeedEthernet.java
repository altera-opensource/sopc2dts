package sopc2dts.lib.components.altera;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.base.SICEthernet;

public class SICTrippleSpeedEthernet extends SICEthernet {

	public SICTrippleSpeedEthernet(SopcComponentDescription scd, String iName, String version) {
		super(scd, iName, version);
	}

	@Override
	public String toDtsExtras(BoardInfo bi, int indentLevel, Connection conn, Boolean endComponent)
	{
		String res = "";
		BasicComponent compTX, compRX;
		compTX = getInterfaceByName("transmit").getConnections().get(0).getMasterModule();
		compRX = getInterfaceByName("receive").getConnections().get(0).getSlaveModule();
		res += AbstractSopcGenerator.indent(indentLevel) + "ALTR,sgdma_tx = <&" + compTX.getInstanceName() + ">;\n";
		res += AbstractSopcGenerator.indent(indentLevel) + "ALTR,sgdma_rx = <&" + compRX.getInstanceName() + ">;\n";
		return res;
	}
}
