package sopc2dts.lib.components.base;

import java.util.Arrays;
import java.util.HashMap;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.SopcComponentDescription;

public class SICI2CMaster extends BasicComponent {
	private static final long serialVersionUID = 8745845145232836596L;

	public SICI2CMaster(SopcComponentDescription scd, String iName, String ver) {
		super(scd, iName, ver);
	}
	@Override
	public String toDtsExtras(BoardInfo bi, int indentLevel, Connection conn, Boolean endComponent)
	{
		String res = "";
		HashMap<Integer, String> mI2C = bi.getI2CChipsForMaster(this.getInstanceName());
		if(mI2C != null)
		{
			if(!mI2C.isEmpty())
			{
				res += AbstractSopcGenerator.indent(indentLevel) + "#address-cells = <1>;\n" +
					AbstractSopcGenerator.indent(indentLevel) + "#size-cells = <0>;\n";
				Integer[] keys = new Integer[mI2C.size()];
				keys = mI2C.keySet().toArray(keys);
				Arrays.sort(keys);
				for(Integer key : keys)
				{
					res += AbstractSopcGenerator.indent(indentLevel++) + mI2C.get(key) + '@' + Integer.toHexString(key) + " {\n" +
							AbstractSopcGenerator.indent(indentLevel) + "compatible = \"" + mI2C.get(key) + "\";\n" +
							AbstractSopcGenerator.indent(indentLevel) + "reg = <0x" + Integer.toHexString(key) + ">;\n" +
							AbstractSopcGenerator.indent(--indentLevel) + "};\n";
				}
			}
		}
		return res;
	}

}
