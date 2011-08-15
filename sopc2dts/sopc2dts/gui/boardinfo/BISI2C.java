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

import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.components.BasicComponent;

public class BISI2C extends BISSubComponentTable {
	private static final long serialVersionUID = 563308640146741980L;
	boolean saveNeeded = false;
	JButton btnNewChip = new JButton("New Chip");
	
	public BISI2C() {
		super("I2C","i2c");
	}

	@Override
	public void save(BoardInfo bi, TableModel tm) {
		if(tm instanceof I2CMapTableModel)
		{
			HashMap<Integer, String> mi2c = new HashMap<Integer, String>();
			for(I2CChip chip : ((I2CMapTableModel)tm).vChips)
			{
				mi2c.put(chip.addr, chip.name);
			}
			bi.setI2CBusForchip((currComp == null ? null : currComp.getInstanceName()), mi2c);
		}
	}

	@Override
	public void setGuiEnabled(boolean ena) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected TableModel tableModelForComponent(BasicComponent comp) {
		HashMap<Integer, String> i2cMap;
		Vector<I2CChip> vChips = new Vector<BISI2C.I2CChip>();
		currComp = comp;

		i2cMap = bInfo.getI2CChipsForMaster((comp == null ? null : comp.getInstanceName()));
		if(i2cMap != null)
		{
			for(Integer addr : i2cMap.keySet())
			{
				I2CChip chip = new I2CChip();
				chip.addr = addr;
				chip.name = i2cMap.get(addr);
				vChips.add(chip);
			}
		}
		return new I2CMapTableModel(vChips);
	}
	protected class I2CChip {
		Integer addr;
		String name;
	}
	protected class I2CMapTableModel implements TableModel {
		Vector<TableModelListener> vListeners = new Vector<TableModelListener>();
		Vector<I2CChip> vChips;
		public I2CMapTableModel(Vector<I2CChip> vChips)
		{
			this.vChips = vChips;
		}
		public void addTableModelListener(TableModelListener l) {
			vListeners.add(l);
		}

		public Class<?> getColumnClass(int col) {
			return String.class;
		}

		public int getColumnCount() {
			return 2;
		}

		public String getColumnName(int col) {
			switch(col)
			{
			//Read-only
			case 0: return "Address(7bit)";
			//Part-name
			case 1: return "Name";
			//Base-addr
			default: return "Arthur Dent";
			}
		}

		public int getRowCount() {
			return vChips.size();
		}

		public Object getValueAt(int row, int col) {
			I2CChip chip = vChips.get(row);
			if(chip !=null)
			{
				switch(col)
				{
				//Read-only
				case 0: return "0x" + Integer.toHexString(chip.addr);
				//Part-name
				case 1: return chip.name;
				default: return null;
				}
			} else {
				return null;
			}
		}

		public boolean isCellEditable(int row, int col) {
			return true;
		}

		public void removeTableModelListener(TableModelListener l) {
			vListeners.remove(l);
		}

		public void setValueAt(Object val, int row, int col) {
			I2CChip chip = vChips.get(row);
			if(chip !=null)
			{
				switch(col)
				{
				//Address
				case 0: {
					try {
						chip.addr = Integer.decode((String)val);
					} catch(NumberFormatException nfe)
					{
						//ignore.
						val = null;
					}
				} break;
				//Size
				case 1: {
						chip.name = (String)val;
				} break;
				default: {
					Logger.logln("Setting non existing value!?",LogLevel.DEBUG);
				}
				}
			}
		}
		protected void addSubComponent(String name) {
			I2CChip c = new I2CChip();
			c.name = name;
			c.addr = 0;
			vChips.add(c);
			for(TableModelListener l : vListeners)
			{
				l.tableChanged(new TableModelEvent(this, vChips.size()-1));
			}
		}
		protected void removeSubComponent(int index) {
			vChips.remove(index);
			for(TableModelListener l : vListeners)
			{
				l.tableChanged(new TableModelEvent(this, index, vChips.size()-1));
			}
		}

	}

	@Override
	public boolean isSaveNeeded(TableModel tm) {
		return saveNeeded;
	}
	@Override
	protected void addSubComponent(TableModel mod, String name) {
		if(mod instanceof I2CMapTableModel)
		{
			((I2CMapTableModel)mod).addSubComponent(name);
		}
	}

	@Override
	protected void removeSubComponent(TableModel mod, int index) {
		if(mod instanceof I2CMapTableModel)
		{
			((I2CMapTableModel)mod).removeSubComponent(index);
		}
	}
}