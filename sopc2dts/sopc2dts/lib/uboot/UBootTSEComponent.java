package sopc2dts.lib.uboot;

import java.util.HashMap;

import sopc2dts.lib.components.BasicComponent;

public class UBootTSEComponent extends UBootLibComponent {

	protected UBootTSEComponent() {
		super(new String[] { "triple_speed_ethernet" }, null, 
				"#define CONFIG_ALTERA_TSE\n" +
				"#define CONFIG_MII\n" +
				"#define CONFIG_CMD_MII\n" +
				"#define CONFIG_SYS_ALTERA_TSE_PHY_ADDR 1\n" +
				"#define CONFIG_SYS_ALTERA_TSE_FLAGS 0\n");
		propertyDefines = new HashMap<String, String>();
		propertyDefines.put("CONFIG_SYS_ALTERA_TSE_MAC_BASE","gen|addr");
		propertyDefines.put("CONFIG_SYS_ALTERA_TSE_RX_FIFO", "prop|embeddedsw.CMacro.RECEIVE_FIFO_DEPTH");
		propertyDefines.put("CONFIG_SYS_ALTERA_TSE_TX_FIFO", "prop|embeddedsw.CMacro.TRANSMIT_FIFO_DEPTH");
	}
	public String getHeadersFor(BasicComponent comp, int ifNum, long addrOffset)
	{
		if(comp.getInterfaces().get(ifNum).getName().equalsIgnoreCase("control_port"))
		{
			return super.getHeadersFor(comp, ifNum, addrOffset);
		} else if(comp.getInterfaces().get(ifNum).getName().equalsIgnoreCase("rx_csr"))
		{
			return String.format("#define CONFIG_SYS_ALTERA_TSE_SGDMA_RX_BASE\t0x%08X\n",
					comp.getInterfaces().get(ifNum).getConnections().get(0).getConnValue() | IO_REGION_BASE);
		} else if(comp.getInterfaces().get(ifNum).getName().equalsIgnoreCase("tx_csr"))
		{
			return String.format("#define CONFIG_SYS_ALTERA_TSE_SGDMA_TX_BASE\t0x%08X\n",
					comp.getInterfaces().get(ifNum).getConnections().get(0).getConnValue() | IO_REGION_BASE);
		} else {
			return "";
		}
	}
}
