package sopc2dts.lib.components.base;

import java.util.Vector;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.SopcInfoConnection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.SopcInfoComponent;

public class SICFlash extends SopcInfoComponent {
	
	public SICFlash(ContentHandler p, XMLReader xr,
			SopcComponentDescription scd, String iName, String version) {
		super(p, xr, scd, iName, version);
	}

	@Override
	public String toDtsExtrasFirst(BoardInfo bi, int indentLevel, SopcInfoConnection conn, Boolean endComponent)
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
	public String toDtsExtras(BoardInfo bi, int indentLevel, SopcInfoConnection conn, Boolean endComponent)
	{
		int bankw = 2;
		try {
			bankw = Integer.decode(getParamValue("dataWidth"))/8;
		}catch(Exception e) { } //ignore
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
				res += AbstractSopcGenerator.indent(indentLevel++) + part.getName() + "@" + Integer.toHexString(part.getAddress()) + " {\n" +
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
