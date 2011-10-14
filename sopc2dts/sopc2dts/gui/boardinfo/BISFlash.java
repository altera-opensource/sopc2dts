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

import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.base.FlashPartition;
import sopc2dts.lib.components.base.SICFlash;

public class BISFlash extends BISSubComponentTable {
	private static final long serialVersionUID = 1531418789892150665L;
	
	public BISFlash() {
		super("Flash", "flash");
	}

	@Override
	public void save(BoardInfo bi, TableModel tm) {
		if(tm instanceof FlashPartitionTableModel)
		{
			bi.setPartitionsForchip((currComp == null ? null : currComp.getInstanceName()),
					((FlashPartitionTableModel)tm).vParts);
		}
	}

	@Override
	public void setGuiEnabled(boolean ena) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean matchComponent(BasicComponent comp)
	{
		if(comp instanceof SICFlash)
		{
			return true;
		} else {
			return super.matchComponent(comp);
		}
	}

	@Override
	protected TableModel tableModelForComponent(BasicComponent comp) {
		Vector<FlashPartition> vfp;
		currComp = comp;
		if(comp == null)
		{
			vfp = bInfo.getPartitionsForChip(null);
		} else {
			vfp = bInfo.getPartitionsForChip(comp.getInstanceName());
		}
		if(vfp != null)
		{
			Vector<FlashPartition> org = vfp;
			vfp = new Vector<FlashPartition>();
			for(FlashPartition fp : org)
			{
				vfp.add(new FlashPartition(fp));
			}
		} else {
			vfp = new Vector<FlashPartition>();
		}
		return new FlashPartitionTableModel(vfp);
	}
	
	protected class FlashPartitionTableModel implements TableModel {
		Vector<TableModelListener> vListeners = new Vector<TableModelListener>();
		Vector<FlashPartition> vParts;
		public FlashPartitionTableModel(Vector<FlashPartition> vParts)
		{
			this.vParts = vParts;
		}
		public void addTableModelListener(TableModelListener l) {
			vListeners.add(l);
		}

		public Class<?> getColumnClass(int col) {
			switch(col)
			{
			//Read-only
			case 0: return Boolean.class;
			//Part-name
			case 1: return String.class;
			//Base-addr
			case 2: return String.class;
			//Size
			case 3: return String.class;
			default: return String.class;
			}
		}

		public int getColumnCount() {
			return 4;
		}

		public String getColumnName(int col) {
			switch(col)
			{
			//Read-only
			case 0: return "Read-Only";
			//Part-name
			case 1: return "Name";
			//Base-addr
			case 2: return "Base Address";
			//Size
			case 3: return "Size";
			default: return "Arthur Dent";
			}
		}

		public int getRowCount() {
			return vParts.size();
		}

		public Object getValueAt(int row, int col) {
			FlashPartition fp = vParts.get(row);
			if(fp !=null)
			{
				switch(col)
				{
				//Read-only
				case 0: return fp.isReadonly();
				//Part-name
				case 1: return fp.getName();
				//Base-addr
				case 2: return String.format("0x%08X", fp.getAddress());
				//Size
				case 3: return String.format("0x%08X", fp.getSize());
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
			FlashPartition fp = vParts.get(row);
			if(fp !=null)
			{
				switch(col)
				{
				//Read-only
				case 0: {
					fp.setReadonly((Boolean)val);
				} break;
				//Part-name
				case 1: {
					fp.setName((String)val);
				}
				//Base-addr
				case 2: {
					try {
						fp.setAddress(Integer.decode((String)val));
					} catch(NumberFormatException nfe)
					{
						//ignore.
						val = null;
					}
				} break;
				//Size
				case 3: {
					try {
						fp.setSize(Integer.decode((String)val));
					} catch(NumberFormatException nfe)
					{
						//ignore.
						val = null;
					}
				} break;
				default: {
					Logger.logln("Setting non existing value!?",LogLevel.DEBUG);
				}
				}
			}
		}
		protected void addSubComponent(String name) {
			FlashPartition fp = new FlashPartition();
			fp.setName(name);
			fp.setAddress(0);
			fp.setReadonly(false);
			fp.setSize(0);
			vParts.add(fp);
			for(TableModelListener l : vListeners)
			{
				l.tableChanged(new TableModelEvent(this));
			}
		}
		protected void removeSubComponent(int index) {
			vParts.remove(index);
			for(TableModelListener l : vListeners)
			{
				l.tableChanged(new TableModelEvent(this));
			}
		}

	}

	@Override
	public boolean isSaveNeeded(TableModel tm) {
		if(tm instanceof FlashPartitionTableModel)
		{
			Vector<FlashPartition> vOurPart = ((FlashPartitionTableModel)tm).vParts;
			Vector<FlashPartition> vOrgPart = bInfo.getPartitionsForChip((currComp == null ? null : currComp.getInstanceName()));
			if(vOrgPart != null)
			{
				if(vOrgPart.size() == vOurPart.size())
				{
					return !vOrgPart.containsAll(vOurPart);
				} else {
					return true;
				}
			} else {
				if(vOurPart.size()>0)
				{
					return true;
				} else {
					return false;
				}
			}
		} else {
			return false;
		}
	}

	@Override
	protected void addSubComponent(TableModel mod, String name) {
		if(mod instanceof FlashPartitionTableModel)
		{
			((FlashPartitionTableModel)mod).addSubComponent(name);
		}
	}

	@Override
	protected void removeSubComponent(TableModel mod, int index) {
		if(mod instanceof FlashPartitionTableModel)
		{
			((FlashPartitionTableModel)mod).removeSubComponent(index);
		}
	}
}
