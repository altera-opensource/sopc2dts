/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2015 Matthew Gerlach <mgerlach@opensource.altera.com>

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
package sopc2dts.lib.components.altera;

import java.util.Vector;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;

public class A10InterfaceGenerator extends BasicComponent
{
	private boolean removed = false;

	public A10InterfaceGenerator(String cName, String iName, String ver, SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}
	private void moveEmacs(AvalonSystem sys)
	{
		final String[] signal_suffix = {"", "_md_clk", "_rx_clk_in", "_tx_clk_in", "_gtx_clk", "_tx_reset", "_rx_reset"}; 
		String hpsName = null;
		Vector<BasicComponent> vHPS = sys.getComponentsByClass("altera_arria10_hps");
		for(int i=0; i<vHPS.size() && (hpsName == null); i++) {
			if(getInstanceName().startsWith(vHPS.get(i).getInstanceName() + '_')) {
				hpsName = vHPS.get(i).getInstanceName();
			}
		}
		if (hpsName != null) {
			Logger.logln("name of HPS is " + hpsName, LogLevel.DEBUG);
		}
		for (int i = 0; i < 3; i++) {
			String signal_base = String.format("emac%d", i);
			String emac_name = hpsName+"_i_emac_"+signal_base;
			BasicComponent emac = sys.getComponentByName(emac_name) ;
			if (emac == null) {
				Logger.logln("Could not find " + emac_name, LogLevel.DEBUG);
			} else {		
				for (String suffix : signal_suffix) {
					String signal = signal_base + suffix;
					Interface intf = getInterfaceByName(signal);
					if (intf != null) {
						Logger.logln("found "+intf.getName()+" mapped to "+emac.getInstanceName(), LogLevel.DEBUG);
						intf.getOwner().removeInterface(intf);
						emac.addInterface(intf);
		
					} else {
						Logger.logln("could not find interface "+signal, LogLevel.DEBUG);
					}
				}
			}
		}			
	}		
	@Override
	public boolean removeFromSystemIfPossible(AvalonSystem sys)
	{
		if (!removed) {
			removed = true;
			moveEmacs(sys);
			return true;
		} else {
			return false;
		}
	}
}
