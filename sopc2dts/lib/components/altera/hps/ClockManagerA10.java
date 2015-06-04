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
import sopc2dts.lib.components.altera.hps.ClockManager.ClockManagerGateGroup;
import sopc2dts.lib.components.altera.hps.ClockManager.ClockManagerPClk;
import sopc2dts.lib.components.altera.hps.ClockManager.ClockManagerPll;

public class ClockManagerA10 extends ClockManager {
	static final SopcComponentDescription scdPLL = new SopcComponentDescription(
		"socfpga-a10-pll", "socfpga-a10-pll", "altr", "socfpga-a10-pll-clock");
	
	static final SopcComponentDescription scdPClk = new SopcComponentDescription(
		"socfpga-a10-perip-clk", "socfpga-a10-perip-clk", "altr", "socfpga-a10-perip-clk");
	
	static final SopcComponentDescription scdGClk = new SopcComponentDescription(
		"socfpga-a10-gate-clk", "socfpga-a10-gate-clk", "altr", "socfpga-a10-gate-clk");
	
	public ClockManagerA10(BasicComponent bc) {
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
		Vector<BasicComponent> vHPS = sys.getComponentsByClass("altera_arria10_hps");
		for(int i=0; i<vHPS.size() && (hpsName == null); i++) {
			if(getInstanceName().startsWith(vHPS.get(i).getInstanceName() + '_')) {
				hpsName = vHPS.get(i).getInstanceName();
			}
		}
		if(hpsName != null) {
			String noc_free_clk[] = new String[]{ "noc_free_clk"};
			String osc1 = hpsName + "_eosc1";
			String cb_intosc_ls = hpsName + "_cb_intosc_ls_clk";
			String cb_intosc_hs = hpsName + "_cb_intosc_hs_div2_clk";
			String f2s_free_clk = hpsName + "_f2s_free_clk";
			cmPLLs = new ClockManagerPll[]{
				new ClockManagerPll("main_pll", 0x40, 
					new String[]{
						osc1,
						cb_intosc_ls,
						f2s_free_clk
					}, 
					new ClockManagerPClk[] {
						new ClockManagerPClk("main_mpu_base_clk", 0, null, new long[]{0x140, 0, 11}, null),
						new ClockManagerPClk("main_noc_base_clk", 0, null, new long[]{0x144, 0, 11}, null),
						new ClockManagerPClk("main_emaca_clk", 0x28, null, null, null),
						new ClockManagerPClk("main_emacb_clk", 0x2c, null, null, null),
						new ClockManagerPClk("main_emac_ptp_clk", 0x30, null, null, null),
						new ClockManagerPClk("main_gpio_db_clk", 0x34, null, null, null),
						new ClockManagerPClk("main_sdmmc_clk", 0x38, null, null, null),
						new ClockManagerPClk("main_s2f_usr0_clk", 0x3c, null, null, null),
						new ClockManagerPClk("main_s2f_usr1_clk", 0x40, null, null, null),
						new ClockManagerPClk("main_hmc_pll_ref_clk", 0x44, null, null, null),
						new ClockManagerPClk("main_periph_ref_clk", 0x5c, null, null, null),
					}
				),
				new ClockManagerPll("periph_pll", 0xC0,
					new String[]{
						osc1,
						cb_intosc_ls,
						f2s_free_clk,
						"main_periph_ref_clk",
					},
					new ClockManagerPClk[] {
						new ClockManagerPClk("peri_mpu_base_clk", 0, null, new long[]{0x140, 16, 11}, null),
						new ClockManagerPClk("peri_noc_base_clk", 0, null, new long[]{0x144, 16, 11}, null),
						new ClockManagerPClk("peri_emaca_clk", 0x28, null, null, null),
						new ClockManagerPClk("peri_emacb_clk", 0x2c, null, null, null),
						new ClockManagerPClk("peri_emac_ptp_clk", 0x30, null, null, null),
						new ClockManagerPClk("peri_gpio_db_clk", 0x34, null, null, null),
						new ClockManagerPClk("peri_sdmmc_clk", 0x38, null, null, null),
						new ClockManagerPClk("peri_s2f_usr0_clk", 0x3c, null, null, null),
						new ClockManagerPClk("peri_s2f_usr1_clk", 0x40, null, null, null),
						new ClockManagerPClk("peri_hmc_pll_ref_clk", 0x44, null, null, null),

					}
				),
			};
			cmGGroups = new ClockManagerGateGroup[] {
				new ClockManagerGateGroup(0x48, new ClockManagerGateClk[] {
					new ClockManagerGateClk("mpu_periph_clk", 	true, null, new Long(4), new String[]{ "mpu_free_clk"}),
					new ClockManagerGateClk("l4_main_clk", 	true, new long[]{0xa8, 0, 2}, null, noc_free_clk),
					new ClockManagerGateClk("l4_mp_clk", 	true, new long[]{0xa8, 8, 2}, null, noc_free_clk),
					new ClockManagerGateClk("l4_sp_clk", 	true, new long[]{0xa8, 16, 2}, null, noc_free_clk),
				}),
				new ClockManagerGateGroup(0xc8, new ClockManagerGateClk[] {
					new ClockManagerGateClk("emac0_clk", true, null, null, new String[]{}),
					new ClockManagerGateClk("emac1_clk", true, null, null, new String[]{}),
					new ClockManagerGateClk("emac2_clk", true, null, null, new String[]{}),
					new ClockManagerGateClk("emac_ptp_clk", true, null, null, new String[]{}),
					new ClockManagerGateClk("gpio_db_clk", true, null, null, new String[]{}),
					new ClockManagerGateClk("sdmmc_clk", true, null, null, new String[]{ "sdmmc_free_clk"}),
					new ClockManagerGateClk("s2f_user1_clk", true, null, null, new String[]{ "peri_s2f_usr1_clk"}),
					new ClockManagerGateClk("reserved", true, null, null, new String[]{}),
					new ClockManagerGateClk("usb_clk", true, null, null, new String[]{"l4_mp_clk"}),
					new ClockManagerGateClk("spi_m_clk", true, null, null, new String[]{"l4_main_clk"}),
					new ClockManagerGateClk("nand_clk", true, null, null, new String[]{"l4_mp_clk"}),
					new ClockManagerGateClk("qspi_clk", true, null, null, new String[]{"l4_main_clk"}),
				}),
			};
			cmPeripheralClks = new ClockManagerPClk[] {
				new ClockManagerPClk("mpu_free_clk", 0x60, null, null, new String[]{"main_mpu_base_clk", "peri_mpu_base_clk", osc1, cb_intosc_hs, f2s_free_clk}),
				new ClockManagerPClk("noc_free_clk", 0x64, null, null, new String[]{"main_noc_base_clk", "peri_noc_base_clk", osc1, cb_intosc_hs, f2s_free_clk}),
				new ClockManagerPClk("s2f_usr1_clk", 0x104, null, null, new String[]{"main_s2f_usr1_clk", "peri_s2f_usr1_clk", osc1, cb_intosc_hs, f2s_free_clk}),
				new ClockManagerPClk("sdmmc_free_clk", 0xf8, new Long(4), null, new String[]{"main_sdmmc_clk", "peri_sdmmc_clk", osc1, cb_intosc_hs, f2s_free_clk}),
				new ClockManagerPClk("l4_sys_free_clk", null, new Long(4), null, noc_free_clk),
	};
			return true;
		} else {
			Logger.logln(this, "Failed to determine the HPS_A10 we belong to", LogLevel.WARNING);
			return false;
		}
	}
	

	
	protected  SocFpgaPllClock getSocFpgaPllClock(String cName, String iName, String ver)
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

