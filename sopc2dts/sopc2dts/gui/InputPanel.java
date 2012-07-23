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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import sopc2dts.Logger;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.parsers.BasicSystemLoader;

public class InputPanel extends ThreadedLoadPanel {
	private static final long serialVersionUID = -2627231677508843875L;
	JTextArea txtSource = new JTextArea();
	JList componentList = new JList();

	public InputPanel(String fName, Sopc2DTSGui parent)
	{
		super(fName, parent);
		componentList.setCellRenderer(new BasicComponentRenderer());
		JScrollPane jsp = new JScrollPane(componentList);
		jsp.setPreferredSize(new Dimension(300, 400));
		jsp.setBorder(BorderFactory.createTitledBorder("Component list"));
		this.add(jsp, BorderLayout.EAST);
		jsp = new JScrollPane(txtSource);
		jsp.setBorder(BorderFactory.createTitledBorder("Input source"));
		this.add(jsp, BorderLayout.CENTER);
	}
	
	public void setSys(AvalonSystem sys) {
		mainGui.setSys(sys);
		if(sys!=null)
		{
			componentList.setListData(sys.getSystemComponents());
		}
	}
	@Override
	protected void threadedLoadFile(File f) {
		componentList.setListData(new Object[]{});
		mainGui.clearLog();
	    StringBuilder contents = new StringBuilder();
	    try {
	      BufferedReader input =  new BufferedReader(new FileReader(f));
	      try {
	        String line = null;
	        while (( line = input.readLine()) != null){
	          contents.append(line).append(System.getProperty("line.separator"));
	        }
	      }
	      finally {
	        input.close();
	      }
	    }
	    catch (IOException ex){
	      Logger.logException(ex);
	    }
	    txtSource.setText(contents.toString());
		setSys(BasicSystemLoader.loadSystem(f));
	}
}
