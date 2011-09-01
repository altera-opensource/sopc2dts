/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 Walter Goossens <waltergoossens@home.nl>

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package sopc2dts.lib.uboot;

import java.util.HashMap;
import java.util.Vector;

import sopc2dts.lib.components.BasicComponent;

public class UBootComponentLib {
	Vector<UBootLibComponent> vComponents = new Vector<UBootLibComponent>();
	static UBootComponentLib me;
	private UBootComponentLib()
	{
		HashMap<String, String> props = new HashMap<String, String>();
		props.put("CONFIG_SYS_CLK_FREQ",		"gen|clk");
		props.put("CONFIG_SYS_RESET_ADDR", 		"prop|embeddedsw.CMacro.RESET_ADDR");
		props.put("CONFIG_SYS_EXCEPTION_ADDR", 	"prop|embeddedsw.CMacro.EXCEPTION_ADDR");
		props.put("CONFIG_SYS_ICACHE_SIZE", 	"prop|embeddedsw.CMacro.ICACHE_SIZE");
		props.put("CONFIG_SYS_ICACHELINE_SIZE", "prop|embeddedsw.CMacro.ICACHE_LINE_SIZE");
		props.put("CONFIG_SYS_DCACHE_SIZE", 	"prop|embeddedsw.CMacro.DCACHE_SIZE");
		props.put("CONFIG_SYS_DCACHELINE_SIZE", "prop|embeddedsw.CMacro.DCACHE_LINE_SIZE");
		/* TODO This is a hack. doesn't work for nommu */
		vComponents.add(new UBootLibComponent(
				new String[] { "altera_nios2"}, props, 
				String.format("#define IO_REGION_BASE\t0x%08X\n",UBootLibComponent.IO_REGION_BASE_MMU)));
		
		props = new HashMap<String, String>();
		props.put("CONFIG_SYS_SYSID_BASE",		"gen|ioaddr");
		vComponents.add(new UBootLibComponent(
				new String[] { "altera_avalon_sysid"}, props, null));
		
		props = new HashMap<String, String>();
		props.put("CONFIG_SYS_GPIO_BASE",		"gen|ioaddr");
		vComponents.add(new UBootLibComponent(
				new String[] { "gpio"}, props, null));

		props = new HashMap<String, String>();
		props.put("CONFIG_SYS_JTAG_UART_BASE",		"gen|ioaddr");
		vComponents.add(new UBootLibComponent(
				new String[] { "altera_avalon_jtag_uart"}, props, null));

		props = new HashMap<String, String>();
		props.put("CONFIG_SYS_UART_BASE",		"gen|ioaddr");
		props.put("CONFIG_SYS_UART_FREQ",		"gen|clk");
		props.put("CONFIG_SYS_UART_BAUD",		"prop|embeddedsw.CMacro.BAUD");
		vComponents.add(new UBootLibComponent(
				new String[] { "altera_avalon_uart", "fifoed_avalon_uart_classic"}, 
				props, null));
		
		props = new HashMap<String, String>();
		props.put("CONFIG_SYS_TIMER_BASE",		"gen|ioaddr");
		props.put("CONFIG_SYS_TIMER_IRQ",		"gen|irq");
		props.put("CONFIG_SYS_TIMER_FREQ",		"gen|clk");
		vComponents.add(new UBootLibComponent(
				new String[] { "altera_avalon_timer"}, props, null));

		props = new HashMap<String, String>();
		props.put("CONFIG_SYS_ATA_BASE_ADDR",	"gen|ioaddr");
		vComponents.add(new UBootLibComponent(
				new String[] { "altera_avalon_cf"}, props, 
				"#define CONFIG_CMD_IDE\n" +
				"#define CONFIG_IDE_RESET\n" +
				"#define CONFIG_CMD_FAT\n" +
				"#define CONFIG_DOS_PARTITION\n" +
				"#define CONFIG_SYS_PIO_MODE 1\n" +
				"#define CONFIG_SYS_IDE_MAXBUS 1\n" +
				"#define CONFIG_SYS_IDE_MAXDEVICE 1\n" +
				"#define CONFIG_SYS_ATA_STRIDE 4\n" +
				"#define CONFIG_SYS_ATA_DATA_OFFSET 0x0\n" +
				"#define CONFIG_SYS_ATA_REG_OFFSET 0x0\n" +
				"#define CONFIG_SYS_ATA_ALT_OFFSET 0x20\n"));

		props = new HashMap<String, String>();
		props.put("CONFIG_SYS_ETHOC_BASE",		"gen|ioaddr");
		vComponents.add(new UBootLibComponent(
				new String[] { "eth_ocm"}, props, "#define CONFIG_ETHOC"));
		
		props = new HashMap<String, String>();
		props.put("CONFIG_SYS_FLASH_BASE",		"gen|ioaddr");
		vComponents.add(new UBootLibComponent(
				new String[] { "altera_avalon_cfi_flash"}, props,
				"#define CONFIG_FLASH_CFI_DRIVER\n" +
				"#define CONFIG_SYS_CFI_FLASH_STATUS_POLL /* fix amd flash issue */\n" +
				"#define CONFIG_SYS_FLASH_CFI\n" +
				"#define CONFIG_SYS_FLASH_USE_BUFFER_WRITE\n" +
				"#define CONFIG_SYS_FLASH_PROTECTION\n" +
				"#define CONFIG_SYS_MAX_FLASH_BANKS 1\n" +
				"#define CONFIG_SYS_MAX_FLASH_SECT 1024\n"));
		
		props = new HashMap<String, String>();
		props.put("CONFIG_SYS_SDRAM_BASE",		"gen|kerneladdr");
		props.put("CONFIG_SYS_SDRAM_SIZE",		"gen|size");
		vComponents.add(new UBootLibComponent(
				new String[] { "ddr_sdram_component_classic", "*altera_avalon_new_sdram_controller*", "*altmemddr*" }, 
				props, null));

		props = new HashMap<String, String>();
		props.put("CONFIG_SYS_ALTERA_TSE_MAC_BASE","gen|ioaddr0");
		props.put("CONFIG_SYS_ALTERA_TSE_SGDMA_RX_BASE","gen|ioaddr1");
		props.put("CONFIG_SYS_ALTERA_TSE_SGDMA_TX_BASE","gen|ioaddr2");
		props.put("CONFIG_SYS_ALTERA_TSE_DESC","gen|ioaddr3");
		props.put("CONFIG_SYS_ALTERA_TSE_RX_FIFO", "prop|embeddedsw.CMacro.RECEIVE_FIFO_DEPTH");
		props.put("CONFIG_SYS_ALTERA_TSE_TX_FIFO", "prop|embeddedsw.CMacro.TRANSMIT_FIFO_DEPTH");
		vComponents.add(new UBootLibComponent(
				new String[] { "triple_speed_ethernet" }, props, 
				"#define CONFIG_ALTERA_TSE\n" +
				"#define CONFIG_MII\n" +
				"#define CONFIG_CMD_MII\n" +
				"#define CONFIG_SYS_ALTERA_TSE_PHY_ADDR 1\n" +
				"#define CONFIG_SYS_ALTERA_TSE_FLAGS 0\n"));
	}
	public static UBootComponentLib getInstance()
	{
		if(me == null)
		{
			me = new UBootComponentLib();
		}
		return me;
	}
	public UBootLibComponent getCompFor(BasicComponent comp)
	{
		for(UBootLibComponent lc : vComponents)
		{
			if(lc.isCompatible(comp.getClassName()))
			{
				return lc;
			}
		}
		return new UBootLibComponent(null, null, null);
	}
}
