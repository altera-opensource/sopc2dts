package sopc2dts.gui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import sopc2dts.lib.components.BasicComponent;

public class BasicComponentRenderer implements ListCellRenderer {

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if(value instanceof BasicComponent)
		{
			BasicComponent bc = (BasicComponent)value;
			return new BasicComponetListItem(bc,isSelected);
		} else {
			return new JLabel(value.toString());
		}
	}

}
