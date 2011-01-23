package sopc2dts.lib.components.altera;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.SopcInfoConnection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.SopcInfoComponent;

public class SICTrippleSpeedEthernet extends SopcInfoComponent {

	public SICTrippleSpeedEthernet(ContentHandler p, XMLReader xr,
			SopcComponentDescription scd, String iName, String version) {
		super(p, xr, scd, iName, version);
	}
	public String toDtsExtras(int indentLevel, SopcInfoConnection conn, Boolean endComponent)
	{
		String res = "";
		SopcInfoComponent compTX, compRX;
		compTX = getInterfaceByName("transmit").getConnections().get(0).getMasterInterface().getOwner();
		compRX = getInterfaceByName("receive").getConnections().get(0).getSlaveInterface().getOwner();
		res += AbstractSopcGenerator.indent(indentLevel) + "altr,sgdma_tx = < &" + compTX.getInstanceName() + " >;\n";
		res += AbstractSopcGenerator.indent(indentLevel) + "altr,sgdma_rx = < &" + compRX.getInstanceName() + " >;\n";
		return res;
	}
}
