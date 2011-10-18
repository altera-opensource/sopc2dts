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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.components.BasicComponent;

public abstract class BISSubComponentTable extends BISComponentGroup implements ActionListener {
	private static final long serialVersionUID = -6542453793278126674L;
	JPanel pnlCenter = new JPanel(new BorderLayout());
	private JTable tblSubComps = new JTable();
	JButton btnAdd = new JButton("Add");
	JButton btnRemove = new JButton("Remove");
	
	public BISSubComponentTable(String name, String group) {
		this(name,group,true);
	}
	public BISSubComponentTable(String name, String group, boolean useWildcard) {
		super(name,group,useWildcard);
		tblSubComps.setAutoCreateRowSorter(true);
		tblSubComps.setShowGrid(true);
		tblSubComps.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		tblSubComps.setGridColor(Color.LIGHT_GRAY);
		JPanel pnlBtns = new JPanel();
		btnAdd.addActionListener(this);
		btnRemove.addActionListener(this);
		pnlBtns.add(btnAdd);
		pnlBtns.add(btnRemove);
		pnlCenter.add(pnlBtns, BorderLayout.NORTH);
		pnlCenter.add(new JScrollPane(tblSubComps), BorderLayout.CENTER);
		this.add(pnlCenter, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(btnAdd))
		{
			String name = JOptionPane.showInputDialog("Please give the name");
			if(name!=null)
			{
				addSubComponent(tblSubComps.getModel(), name);
			}
		} else if(e.getSource().equals(btnRemove))
		{
			int index = tblSubComps.getSelectedRow();
			if(index>=0)
			{
				if(JOptionPane.showConfirmDialog(this, "Remove selected entry?","Confirm remove",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
				{
					removeSubComponent(tblSubComps.getModel(), index);
				}
			}
		}
	}
	protected void selectedComponent(BasicComponent comp)
	{
		tblSubComps.setModel(tableModelForComponent(comp));
	}
	protected abstract void addSubComponent(TableModel mod, String name);
	protected abstract void removeSubComponent(TableModel mod, int index);
	protected abstract TableModel tableModelForComponent(BasicComponent comp);
	protected abstract boolean isSaveNeeded(TableModel tm);

	@Override
	public void load(BoardInfo bi) {
		bInfo = bi;
		selectedComponent(currComp);
	}
	protected abstract void save(BoardInfo bi, TableModel tm);
	@Override
	public void save(BoardInfo bi)
	{
		save(bi, tblSubComps.getModel());
	}
	protected boolean isSaveNeeded()
	{
		return isSaveNeeded(tblSubComps.getModel());
	}
}
