package sopc2dts.lib.components.altera;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.base.SICFlash;

public class SICEpcs extends SICFlash {
	
	public SICEpcs(SopcComponentDescription scd, String iName, String version) {
		super(scd, iName, version);
	}
	@Override
	public String toDtsExtrasFirst(BoardInfo bi, int indentLevel, 
			Connection conn, Boolean endComponent)
	{
		return AbstractSopcGenerator.indent(indentLevel) + "#address-cells = <1>;\n"
				+ AbstractSopcGenerator.indent(indentLevel) + "#size-cells = <0>;\n";
	}
	@Override
	public String toDtsExtras(BoardInfo bi, int indentLevel, 
			Connection conn, Boolean endComponent)
	{
		String res = "\n" + AbstractSopcGenerator.indent(indentLevel++) + "m25p80@0 {\n"
					+ super.toDtsExtrasFirst(bi, indentLevel, conn, endComponent)
					+ AbstractSopcGenerator.indent(indentLevel) + "compatible = \"m25p80\";\n"
					+ AbstractSopcGenerator.indent(indentLevel) + "reg = <0>;\n"
					+ partitionsForDts(bi, indentLevel);
		
		res += AbstractSopcGenerator.indent(--indentLevel) + "};\n";
		return res;
	}

	@Override
	protected int getAddrFromConnection(Connection conn)
	{
		//Yes this is REALLY ugly. But it just might work :)
		int regOffset;
		try {
			regOffset = Integer.decode(getParamValByName("embeddedsw.CMacro.REGISTER_OFFSET"));
		} catch(Exception e)
		{
			regOffset = 0;
		}
		return (conn==null ? getAddr() : conn.getConnValue()) + regOffset;
	}
	@Override
	protected int getSizeFromInterface(Interface intf)
	{
		return 8;
	}

}
