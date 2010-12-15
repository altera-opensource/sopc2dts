package sopc2dts.lib.components.base;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.SopcInfoConnection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.SopcInfoComponent;
import sopc2dts.lib.components.SopcInfoInterface;


public class SICBridge extends SopcInfoComponent {

	public SICBridge(ContentHandler p, XMLReader xr,
			SopcComponentDescription scd, String iName) {
		super(p, xr, scd, iName);
//		vAutoParams.add(new SICAutoParam("clock-frequency", ""));
	}
	protected String getDtsRanges(int indentLevel, SopcInfoConnection conn)
	{
		String res = "";
		for(SopcInfoInterface master : getInterfaces())
		{
			if(master.isMemoryMaster())
			{
				for(SopcInfoConnection childConn : master.getMasterConnections())
				{
					int size = 0;
					SopcInfoInterface childIf=childConn.getEndInterface();
					if(childIf!=null) size = childIf.getAddressableSize();
					if(res == "")
					{
						res = AbstractSopcGenerator.indent(indentLevel) + "ranges = <"; 
					} else {
						res += "\n" + AbstractSopcGenerator.indent(indentLevel) + "\t";
					}					
					res += String.format("0x%08X 0x%08X 0x%08X", childConn.getBaseAddress(),
							childConn.getBaseAddress() + conn.getBaseAddress(), size);
				}
			}
		}
		if(res=="")
		{
			res = AbstractSopcGenerator.indent(indentLevel) + "ranges;\n";
		} else {
			res += ">;\n";
		}
		return res;
	}
	public String toDtsExtras(int indentLevel, SopcInfoConnection conn, Boolean endComponent)
	{
		return AbstractSopcGenerator.indent(indentLevel) + "#address-cells = <1>;\n" +
				AbstractSopcGenerator.indent(indentLevel) + "#size-cells = <1>;\n" +
				getDtsRanges(indentLevel,conn);
	}
}
