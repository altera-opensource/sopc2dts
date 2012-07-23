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
package sopc2dts.parsers.ptf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.Connection;
import sopc2dts.lib.AvalonSystem.SystemDataType;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;

public class PtfSystemLoader {
	AvalonSystem currSys;
	
	public AvalonSystem loadSystem(File source) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(source));
			String line;
			PtfHandler h = null;
			while((line = in.readLine())!=null)
			{
				line = line.trim();
				if(h != null)
				{
					h = h.handle(line);
				} else if(line.split(" ")[0].equalsIgnoreCase("SYSTEM"))
				{
					currSys = new AvalonSystem(line.split(" ")[1], "", source);
					h = new PtfHandler(null, currSys, "SYSTEM");
				} else {
					Logger.logln("PTF: Unexpected line: " + line, LogLevel.DEBUG);
				}
			}
		} catch (FileNotFoundException e) {
			Logger.logln("Error \'" + e.getMessage() + "\' while loading ptf file",
					LogLevel.ERROR);
		} catch (IOException e) {
			Logger.logln("Error \'" + e.getMessage() + "\' while loading ptf file",
					LogLevel.ERROR);
		}
		for(BasicComponent c : currSys.getSystemComponents())
		{
			
			for(int intfNum = 0; intfNum < c.getInterfaces().size(); intfNum++)
			{
				Interface intf = c.getInterfaces().get(intfNum);
				int i=0;
				while(i<intf.getParams().size())
				{
					if(intf.getParams().get(i).getName().equals("PTFConnection"))
					{
						String connName = intf.getParams().get(i).getValue();
						if(connName.startsWith("MASTERED_BY") && (connName.indexOf(' ')!=-1))
						{
							connectMemoryMapped(intf, connName);
						} else if(connName.startsWith("IRQ_MASTER") && (connName.indexOf(' ')!=-1))
						{
							connectIRQ(c, intf, connName);
						} else {
							Logger.logln("Conn " + connName + 
									" on " + intf.getName() + 
									" of " + c.getInstanceName(), LogLevel.DEBUG);						
						}
						intf.getParams().remove(i);
					} else {
						i++;
					}
				}
			}
		}
		currSys.recheckComponents();
		return currSys;
	}
	protected void connectIRQ(BasicComponent comp, Interface intf, String connName)
	{
		String masterName = connName.split(" ")[1];
		Long conVal = -1l;
		if(connName.split(" ").length==3)
		{
			String sconVal = connName.split(" ")[2];
			if(!sconVal.equals("NC"))
			{
				try {
					conVal = Long.decode(sconVal);
				} catch(NumberFormatException e) { 
					Logger.logln("Failed to parse IRQ-nr: '" + sconVal + '\'' +
							" for " + comp.getInstanceName(), LogLevel.ERROR);
				}
			}
		}
		String masterComp = masterName.split("/")[0];
		BasicComponent irqMaster = currSys.getComponentByName(masterComp);
		if((irqMaster!=null)&&(conVal>=0))
		{
			Interface irqMI = irqMaster.getInterfaceByName("PtfIrqMaster");
			if(irqMI==null)
			{
				irqMI = new Interface("PtfIrqMaster", SystemDataType.INTERRUPT, true, irqMaster);
				irqMaster.addInterface(irqMI);
			}
			Interface irqSI = comp.getInterfaceByName("PtfIrqSlave");
			if(irqSI==null)
			{
				irqSI = new Interface("PtfIrqSlave", SystemDataType.INTERRUPT, false, irqMaster);
				comp.addInterface(irqSI);
			}
			Connection conn = new Connection(irqMI, irqSI, SystemDataType.INTERRUPT, true);
			conn.setConnValue(conVal);
		}		
	}
	protected void connectMemoryMapped(Interface intf, String connName)
	{
		String masterName = connName.split(" ")[1];
		String conVal = null;
		if(connName.split(" ").length==3)
		{
			conVal = connName.split(" ")[2];
		}
		if(masterName.indexOf('/')!=-1)
		{
			String masterComp = masterName.split("/")[0];
			String masterIntf = masterName.split("/")[1];
			BasicComponent master = currSys.getComponentByName(masterComp);
			if(master!=null)
			{
				Interface mi = master.getInterfaceByName(masterIntf);
				if(mi!=null)
				{
					Connection conn = new Connection(mi, intf, intf.getType(),true);
					if(conVal!=null)
					{
						try {
							conn.setConnValue(Long.decode(conVal));
						} catch(NumberFormatException e) { 
							Logger.logln("Failed to parse baseAddress: '" + conVal + '\'' +
									" for link between " + master.getInstanceName() +
									" and " + intf.getOwner().getInstanceName(), LogLevel.ERROR);
						}
					}
				}
			}
		}
	}
}
