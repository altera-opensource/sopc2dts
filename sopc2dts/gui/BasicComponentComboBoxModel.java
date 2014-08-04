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
package sopc2dts.gui;

import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.components.BasicComponent;

public class BasicComponentComboBoxModel extends AbstractListModel implements ComboBoxModel {
	private static final long serialVersionUID = 7471198219999627210L;
	Vector<BasicComponent> vList;
	String selected;
	
	public BasicComponentComboBoxModel(AvalonSystem sys, boolean mastersOnly, String selectedCompName)
	{
		if(sys==null)
		{
			vList = new Vector<BasicComponent>();
		} else {
			if(mastersOnly)
			{
				vList = sys.getMasterComponents();
			} else {
				vList = sys.getSystemComponents();
			}
			if(selectedCompName!=null)
			{
				BasicComponent comp = sys.getComponentByName(selectedCompName);
				if(comp!=null)
				{
					if(vList.contains(comp))
					{
						selected = basicComponent2Item(comp);
					}
				}
			}
		}
	}
	public String basicComponent2Item(BasicComponent comp)
	{
		return comp.getScd().getGroup() + ": " + comp.getInstanceName();
	}
	public Object getElementAt(int index) {
		return basicComponent2Item(vList.get(index));
	}

	public int getSize() {
		return vList.size();
	}

	public Object getSelectedItem() {
		return selected;
	}

	public void setSelectedItem(Object anItem) {
		selected = anItem.toString();
	}

}
