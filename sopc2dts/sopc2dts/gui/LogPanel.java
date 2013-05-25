/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2013 Walter Goossens <waltergoossens@home.nl>

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import sopc2dts.LogEntry;
import sopc2dts.LogListener;
import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;

public class LogPanel  extends JPanel implements LogListener {
	private static final long serialVersionUID = -2484383142821969553L;
	JButton btnClear = new JButton("clear");
	DefaultListModel lm = new DefaultListModel();
	JList logList = new JList(lm);

	public LogPanel() {
		logList.setCellRenderer(new LogListCellRenderer());
		Logger.addLogListener(this);
		this.setLayout(new BorderLayout());
		JPanel pnlButtons = new JPanel();
		pnlButtons.add(btnClear);
		this.add(pnlButtons, BorderLayout.NORTH);
		this.add(new JScrollPane(logList));
	}
	public void messageLogged(LogEntry log) {
		lm.addElement(log);
	}
	class LogListCellRenderer implements ListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object val,
				int idx, boolean selected, boolean focus) {
			LogEntry log = (LogEntry)val;
			JLabel lbl = new JLabel(log.getMessage());
			if(selected)
				lbl.setBackground(list.getSelectionBackground());
			lbl.setForeground(logLevel2Color(log.getLevel()));
			return lbl;
		}
		public Color logLevel2Color(LogLevel ll) {
			switch(ll) {
			case ERROR:		return Color.RED;
			case INFO:		return Color.GREEN;
			case WARNING:	return Color.BLUE;
			case DEBUG:		return Color.GRAY;
			default:
				return Color.BLACK;
			}
		}
	}
}
