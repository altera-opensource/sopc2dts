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
package sopc2dts.gui.boardinfo;

import javax.swing.JPanel;

import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.BoardInfo;

public abstract class BoardInfoSubPanel extends JPanel {
	private static final long serialVersionUID = -663003741974804172L;
	protected BoardInfo bInfo;
	AvalonSystem sys;
	
	public BoardInfoSubPanel(String name) {
		this.setName(name);
	}
	public boolean setBoardInfoAndSys(BoardInfo bi, AvalonSystem s)
	{
		bInfo = bi;
		sys = s;
		load(bi);
		if(bInfo != null)
		{
			return true;
		} else {
			return false;
		}
	}
	public abstract void save(BoardInfo bi);
	public abstract void load(BoardInfo bi);
	public abstract void setGuiEnabled(boolean ena);
}
