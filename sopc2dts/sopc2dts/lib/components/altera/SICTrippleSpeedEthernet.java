package sopc2dts.lib.components.altera;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.base.SICEthernet;

public class SICTrippleSpeedEthernet extends SICEthernet {

	public SICTrippleSpeedEthernet(SopcComponentDescription scd, String iName, String version) {
		super(scd, iName, version);
	}

	private String getSGDMAEngine(int indentLevel, Interface intf, String dtsEntry)
	{
		String res = "";
		if((intf!=null) && (intf.getConnections().size()>0))
		{
			BasicComponent comp = null;
			if(intf.isMaster())
			{
				comp = intf.getConnections().firstElement().getSlaveModule();
			} else {
				comp = intf.getConnections().firstElement().getMasterModule();
			}
			if(comp != null)
			{
				res += AbstractSopcGenerator.indent(indentLevel) + dtsEntry + 
									" = <&" + comp.getInstanceName() + ">;\n";
			}
		}
		if(res.length()==0)
		{
			res = AbstractSopcGenerator.indent(indentLevel) + 
					"//Port " + intf.getName() + " seems not connected\n";
		}
		return res;
	}
	@Override
	public String toDtsExtras(BoardInfo bi, int indentLevel, Connection conn, Boolean endComponent)
	{
		return getSGDMAEngine(indentLevel, getInterfaceByName("transmit"), "ALTR,sgdma_tx") +
				getSGDMAEngine(indentLevel, getInterfaceByName("receive"), "ALTR,sgdma_rx");
	}
}
