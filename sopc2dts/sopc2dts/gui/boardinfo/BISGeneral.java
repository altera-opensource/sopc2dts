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

import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.gui.BasicComponentComboBoxModel;
import sopc2dts.gui.BasicComponentRenderer;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.components.BasicComponent;

public class BISGeneral extends BoardInfoSubPanel {
	private static final long serialVersionUID = -5792709661012221030L;
	JComboBox cbPov = new JComboBox();
	JTextField txtBootArgs = new JTextField();
	JList memComponentList = new JList();

	public BISGeneral()
	{
		super("General");
		cbPov.setEnabled(false);
		//cbPov.addItemListener(this);
		memComponentList.setCellRenderer(new BasicComponentRenderer());
		JPanel pnlPov = new JPanel(new GridLayout(1,2));
		pnlPov.setBorder(BorderFactory.createTitledBorder("Point of view"));
		pnlPov.add(new JLabel("Select master"));
		pnlPov.add(cbPov);
		JPanel pnlMemory = new JPanel(new GridLayout(1,3));
		pnlMemory.setBorder(BorderFactory.createTitledBorder("Memory nodes"));
		pnlMemory.add(new JLabel("<HTML>Memory nodes<BR><BR>You can specify " +
				"what nodes should be displayed in the memory section of the " +
				"DTS file.<BR>You normally only select the memories you wish " +
				"to run code from. I you don't select any node, all nodes will " +
				"be added to the memory section.</HTML>"));
		pnlMemory.add(new JScrollPane(memComponentList));
		JPanel pnlChosen = new JPanel(new GridLayout(1,2));
		pnlChosen.setBorder(BorderFactory.createTitledBorder("Chosen"));
		pnlChosen.add(new JLabel("Kernel Bootargs"));
		pnlChosen.add(txtBootArgs);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(pnlPov);
		add(pnlMemory);
		add(pnlChosen);
		add(Box.createVerticalGlue());
	}
	@Override
	public void save(BoardInfo bi) {
		bi.setBootArgs(txtBootArgs.getText());
		if(cbPov.getSelectedIndex()!=-1)
		{
			String[] povName = ((String)(cbPov.getSelectedItem())).split("\\: ");
			if(povName.length == 2)
			{
				bi.setPov(povName[1]);
				Vector<String> vMem = new Vector<String>();
				for(int i=0; i<memComponentList.getModel().getSize(); i++)
				{
					if(memComponentList.isSelectedIndex(i))
					{
						Logger.logln("Selected memory: " + ((BasicComponent)(memComponentList.getModel().getElementAt(i))).getInstanceName(), LogLevel.DEBUG);
						vMem.add(((BasicComponent)(memComponentList.getModel().getElementAt(i))).getInstanceName());
					}
				}
				bi.setMemoryNodes(vMem);
			} else {
				Logger.logln("Failed to parse POV component name");
			}
		}
	}

	@Override
	public void setGuiEnabled(boolean ena) {
		cbPov.setEnabled(ena);
		txtBootArgs.setEnabled(ena);
		memComponentList.setEnabled(ena);
	}
	@Override
	public void load(BoardInfo bi) {
		if(bi == null)
		{
			setGuiEnabled(false);
		} else {
			BasicComponentComboBoxModel bccbm = new BasicComponentComboBoxModel(sys, 
					true, bi.getPov());
			cbPov.setModel(bccbm);
			txtBootArgs.setText(bi.getBootArgs());
			if(sys!=null)
			{
				int i=0;
				memComponentList.setEnabled(true);
				Vector<BasicComponent> vMemComponents = new Vector<BasicComponent>();
				vMemComponents.addAll(sys.getSystemComponents());
				while(i<vMemComponents.size())
				{
					if(vMemComponents.get(i).getScd().getGroup().equalsIgnoreCase("memory"))
					{
						i++;
					} else {
						vMemComponents.remove(i);
					}
				}
				memComponentList.setListData(vMemComponents);
				for(i=0; i<vMemComponents.size(); i++)
				{
					if(bi.getMemoryNodes().contains(vMemComponents.get(i).getInstanceName()))
					{
						memComponentList.addSelectionInterval(i, i);
					}
				}
			} else {
				memComponentList.setListData(new Object[]{});
				memComponentList.setEnabled(false);
			}
		}
	}
}
