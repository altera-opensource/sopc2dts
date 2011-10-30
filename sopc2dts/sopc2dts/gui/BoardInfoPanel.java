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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.gui.boardinfo.BISFlash;
import sopc2dts.gui.boardinfo.BISGeneral;
import sopc2dts.gui.boardinfo.BISI2C;
import sopc2dts.gui.boardinfo.BISEthernet;
import sopc2dts.gui.boardinfo.BISSpi;
import sopc2dts.gui.boardinfo.BoardInfoSubPanel;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;

public class BoardInfoPanel extends ThreadedLoadPanel {
	private static final long serialVersionUID = 3765844437274914520L;
	JTabbedPane jtp = new JTabbedPane();
	JButton btnApply = new JButton("Apply");
	JButton btnRevert = new JButton("Revert");
	JButton btnSave = new JButton("Save");
	BoardInfoSubPanel[] pnlBIS = {
			new BISGeneral(),
			new BISEthernet(),
			new BISFlash(),
			new BISI2C(),
			new BISSpi(),
	};
	public BoardInfoPanel(String fName, Sopc2DTSGui parent)
	{
		super(fName, parent, false);
		for(BoardInfoSubPanel bis : pnlBIS)
		{
			jtp.addTab(bis.getName(), bis);
		}
		btnApply.addActionListener(this);
		btnRevert.addActionListener(this);
		btnSave.addActionListener(this);
		jtp.setEnabled(false);
		JPanel pnlBottom = new JPanel();
		pnlBottom.add(btnApply);
		pnlBottom.add(btnRevert);
		pnlBottom.add(btnSave);
		this.add(jtp, BorderLayout.CENTER);
		this.add(pnlBottom, BorderLayout.SOUTH);
	}
	private void bisSaveAll(BoardInfo bi)
	{
		for(BoardInfoSubPanel bis : pnlBIS)
		{
			bis.save(bi);
		}
		mainGui.setBoardInfo(bi);
	}
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(btnApply))
		{
			bisSaveAll(mainGui.getBoardInfo());
		} else if(e.getSource().equals(btnRevert))
		{
			pnlBIS[jtp.getSelectedIndex()].load(mainGui.getBoardInfo());
		} else if(e.getSource().equals(btnSave))
		{
			if(txtFileName.getText().length()==0)
			{
				JOptionPane.showMessageDialog(this, "Please enter filename to save to","Save Boardinfo",JOptionPane.WARNING_MESSAGE);
			} else {
				BoardInfo bi = mainGui.getBoardInfo();
				bisSaveAll(bi);
				
				BufferedWriter out;
				try {
					out = new BufferedWriter(new FileWriter(txtFileName.getText()));
					out.write(bi.getXml());
					out.close();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(this, "Error '" + e1.getLocalizedMessage() + "'" +
							" while trying to save to '" + txtFileName.getText() + "'", 
							"Boardinfo save failed", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else {
			super.actionPerformed(e);
		}
	}
	@Override
	protected void threadedLoadFile(File f) {
		if(f.exists())
		{
			try {
				mainGui.setBoardInfo(new BoardInfo(new InputSource(new BufferedReader(new FileReader(f)))));
				return;
			} catch (FileNotFoundException e) {
				Logger.logln(e.getMessage(), LogLevel.ERROR);
			} catch (SAXException e) {
				Logger.logln(e.getMessage(), LogLevel.ERROR);
			} catch (IOException e) {
				Logger.logln(e.getMessage(), LogLevel.ERROR);
			}
		} else {
			Logger.logln("Boardinfo " + f.getName() + " does not exist", LogLevel.WARNING);
		}
		mainGui.setBoardInfo(null);
	}
	public void setBoardInfoAndSys(BoardInfo bi, AvalonSystem sys)
	{
		jtp.setEnabled((bi != null));
		for(int i=0; i<pnlBIS.length; i++)
		{
			if(pnlBIS[i].setBoardInfoAndSys(bi, sys))
			{
				pnlBIS[i].setGuiEnabled(true);
				jtp.setEnabledAt(i, true);
			} else {
				pnlBIS[i].setGuiEnabled(false);
				jtp.setEnabledAt(i, false);
			}
		}
	}
}
