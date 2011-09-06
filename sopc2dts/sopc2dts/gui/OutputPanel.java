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

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import sopc2dts.Logger;
import sopc2dts.generators.AbstractSopcGenerator;
import sopc2dts.generators.GeneratorFactory;
import sopc2dts.generators.GeneratorFactory.GeneratorType;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;

public class OutputPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 8995906903008952772L;
	JTextArea txtDts = new JTextArea();
	JButton saveAs = new JButton("Save as");
	GeneratorType genType;
	AvalonSystem sys;
	BoardInfo bi;
	
	public OutputPanel(GeneratorType outputType)
	{
		genType = outputType;
		txtDts.setEditable(false);
		txtDts.setTabSize(4);
		JPanel pnlTop = new JPanel();
		JPanel pnlLeft = new JPanel();
		pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));
		ButtonGroup bGroup = new ButtonGroup();
		for(GeneratorType gt : GeneratorType.values())
		{
			JRadioButton jrb = new JRadioButton(GeneratorFactory.getGeneratorNameByType(gt));
			if(gt == outputType)
			{
				jrb.setSelected(true);
			}
			jrb.addActionListener(this);
			pnlLeft.add(jrb);
			bGroup.add(jrb);
			JLabel lbl = new JLabel(GeneratorFactory.getGeneratorDescriptionByType(gt));
			pnlLeft.add(lbl);
		}
		saveAs.addActionListener(this);
		pnlTop.add(saveAs);
		this.setLayout(new BorderLayout());
		this.add(pnlTop, BorderLayout.NORTH);
		this.add(pnlLeft, BorderLayout.WEST);
		this.add(new JScrollPane(txtDts), BorderLayout.CENTER);
	}
	void updateDts()
	{
		if(sys!=null)
		{
			AbstractSopcGenerator gen = GeneratorFactory.createGeneratorFor(sys, genType);
			if(gen!=null)
			{
				if(gen.isTextOutput())
				{
					txtDts.setText(gen.getTextOutput((bi == null ? new BoardInfo() : bi)));
				} else {
					txtDts.setText("TBD");
				}
			} else {
				txtDts.setText("");
			}
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
		if(e.getSource().equals(saveAs))
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
					Logger.logException(ex);
				}
			}
		} else if(e.getSource() instanceof JRadioButton)
		{
			JRadioButton jrb = (JRadioButton)e.getSource();
			genType = GeneratorFactory.getGeneratorTypeByName(jrb.getText());
			updateDts();
		}
	}
}
