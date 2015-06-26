/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 - 2013 Walter Goossens <waltergoossens@home.nl>

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

import java.util.Vector;

import javax.swing.JButton;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.boardinfo.I2CSlave;
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
		if(tm instanceof I2CTableModel)
		{
			Vector<I2CSlave> vSlaves = new Vector<I2CSlave>();
			for(I2CSlave chip : ((I2CTableModel)tm).vChips)
			{
				vSlaves.add(chip);
			}
			bi.setI2CBusForchip((currComp == null ? null : currComp.getInstanceName()), vSlaves);
		}
	}

	@Override
	public void setGuiEnabled(boolean ena) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected TableModel tableModelForComponent(BasicComponent comp) {
		currComp = comp;
		return new I2CTableModel(bInfo.getI2CForChip((comp == null ? "" : comp.getInstanceName())).getSlaves());
	}
	protected class I2CTableModel implements TableModel {
		Vector<TableModelListener> vListeners = new Vector<TableModelListener>();
		Vector<I2CSlave> vChips;
		public I2CTableModel(Vector<I2CSlave> vChips)
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
			return 3;
		}

		public String getColumnName(int col) {
			switch(col)
			{
			//Read-only
			case 0: return "Address(7bit)";
			//Part-name
			case 1: return "Name";
			case 2: return "Label";
			//Base-addr
			default: return "Arthur Dent";
			}
		}

		public int getRowCount() {
			return vChips.size();
		}

		public Object getValueAt(int row, int col) {
			I2CSlave chip = vChips.get(row);
			if(chip !=null)
			{
				switch(col)
				{
				//Read-only
				case 0: return "0x" + Integer.toHexString(chip.getAddr());
				//Part-name
				case 1: return chip.getName();
				case 2: return chip.getLabel();
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
			I2CSlave chip = vChips.get(row);
			if(chip !=null)
			{
				switch(col)
				{
				//Address
				case 0: {
					try {
						chip.setAddr(Integer.decode((String)val));
					} catch(NumberFormatException nfe)
					{
						//ignore.
						val = null;
					}
				} break;
				//Size
				case 1: {
						chip.setName((String)val);
				} break;
				case 2: {
					chip.setLabel((String)val);
				} break;
				default: {
					Logger.logln("Setting non existing value!?",LogLevel.DEBUG);
				}
				}
			}
		}
		protected void addSubComponent(String name) {
			I2CSlave c = new I2CSlave(0,name,null);
			vChips.add(c);
			for(TableModelListener l : vListeners)
			{
				l.tableChanged(new TableModelEvent(this));
			}
		}
		protected void removeSubComponent(int index) {
			vChips.remove(index);
			for(TableModelListener l : vListeners)
			{
				l.tableChanged(new TableModelEvent(this));
			}
		}

	}

	@Override
	public boolean isSaveNeeded(TableModel tm) {
		return saveNeeded;
	}
	@Override
	protected void addSubComponent(TableModel mod, String name) {
		if(mod instanceof I2CTableModel)
		{
			((I2CTableModel)mod).addSubComponent(name);
		}
	}

	@Override
	protected void removeSubComponent(TableModel mod, int index) {
		if(mod instanceof I2CTableModel)
		{
			((I2CTableModel)mod).removeSubComponent(index);
		}
	}
}
