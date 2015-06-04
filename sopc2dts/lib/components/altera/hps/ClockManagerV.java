/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2014 Walter Goossens <waltergoossens@home.nl>

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
package sopc2dts.lib.components.altera.hps;

import java.util.Vector;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.components.altera.hps.ClockManager.ClockManagerGateClk;

public class ClockManagerV extends ClockManager {
	static final SopcComponentDescription scdPLL = 
		new SopcComponentDescription("socfpga-pll", "socfpga-pll", "altr", "socfpga-pll-clock");
	
	static final SopcComponentDescription scdPClk = 
		new SopcComponentDescription("socfpga-perip-clk", "socfpga-perip-clk", "altr", "socfpga-perip-clk");
	
	static final SopcComponentDescription scdGClk = 
		new SopcComponentDescription("socfpga-gate-clk", "socfpga-gate-clk", "altr", "socfpga-gate-clk");

	public ClockManagerV(BasicComponent bc) {
		super(bc);
	}
	@Override
	protected String getFirstSupportedVersion() {
		/* Other classes can overload this function when needed in the future */
		return "14.0";
	}
	@Override
	protected boolean preRemovalChecks(AvalonSystem sys) {
		String hpsName = null;
		Vector<BasicComponent> vHPS = sys.getComponentsByClass("altera_hps");
		for(int i=0; i<vHPS.size() && (hpsName == null); i++) {
			if(getInstanceName().startsWith(vHPS.get(i).getInstanceName() + '_')) {
				hpsName = vHPS.get(i).getInstanceName();
			}
		}
		if(hpsName != null) {
			cmPLLs = new ClockManagerPll[]{
					new ClockManagerPll("sdram_pll", 0xC0, 
							new String[] { hpsName + "_eosc1", hpsName +"_eosc2", hpsName + "_f2s_sdram_ref_clk" } ,
							new ClockManagerPClk[] {
									new ClockManagerPClk("ddr_dqs_clk", 	0x08, null, null),
									new ClockManagerPClk("ddr_2x_dqs_clk", 	0x0C, null, null),
									new ClockManagerPClk("ddr_dq_clk", 		0x10, null, null),
									new ClockManagerPClk("s2f_usr2_clk", 	0x14, null, null),
							}
					),
					new ClockManagerPll("periph_pll", 0x80, 
							new String[] { hpsName + "_eosc1", hpsName + "_eosc2", hpsName + "_f2s_periph_ref_clk"}, 
							new ClockManagerPClk[] {
									new ClockManagerPClk("per_nand_mmc_clk",0x14, null, null),
									new ClockManagerPClk("per_base_clk",	0x18, null, null),
									new ClockManagerPClk("per_qspi_clk",	0x10, null, null),
									new ClockManagerPClk("s2f_usr1_clk",	0x1C, null, null),
									new ClockManagerPClk("emac0_clk",		0x08, null, null),
									new ClockManagerPClk("emac1_clk",		0x0C, null, null),
							}
					),
					new ClockManagerPll("main_pll", 0x40, 
							new String[]{ hpsName + "_eosc1" }, 
							new ClockManagerPClk[] {
									new ClockManagerPClk("cfg_s2f_usr0_clk",0x1C, null, null),
									new ClockManagerPClk("main_qspi_clk",	0x14, null, null),
									new ClockManagerPClk("dbg_base_clk",	0x10, null, new long[]{0xe8, 0, 9}, new String[]{ hpsName + "_eosc1"}),
									new ClockManagerPClk("mpuclk",			0x08, null, new long[]{0xe0, 0, 9}, null),
									new ClockManagerPClk("mainclk",			0x0C, null, new long[]{0xe4, 0, 9}, null),
									new ClockManagerPClk("main_nand_sdmmc_clk",0x18, null, null),
							}
					),
			};
			cmGGroups = new ClockManagerGateGroup[] {
					new ClockManagerGateGroup(0x60, new ClockManagerGateClk[] {
							new ClockManagerGateClk("mpu_l2_ram_clk",false,null, 							new Long(2), new String[]{ "mpuclk" }),
							new ClockManagerGateClk("l4_main_clk", 	true, null, 							null, new String[]{ "mainclk" }),
							new ClockManagerGateClk("l3_mp_clk",	true, new long[]{ 0x64, 0x00, 0x02 }, 	null, new String[]{ "mainclk" }),
							new ClockManagerGateClk("l3_sp_clk", 	false,new long[]{ 0x64, 0x02, 0x02 }, 	null, new String[]{ "l3_mp_clk" }),
							new ClockManagerGateClk("l4_mp_clk", 	true ,new long[]{ 0x64, 0x04, 0x03 }, 	null, new String[]{ "mainclk", "per_base_clk" }),
							new ClockManagerGateClk("l4_sp_clk", 	true, new long[]{ 0x64, 0x07, 0x03 }, 	null, new String[]{ "mainclk", "per_base_clk" }),
							new ClockManagerGateClk("dbg_at_clk", 	true, new long[]{ 0x68, 0x00, 0x02 }, 	null, new String[]{ "dbg_base_clk" }),
							new ClockManagerGateClk("dbg_clk", 		true, new long[]{ 0x68, 0x02, 0x02 }, 	null, new String[]{ "dbg_at_clk" }),
							new ClockManagerGateClk("dbg_trace_clk",true, new long[]{ 0x6C, 0x00, 0x03 }, 	null, new String[]{ "dbg_base_clk" }),
							new ClockManagerGateClk("dbg_timer_clk",true, null, 							null, new String[]{ "dbg_base_clk" }),
							new ClockManagerGateClk("cfg_clk", 		true, null, 							null, new String[]{ "cfg_s2f_usr0_clk" }),
							new ClockManagerGateClk("h2f_user0_clock",true,null,							null, new String[]{ "cfg_s2f_usr0_clk" }),
					}),
					new ClockManagerGateGroup(0xA0, new ClockManagerGateClk[] {
							new ClockManagerGateClk("emac_0_clk", 	true, null, 							null, new String[]{ "emac0_clk" }),
							new ClockManagerGateClk("emac_1_clk",	true, null,							 	null, new String[]{ "emac1_clk" }),
							new ClockManagerGateClk("usb_mp_clk", 	true, new long[]{ 0xA4, 0x00, 0x03 }, 	null, new String[]{ "per_base_clk" }),
							new ClockManagerGateClk("spi_m_clk", 	true ,new long[]{ 0xA4, 0x03, 0x03 }, 	null, new String[]{ "per_base_clk" }),
							new ClockManagerGateClk("can0_clk", 	true, new long[]{ 0xA4, 0x06, 0x03 }, 	null, new String[]{ "per_base_clk" }),
							new ClockManagerGateClk("can1_clk", 	true, new long[]{ 0xA4, 0x09, 0x03 }, 	null, new String[]{ "per_base_clk" }),
							new ClockManagerGateClk("gpio_db_clk", 	true, new long[]{ 0xA8, 0x00, 0x18 }, 	null, new String[]{ "per_base_clk" }),
							new ClockManagerGateClk("h2f_user1_clock",true,null,						 	null, new String[]{ "s2f_usr1_clk" }),
							new ClockManagerGateClk("sdmmc_clk",	true, null,	 							null, new String[]{ hpsName + "_f2s_periph_ref_clk", "main_nand_sdmmc_clk", "per_nand_mmc_clk" }),
							new ClockManagerGateClk("nand_x_clk",	true, null, 							null, new String[]{ hpsName + "_f2s_periph_ref_clk", "main_nand_sdmmc_clk", "per_nand_mmc_clk" }),
							new ClockManagerGateClk("nand_clk", 	true, null, 							new Long(4), new String[]{ hpsName + "_f2s_periph_ref_clk", "main_nand_sdmmc_clk", "per_nand_mmc_clk" }),
							new ClockManagerGateClk("qspi_clk",		true, null,								null, new String[]{ hpsName + "_f2s_periph_ref_clk", "main_qspi_clk", "per_qspi_clk" }),
					}),
					new ClockManagerGateGroup(0xD8, new ClockManagerGateClk[]{
							new ClockManagerGateClk("ddr_dqs_clk_gate",		true, null,	 					null, new String[]{ "ddr_dqs_clk" }),
							new ClockManagerGateClk("ddr_2x_dqs_clk_gate",	true, null, 					null, new String[]{ "ddr_2x_dqs_clk" }),
							new ClockManagerGateClk("ddr_dq_clk_gate", 		true, null, 					null, new String[]{ "ddr_dq_clk" }),
							new ClockManagerGateClk("h2f_user2_clock",		true, null,						null, new String[]{ "s2f_usr2_clk" }),
							new ClockManagerGateClk("l3_main_clk", 			false,null, 					null, new String[]{ "mainclk" }),
					}),
			};
			cmPeripheralClks = new ClockManagerPClk[] {
						new ClockManagerPClk("mpu_periph_clk", 0, new Long(4), new String[]{"mpuclk"}),
			};
			return true;
		} else {
			Logger.logln(this, "Failed to determine the HPS we belong to", LogLevel.WARNING);
			return false;
		}
	}
	protected SocFpgaPllClock getSocFpgaPllClock(String cName, String iName, String ver)
	{
		return new SocFpgaPllClock(cName, iName, ver, scdPLL);
	}
	protected SocFpgaPeripClock getSocFpgaPeripClock(String cName, String iName, String ver, Long reg, Long div, long[]divreg)
	{
		return new SocFpgaPeripClock(cName, iName, ver, reg, div, divreg, scdPClk);
	}
	protected SocFpgaGateClock getSocFpgaGateClock(ClockManagerGateClk cmgClk, String ver)
	{
		return new SocFpgaGateClock(cmgClk, ver, scdGClk);
	}
}
