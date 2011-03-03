package sopc2dts.lib.components.base;

import java.util.Vector;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.BasicComponent;

public class SICFlash extends BasicComponent {
	
	public SICFlash(SopcComponentDescription scd, String iName, String version) {
		super(scd, iName, version);
	}

	@Override
	public String toDtsExtrasFirst(BoardInfo bi, int indentLevel, Connection conn, Boolean endComponent)
	{
		String res = "";
		Vector<FlashPartition> vPartitions = bi.getPartitionsForChip(this.getInstanceName());
		if(vPartitions != null)
		{
			if(vPartitions.size()>0)
			{
				res += AbstractSopcGenerator.indent(indentLevel) + "#address-cells = <1>;\n" +
					AbstractSopcGenerator.indent(indentLevel) + "#size-cells = <1>;\n";
			}
		}
		return res;
	}
	
	@Override
	public String toDtsExtras(BoardInfo bi, int indentLevel, Connection conn, Boolean endComponent)
	{
		int bankw;
		try {
			bankw = Integer.decode(getParamValByName("dataWidth"))/8;
		}catch(Exception e) {
			//Default to 16bit on failure
			bankw = 2;
		}
		String res = AbstractSopcGenerator.indent(indentLevel) + "bank-width = <"+bankw+">;\n" +
					AbstractSopcGenerator.indent(indentLevel) + "device-width = <1>;\n" +
					partitionsForDts(bi, indentLevel);

		return res;	
	}
	protected String partitionsForDts(BoardInfo bi, int indentLevel)
	{
		String res = "";
		Vector<FlashPartition> vPartitions = bi.getPartitionsForChip(this.getInstanceName());
		if(vPartitions != null)
		{
			for(FlashPartition part : vPartitions)
			{
				res += AbstractSopcGenerator.indent(indentLevel++) + part.getName() + '@' + Integer.toHexString(part.getAddress()) + " {\n" +
						AbstractSopcGenerator.indent(indentLevel) + String.format("reg = < 0x%08X 0x%08X >;\n", part.getAddress(),part.getSize());
				if(part.isReadonly())
				{
					res += AbstractSopcGenerator.indent(indentLevel) + "read-only;\n";
				}
				res += AbstractSopcGenerator.indent(--indentLevel) + "};\n";
			}
		}
		return res;
	}
}
