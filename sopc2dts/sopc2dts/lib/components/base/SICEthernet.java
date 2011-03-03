package sopc2dts.lib.components.base;

import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.BasicComponent;
/*
 * This class handles all default network stuff, subclasses can override 
 * defaults declared in this class, or just don't and keep 'm simple.
 * This is not really a special case (as are most classes in these packages) but
 * rather a convenience class to help the lazy people such as myself.
 */
public class SICEthernet extends BasicComponent {
	
	public SICEthernet(SopcComponentDescription scd, String iName, String version) {
		super(scd, iName, version);
	}

	@Override
	public String toDtsExtras(BoardInfo bi, int indentLevel, Connection conn, Boolean endComponent)
	{
		String res =  AbstractSopcGenerator.indent(indentLevel) + "address-bits = <" + getAddressBits() + ">;\n"
					+ AbstractSopcGenerator.indent(indentLevel) + "max-frame-size = <" + getMaxFrameSize() + ">;\n"
					+ AbstractSopcGenerator.indent(indentLevel) + "local-mac-address = [ ";
		int[] mac_address = getMacAddress();
		for(int i=0; i<mac_address.length; i++)
		{
			res += String.format("%02X ", mac_address[i]);
		}
		res += "];\n";
		return res;
	}
	protected int[] getMacAddress()
	{
		return new int[]{ 0, 0, 0, 0, 0, 0 };
	}
	protected int getAddressBits()
	{
		return 48;
	}
	protected int getMaxFrameSize()
	{
		return 1518;
	}
}
