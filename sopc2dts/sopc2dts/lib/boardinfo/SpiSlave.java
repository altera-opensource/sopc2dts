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
package sopc2dts.lib.boardinfo;

import org.xml.sax.Attributes;

import sopc2dts.generators.AbstractSopcGenerator;

public class SpiSlave {
	public static String[] slaveTypeNames = {
		"MMC Slot",
		"Custom",
	};
	int spiMaxFrequency = 10000000;	//10MHz
	int reg;
	String name;
	String compatible;
	boolean cpol;
	boolean cpha;
	boolean csHigh;

	public SpiSlave(Attributes atts)
	{
		reg = Integer.decode(atts.getValue("reg"));
		name = atts.getValue("name");
		compatible = atts.getValue("compatible");
		spiMaxFrequency = Integer.decode(atts.getValue("maxfreq"));
		cpol = Boolean.getBoolean(atts.getValue("cpol"));
		cpha = Boolean.getBoolean(atts.getValue("cpha"));
		csHigh = Boolean.getBoolean(atts.getValue("csHigh"));
	}
	public SpiSlave(String name, int reg, String compat)
	{
		this(name,reg,compat,10000000,false,false,false);
	}
	
	public SpiSlave(String name, int reg, String compat, int max_freq)
	{
		this(name,reg,compat,max_freq,false,false,false);
	}
	
	protected SpiSlave(String name, int reg, String compat, int max_freq, 
			boolean cpol, boolean cpha, boolean csHigh)
	{
		this.name = name;
		this.reg = reg;
		this.compatible = compat;
		this.spiMaxFrequency = max_freq;
		this.cpol = cpol;
		this.cpha = cpha;
		this.csHigh = csHigh;
	}
	
	public String getXml() {
		return "\t\t<SpiSlave reg=\"" + reg + "\" name=\"" + name + "\"" +
				" class=\"" + this.getClass().getCanonicalName() + "\"" +
				" compatible=\"" + compatible +"\" maxfreq=\"" + spiMaxFrequency + "\"" +
				" cpol=\"" + cpol + "\" cpha=\"" + cpha + "\" csHigh=\"" + csHigh + "\">" +
				"</SpiSlave>\n";
	}
	public String toDts(int indentLevel)
	{
		String res = AbstractSopcGenerator.indent(indentLevel++) + name + '@' + reg + " {\n" +
					AbstractSopcGenerator.indent(indentLevel) + "compatible = \"" + compatible + "\";\n" +
					AbstractSopcGenerator.indent(indentLevel) + "spi-max-frequency = <" + spiMaxFrequency + ">;\n" +
					AbstractSopcGenerator.indent(indentLevel) + "reg = <" + reg + ">;\n";
		if(cpol)
		{
			res += AbstractSopcGenerator.indent(indentLevel) + "spi-cpol;\n";
			
		}
		if(cpha)
		{
			res += AbstractSopcGenerator.indent(indentLevel) + "spi-cpha;\n";
			
		}
		if(csHigh)
		{
			res += AbstractSopcGenerator.indent(indentLevel) + "spi-cs-high;\n";
			
		}
		res += toDtsExtras(indentLevel);
		res += AbstractSopcGenerator.indent(--indentLevel) + "};\n";
		return res;
	}
	public String toDtsExtras(int indentLevel)
	{
		return "";
	}
	public int getSpiMaxFrequency() {
		return spiMaxFrequency;
	}
	public void setSpiMaxFrequency(int spiMaxFrequency) {
		this.spiMaxFrequency = spiMaxFrequency;
	}
	public int getReg() {
		return reg;
	}
	public void setReg(int reg) {
		this.reg = reg;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCompatible() {
		return compatible;
	}
	public void setCompatible(String compatible) {
		this.compatible = compatible;
	}
	public boolean isCpol() {
		return cpol;
	}
	public void setCpol(boolean cpol) {
		this.cpol = cpol;
	}
	public boolean isCpha() {
		return cpha;
	}
	public void setCpha(boolean cpha) {
		this.cpha = cpha;
	}
	public boolean isCsHigh() {
		return csHigh;
	}
	public void setCsHigh(boolean csHigh) {
		this.csHigh = csHigh;
	}
}
