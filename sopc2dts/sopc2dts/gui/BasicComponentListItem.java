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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.base.SCDSelfDescribing;
import sopc2dts.lib.components.base.SICUnknown;

public class BasicComponentListItem extends JPanel {
	private static final long serialVersionUID	= 3548406165403921773L;

	public BasicComponentListItem(BasicComponent bc, boolean selected)
	{
		super();
		this.setLayout(new GridLayout(3,2));
		setPreferredSize(new Dimension(270, 80));
		this.setBorder(BorderFactory.createTitledBorder(bc.getInstanceName()));
		if(bc.getScd() instanceof SCDSelfDescribing)
		{
			this.setBackground(new Color(0, 255, 0, (selected ? 128 : 64)));
		} else if(bc.getScd() instanceof SICUnknown)
		{
			this.setBackground(new Color(255, 0, 0, (selected ? 128 : 64)));
		} else {
			this.setOpaque(selected);
		}
//		this.add(new JLabel("Name"));
//		this.add(new JLabel(bc.getInstanceName()));
		this.add(new JLabel("Type"));
		this.add(new JLabel(bc.getScd().getDevice()));
		this.add(new JLabel("Group"));
		this.add(new JLabel(bc.getScd().getGroup()));
		this.add(new JLabel("SOPC-Class"));
		this.add(new JLabel(bc.getScd().getClassName()));
	}
}
