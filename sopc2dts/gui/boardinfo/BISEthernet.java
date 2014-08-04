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
package sopc2dts.gui.boardinfo;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.boardinfo.BICEthernet;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.altera.TSEMonolithic;
import sopc2dts.lib.components.base.SICEthernet;

public class BISEthernet extends BISComponentGroup {
	private static final long serialVersionUID = -3161034882365005048L;
	JTextField txtMac = new JTextField();
	JComboBox cbMII = new JComboBox();
	JComboBox cbPHY = new JComboBox();
	JPanel pnlEdit;
	
	protected SICEthernet currEth;
	public BISEthernet() {
		super("Ethernet","ethernet",false);
		for(int i=0; i<0x20; i++)
		{
			cbMII.addItem("" + i);
			cbPHY.addItem("" + i);
		}
		cbPHY.addItem("Auto");
	}

	@Override
	protected boolean isSaveNeeded() {
		return (currEth != null);
	}

	@Override
	protected void selectedComponent(BasicComponent comp) {
		if(comp instanceof SICEthernet)
		{
			BICEthernet be = bInfo.getEthernetForChip(comp.getInstanceName());
			int rows = 1;
			if(pnlEdit != null)
			{
				this.remove(pnlEdit);
			}
			currEth = (SICEthernet)comp;
			if(comp instanceof TSEMonolithic)
			{
				rows += 2;
			}
			pnlEdit = new JPanel(new GridLayout(rows,2));
			pnlEdit.setBorder(BorderFactory.createTitledBorder(comp.getInstanceName()));
			txtMac.setText(be.getMacString());
			pnlEdit.add(new JLabel("MAC-Adress"));
			pnlEdit.add(txtMac);
			if(comp instanceof TSEMonolithic)
			{
				cbMII.setSelectedIndex((be.getMiiID() == null ? 0 : be.getMiiID()));
				cbPHY.setSelectedIndex((be.getPhyID() == null ? cbPHY.getItemCount() -1 : be.getPhyID()));
				pnlEdit.add(new JLabel("MII-Id"));
				pnlEdit.add(cbMII);
				pnlEdit.add(new JLabel("PHY-Id"));
				pnlEdit.add(cbPHY);
			}
			this.add(pnlEdit, BorderLayout.CENTER);
			this.revalidate();
		}		
	}

	@Override
	public void save(BoardInfo bi) {
		if(currEth != null)
		{
			BICEthernet be = bi.getEthernetForChip(currEth.getInstanceName());
			if(currEth instanceof TSEMonolithic)
			{
				be.setMac(txtMac.getText());
				be.setMiiID(new Integer(cbMII.getSelectedIndex()));
				if(cbPHY.getSelectedIndex()<(cbPHY.getItemCount()-1))
				{
					be.setPhyID(new Integer(cbPHY.getSelectedIndex()));
				} else {
					be.setPhyID(null);
				}
			} else {
				be.setMiiID(null);
				be.setPhyID(null);			
			}
			bi.setEthernetForChip(be);
		}
	}

	@Override
	public void load(BoardInfo bi) {
		if(currEth!=null)
		{
			selectedComponent(currEth);
		}
	}
}
