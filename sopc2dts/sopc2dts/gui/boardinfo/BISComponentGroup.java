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
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sopc2dts.gui.BasicComponentRenderer;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.SopcComponentDescription;

public abstract class BISComponentGroup extends BoardInfoSubPanel implements ListSelectionListener{
	private static final long serialVersionUID = 6266428894255737000L;
	String group;
	BasicComponent wildCardComp;
	private JList componentList = new JList();
	protected BasicComponent currComp;

	public BISComponentGroup(String name, String group, boolean useWildcard) {
		super(name);
		this.group = group;
		if(useWildcard)
		{
			wildCardComp = new BasicComponent(group,"all","0",
					new SopcComponentDescription(group, group, "none", "any"));
			componentList.setListData(new BasicComponent[]{ wildCardComp });
		}
		componentList.setCellRenderer(new BasicComponentRenderer());
		componentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		componentList.addListSelectionListener(this);
		JScrollPane pnlWest = new JScrollPane(componentList);
		pnlWest.setBorder(BorderFactory.createTitledBorder("Choose component"));
		this.setLayout(new BorderLayout());
		this.add(pnlWest, BorderLayout.WEST);
	}

	protected abstract boolean isSaveNeeded();
	protected abstract void selectedComponent(BasicComponent comp);

	@Override
	public boolean setBoardInfoAndSys(BoardInfo bi, AvalonSystem s)
	{
		super.setBoardInfoAndSys(bi, s);
		Vector<BasicComponent> vComp = new Vector<BasicComponent>();
		vComp.add(wildCardComp);
		if(s!=null)
		{
			for(BasicComponent comp : s.getSystemComponents())
			{
				if(comp.getScd().getGroup().equalsIgnoreCase(group))
				{
					vComp.add(comp);
				}
			}
		}
		componentList.setListData(vComp);
		return (vComp.size()>1);
	}

	@Override
	public void setGuiEnabled(boolean ena) {
		componentList.setEnabled(ena);
	}

	public void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting())
		{
			BasicComponent comp = (BasicComponent)componentList.getSelectedValue();
			if(comp!=null)
			{
				if(comp.equals(wildCardComp))
				{
					comp = null;
				}
				if(isSaveNeeded())
				{
					int res = JOptionPane.showConfirmDialog(this, "Apply changes?");
					if(res == JOptionPane.YES_OPTION)
					{
						save(bInfo);
					}
					if(res != JOptionPane.CANCEL_OPTION)
					{
						selectedComponent(comp);
					}
				} else {
					selectedComponent(comp);
				}
			}
		}
	}
}
