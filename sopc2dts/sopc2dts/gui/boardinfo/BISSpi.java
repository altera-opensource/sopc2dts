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

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.boardinfo.BICSpi;
import sopc2dts.lib.boardinfo.BoardInfoComponent;
import sopc2dts.lib.boardinfo.SpiSlave;
import sopc2dts.lib.boardinfo.SpiSlaveMMC;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.altera.SICEpcs;

public class BISSpi extends BISSubComponentTable {
	private static final long serialVersionUID = -417271329560901791L;

	public BISSpi() {
		super("SPI","spi",false);
	}
	
	protected boolean matchComponent(BasicComponent comp)
	{
		if(comp instanceof SICEpcs)
		{
			return false;
		} else {
			return super.matchComponent(comp);
		}
	}
	@Override
	protected void addSubComponent(TableModel mod)
	{
		String choice = (String)JOptionPane.showInputDialog(this, "Please select the type of SPI-slave to add", 
				"Select type", JOptionPane.QUESTION_MESSAGE, null, SpiSlave.slaveTypeNames, null);
		if(choice != null) {
			SpiTableModel stm = (SpiTableModel)mod;
			if(choice.equals(SpiSlave.slaveTypeNames[0]))
			{
				stm.addSlave(new SpiSlaveMMC(stm.vSlaves.size()));
			} else {
				stm.addSlave(new SpiSlave("custom",stm.vSlaves.size(),"unknown,unknown",0));
			}
		}
	}

	protected void addSubComponent(TableModel mod, String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void removeSubComponent(TableModel mod, int index) {
		((SpiTableModel)mod).removeSlave(index);
	}

	@Override
	protected TableModel tableModelForComponent(BasicComponent comp) {
		currComp = comp;
		if(comp!=null)
		{
			BoardInfoComponent bic = bInfo.getBicForChip(comp.getInstanceName());
			if(bic != null) 
			{
				if(bic instanceof BICSpi)
				{
					return new SpiTableModel((BICSpi)bic);
				}
			}
			return new SpiTableModel(new BICSpi(comp.getInstanceName()));
		}
		return null;
	}

	@Override
	protected boolean isSaveNeeded(TableModel tm) {
		// TODO Auto-generated method stub
		return (currComp != null);
	}

	@Override
	protected void save(BoardInfo bi, TableModel tm) {
		if(currComp!=null)
		{
			BICSpi bs = (BICSpi)bi.getBicForChip(currComp.getInstanceName());
			if(bs == null)
			{
				bs = new BICSpi(currComp.getInstanceName());
				bi.setBic(bs);
			}
			bs.setSlaves(((SpiTableModel)tm).vSlaves);
		}
	}
	protected class SpiTableModel implements TableModel {
		Vector<TableModelListener> vListeners = new Vector<TableModelListener>();
		Vector<SpiSlave> vSlaves;
		public SpiTableModel(BICSpi bs)
		{
			vSlaves = new Vector<SpiSlave>(bs.getSlaves());
		}
		public void addTableModelListener(TableModelListener l) {
			vListeners.add(l);
		}
		protected void addSlave(SpiSlave slave) {
			vSlaves.add(slave);
			for(TableModelListener l : vListeners)
			{
				l.tableChanged(new TableModelEvent(this));
			}
		}
		protected void removeSlave(int index) {
			vSlaves.remove(index);
			for(TableModelListener l : vListeners)
			{
				l.tableChanged(new TableModelEvent(this));
			}
		}
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
			case 0: return Integer.class;
			case 1: return String.class;
			case 2: return String.class;
			case 3: return Integer.class;
			case 4: return Boolean.class;
			case 5: return Boolean.class;
			case 6: return Boolean.class;
			default: return String.class;
			}
		}

		public int getColumnCount() {
			return 7;
		}

		public String getColumnName(int columnIndex) {
			switch(columnIndex) {
			case 0: return "Reg";
			case 1: return "Name";
			case 2: return "Compatible";
			case 3: return "Max Speed";
			case 4: return "CPOL";
			case 5: return "CPHA";
			case 6: return "CS active high";
			default: return null;
			}
		}

		public int getRowCount() {
			return vSlaves.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			SpiSlave slave = vSlaves.get(rowIndex);
			switch(columnIndex) {
			case 0: return slave.getReg();
			case 1: return slave.getName();
			case 2: return slave.getCompatible();
			case 3: return slave.getSpiMaxFrequency();
			case 4: return slave.isCpol();
			case 5: return slave.isCpha();
			case 6: return slave.isCsHigh();
			default: return null;
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if(columnIndex == 0)
			{
				return true; //Reg is always changeable
			}
			SpiSlave slave = vSlaves.get(rowIndex);
			if(!(slave instanceof SpiSlaveMMC))
			{
				return true;
			}
			return false;
		}

		public void removeTableModelListener(TableModelListener l) {
			vListeners.remove(l);
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			SpiSlave slave = vSlaves.get(rowIndex);
			switch(columnIndex) {
			case 0: slave.setReg((Integer)aValue); break;
			case 1: slave.setName((String)aValue); break;
			case 2: slave.setCompatible((String)aValue); break;
			case 3: slave.setSpiMaxFrequency((Integer)aValue); break;
			case 4: slave.setCpol((Boolean)aValue); break;
			case 5: slave.setCpha((Boolean)aValue); break;
			case 6: slave.setCsHigh((Boolean)aValue); break;
			default:
			}
		}
		
	}
}
