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
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import sopc2dts.LogListener;
import sopc2dts.Logger;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;

public class Sopc2DTSGui extends JFrame implements LogListener {
	private static final long serialVersionUID = 8613192420393857538L;
	JTabbedPane jtp = new JTabbedPane();
	JTextArea txtLog = new JTextArea();
	InputPanel pnlInput;
	OutputPanel pnlOutput = new OutputPanel();
	private AvalonSystem sys;
	private BoardInfo boardInfo;
	
	public Sopc2DTSGui(String inpFile, BoardInfo bInfo) {
		super("Sopc2DTS");
		boardInfo = bInfo;
		Logger.addLogListener(this);
		Logger.setUseStdOutErr(false);
		pnlInput = new InputPanel(inpFile, this);
		jtp.addTab("Input", pnlInput);
//		jtp.addTab("Settings", new JPanel());
//		jtp.addTab("Boardinfo", new JPanel());
		jtp.addTab("Output", pnlOutput);
		txtLog.setEditable(false);
		JScrollPane jsp = new JScrollPane(txtLog);
		jsp.setPreferredSize(new Dimension(800,200));
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(jsp, BorderLayout.SOUTH);
		this.getContentPane().add(jtp, BorderLayout.CENTER);
		this.pack();
	}
	public void clearLog()
	{
		txtLog.setText("");
	}
	public void messageLogged(String log) {
		txtLog.setText(txtLog.getText() + log);
	}
	public void setSys(AvalonSystem sys) {
		this.sys = sys;
		pnlOutput.setSys(sys);
	}
	public AvalonSystem getSys() {
		return sys;
	}
	
}
