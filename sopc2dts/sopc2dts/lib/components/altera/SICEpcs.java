package sopc2dts.lib.components.altera;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.SopcInfoConnection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.SopcInfoComponent;
import sopc2dts.lib.components.SopcInfoInterface;

public class SICEpcs extends SopcInfoComponent {
	
	public SICEpcs(ContentHandler p, XMLReader xr,
			SopcComponentDescription scd, String iName, String version) {
		super(p, xr, scd, iName, version);
	}
	public String toDtsExtrasFirst(int indentLevel, SopcInfoConnection conn, Boolean endComponent)
	{
		return AbstractSopcGenerator.indent(indentLevel) + "#address-cells = <1>;\n"
				+ AbstractSopcGenerator.indent(indentLevel) + "#size-cells = <0>;\n";
	}
	public String toDtsExtras(int indentLevel, SopcInfoConnection conn, Boolean endComponent)
	{
		String res = "\n" + AbstractSopcGenerator.indent(indentLevel++) + "m25p80@0 {\n"
					+ AbstractSopcGenerator.indent(indentLevel) + "compatible = \"m25p80\";\n"
					+ AbstractSopcGenerator.indent(indentLevel) + "reg = <0>;\n";
					
		res += AbstractSopcGenerator.indent(--indentLevel) + "};\n";
		return res;
	}
	protected String getRegForDTS(int indentLevel, SopcInfoConnection conn, SopcInfoInterface intf)
	{
		if((conn!=null) && (intf!=null))
		{
			return AbstractSopcGenerator.indent(indentLevel) + "reg = <0x" + Integer.toHexString(getAddrFromConnection(conn)) + 
					" 0x8>;\n";
		} else {
			return "";
		}
	}

	protected int getAddrFromConnection(SopcInfoConnection conn)
	{
		//Yes this is REALLY ugly. But it just might work :)
		int regOffset = 0;
		try {
			regOffset = Integer.decode(getParamValue("embeddedsw.CMacro.REGISTER_OFFSET"));
		} catch(Exception e)
		{
			//Ignore errors and keep regOffset at 0
		}
		return (conn==null ? getAddr() : conn.getBaseAddress()) + regOffset;
	}
}
