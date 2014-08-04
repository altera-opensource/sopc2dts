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
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;

public abstract class ThreadedLoadPanel  extends JPanel implements ActionListener {
	private static final long serialVersionUID = 6543239157358674037L;
	JButton btnChoose = new JButton("Choose file");
	JTextField txtFileName = new JTextField();
	JButton btnLoad = new JButton("Load file");
	Sopc2DTSGui mainGui;
	public ThreadedLoadPanel(String fName, Sopc2DTSGui parent)
	{
		this(fName,parent,true);
	}
	public ThreadedLoadPanel(String fName, Sopc2DTSGui parent, boolean autoload)
	{
		mainGui = parent;
		txtFileName.setText(fName);
		btnChoose.addActionListener(this);
		btnLoad.addActionListener(this);
		JPanel pnlTop = new JPanel(new BorderLayout());
		pnlTop.add(btnChoose, BorderLayout.WEST);
		pnlTop.add(btnLoad, BorderLayout.EAST);
		pnlTop.add(txtFileName, BorderLayout.CENTER);
		this.setLayout(new BorderLayout());
		this.add(pnlTop, BorderLayout.NORTH);
		if(autoload && (fName!=null))
		{
			loadFile(fName,500);
		}
	}
	protected abstract void threadedLoadFile(File f);
	
	void loadFile(String fName, int delay)
	{
		File f = new File(fName);
		if(f.exists())
		{
			Thread t = new Thread(new ThreadedLoader(f,delay));
			t.start();
		} else {
			Logger.logln("Refusing to load " + fName + ". It does not exist", 
					LogLevel.WARNING);
		}
	}
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(btnLoad))
		{
			loadFile(txtFileName.getText(), 0);
		} else if(e.getSource().equals(btnChoose))
		{
			JFileChooser jfc = new JFileChooser(".");
			if(jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			{
				txtFileName.setText(jfc.getSelectedFile().getAbsolutePath());
				loadFile(jfc.getSelectedFile().getAbsolutePath(), 0);
			}
		}
	}
	protected class ThreadedLoader implements Runnable {
		File f;
		int delayMillis;
		protected ThreadedLoader(File inp)
		{
			this(inp, 0);
		}
		protected ThreadedLoader(File inp, int delay)
		{
			f = inp;
			delayMillis = delay;
		}
		public void run() {
			if(delayMillis>0)
			{
				try {
					Thread.sleep(delayMillis);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			threadedLoadFile(f);
		}
		
	}
}
