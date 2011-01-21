package sopc2dts.lib.components.altera;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.SopcInfoConnection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.SopcInfoComponent;

public class SICSgdma extends SopcInfoComponent {
	public static final String[] TYPE_NAMES = {
		"MEMORY_TO_MEMORY",
		"MEMORY_TO_STREAM",
		"STREAM_TO_MEMORY",
		"STREAM_TO_STREAM",
		"UNKNOWN"
	};
	
	public SICSgdma(ContentHandler p, XMLReader xr,
			SopcComponentDescription scd, String iName, String version) {
		super(p, xr, scd, iName, version);
	}
	public String toDtsExtras(int indentLevel, SopcInfoConnection conn, Boolean endComponent)
	{
		String res = "";
		int iType = 0;
		while(iType<(TYPE_NAMES.length-1))
		{
			if(TYPE_NAMES[iType].equals(getParamValue("transferMode")))
			{
				break;
			}
			iType++;
		}
		//SopcInfoComponent compTX, compRX;
		//compTX = getInterfaceByName("transmit").getConnections().get(0).getMasterInterface().getOwner();
		//compRX = getInterfaceByName("receive").getConnections().get(0).getSlaveInterface().getOwner();
//		res += AbstractSopcGenerator.indent(indentLevel) + "altera,sgdma_tx = < &" + compTX.getInstanceName() + " >;\n";
//		res += AbstractSopcGenerator.indent(indentLevel) + "altera,sgdma_rx = < &" + compRX.getInstanceName() + " >;\n";
		res += AbstractSopcGenerator.indent(indentLevel) + "type = < " + iType + " >; //" + TYPE_NAMES[iType] + "\n";
		return res;
	}
}
