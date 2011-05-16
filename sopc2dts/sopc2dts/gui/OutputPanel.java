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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import sopc2dts.generators.DTSGenerator;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;

public class OutputPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 8995906903008952772L;
	JTextArea txtDts = new JTextArea();
	JButton saveDTS = new JButton("Save as DTS");
	AvalonSystem sys;
	BoardInfo bi;
	
	public OutputPanel()
	{
		txtDts.setEditable(false);
		txtDts.setTabSize(4);
		JPanel pnlTop = new JPanel();
		saveDTS.addActionListener(this);
		pnlTop.add(saveDTS);
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(txtDts), BorderLayout.CENTER);
		this.add(pnlTop, BorderLayout.NORTH);
	}
	void updateDts()
	{
		if(sys!=null)
		{
			DTSGenerator gen = new DTSGenerator(sys);
			txtDts.setText(gen.getOutput((bi == null ? new BoardInfo() : bi)));
		} else {
			txtDts.setText("");
		}
	}
	public void setSys(AvalonSystem s)
	{
		sys = s;
		updateDts();
	}
	public void setBoardInfo(BoardInfo b)
	{
		bi = b;
		updateDts();
	}
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(saveDTS))
		{
			JFileChooser jfc = new JFileChooser();
			if(jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
			{
				File fOut = jfc.getSelectedFile();
				try {
					fOut.createNewFile();
					BufferedWriter out = new BufferedWriter(new FileWriter(fOut));
					out.write(txtDts.getText());
					out.close();
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}
		}
	}
}
