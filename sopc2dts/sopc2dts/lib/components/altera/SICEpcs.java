package sopc2dts.lib.components.altera;

import org.xml.sax.ContentHandler;

import org.xml.sax.XMLReader;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.SopcInfoConnection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.SopcInfoInterface;
import sopc2dts.lib.components.base.SICFlash;

public class SICEpcs extends SICFlash {
	
	public SICEpcs(ContentHandler p, XMLReader xr,
			SopcComponentDescription scd, String iName, String version) {
		super(p, xr, scd, iName, version);
	}
	@Override
	public String toDtsExtrasFirst(BoardInfo bi, int indentLevel, 
			SopcInfoConnection conn, Boolean endComponent)
	{
		return AbstractSopcGenerator.indent(indentLevel) + "#address-cells = <1>;\n"
				+ AbstractSopcGenerator.indent(indentLevel) + "#size-cells = <0>;\n";
	}
	@Override
	public String toDtsExtras(BoardInfo bi, int indentLevel, 
			SopcInfoConnection conn, Boolean endComponent)
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
	protected int getAddrFromConnection(SopcInfoConnection conn)
	{
		//Yes this is REALLY ugly. But it just might work :)
		int regOffset;
		try {
			regOffset = Integer.decode(getParamValue("embeddedsw.CMacro.REGISTER_OFFSET"));
		} catch(Exception e)
		{
			regOffset = 0;
		}
		return (conn==null ? getAddr() : conn.getBaseAddress()) + regOffset;
	}
	@Override
	protected int getSizeFromInterface(SopcInfoInterface intf)
	{
		return 8;
	}

}
