package sopc2dts.lib.components.altera;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.BasicComponent;

public class SICSgdma extends BasicComponent {
	public static final String[] TYPE_NAMES = {
		"MEMORY_TO_MEMORY",
		"MEMORY_TO_STREAM",
		"STREAM_TO_MEMORY",
		"STREAM_TO_STREAM",
		"UNKNOWN"
	};
	
	public SICSgdma(SopcComponentDescription scd, String iName, String version) {
		super(scd, iName, version);
	}

	@Override
	public String toDtsExtras(BoardInfo bi, int indentLevel, Connection conn, Boolean endComponent)
	{
		String res = "";
		int iType = 0;
		while(iType<(TYPE_NAMES.length-1))
		{
			if(TYPE_NAMES[iType].equals(getParamValByName("transferMode")))
			{
				break;
			}
			iType++;
		}
		res += AbstractSopcGenerator.indent(indentLevel) + "type = < " + iType + " >; //" + TYPE_NAMES[iType] + "\n";
		return res;
	}
}
