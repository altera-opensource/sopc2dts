/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 - 2012 Walter Goossens <waltergoossens@home.nl>

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
package sopc2dts.lib.components;

import java.util.Vector;

import sopc2dts.lib.AvalonSystem.SystemDataType;

public class SopcComponentDescription {
	protected String[] classNames;
	protected String group;
	protected String vendor;
	protected String device;
	private Vector<String> vCompatible = new Vector<String>();
	private Vector<SICAutoParam> vAutoParams = new Vector<SICAutoParam>();
	Vector<SICRequiredParam> vRequiredParams = new Vector<SICRequiredParam>();
	Vector<String> vCompatibleVersions = new Vector<String>();
	Vector<TransparentInterfaceBridge> vTransparentBridges = new Vector<TransparentInterfaceBridge>();
	
	public SopcComponentDescription(String cn, String grp, String vnd, String dev)
	{
		classNames = cn.split(",");
		for(String name : classNames)
		{
			name = name.trim();
		}
		group = grp;
		vendor = vnd;
		device = dev;
	}
	public class SICAutoParam {
		private String dtsName;
		private String sopcInfoName;
		private String fixedValue;
		private String forceType;
		
		public SICAutoParam(String dts, String sopcInfo, String type, String fixedValue)
		{
			setDtsName(dts);
			setSopcInfoName(sopcInfo);
			setFixedValue(fixedValue);
			setForceType(type);
		}

		public SICAutoParam(String dts, String sopcInfo, String type)
		{
			this(dts, sopcInfo, type, null);
		}

		public void setSopcInfoName(String sopcInfoName) {
			this.sopcInfoName = sopcInfoName;
		}

		public String getSopcInfoName() {
			return sopcInfoName;
		}

		public void setFixedValue(String value) {
			this.fixedValue = value;
		}
		public String getFixedValue() {
			return fixedValue;
		}

		public void setDtsName(String dtsName) {
			this.dtsName = dtsName;
		}

		public String getDtsName() {
			return dtsName;
		}

		public void setForceType(String forceType) {
			this.forceType = forceType;
		}

		public String getForceType() {
			return forceType;
		}
	}
	protected class SICRequiredParam {
		String name;
		String value;
		public SICRequiredParam(String n, String v)
		{
			name = n;
			value = v;
		}
	}
	public class TransparentInterfaceBridge {
		String masterIntfName;
		String slaveIntfName;
		SystemDataType type;
		public TransparentInterfaceBridge(SystemDataType t)
		{
			type = t;
		}
		public TransparentInterfaceBridge(String master, String slave)
		{
			masterIntfName = master;
			slaveIntfName = slave;
		}
		public String getMasterIntfName() {
			return masterIntfName;
		}
		public String getSlaveIntfName() {
			return slaveIntfName;
		}
	}
	public void addAutoParam(String dtsName, String sopcName, String type) {
		vAutoParams.add(new SICAutoParam(dtsName, sopcName, type));
	}

	public void addAutoParam(String dtsName, String sopcName, String type, String value) {
		vAutoParams.add(new SICAutoParam(dtsName, sopcName, type, value));
	}

	public void addRequiredParam(String dtsName, String sopcName) {
		vRequiredParams.add(new SICRequiredParam(dtsName, sopcName));
	}
	public Vector<SICRequiredParam> getRequiredParams()
	{
		return vRequiredParams;
	}
	//XXX can this be done in one call?
	public boolean isRequiredParamsOk(BasicComponent comp)
	{
		for(SICRequiredParam rp : vRequiredParams)
		{
			if(comp.getParamByName(rp.name)==null) return false;
			if(!comp.getParamValByName(rp.name).equalsIgnoreCase(rp.value)) return false;
		}
		return true;
	}
	public void setGroup(String group) {
		this.group = group;
	}

	public String getGroup() {
		return group;
	}

	public String[] getClassNames() {
		return classNames;
	}
	public boolean isSupportingClassName(String cn)
	{
		for(String name : classNames)
		{
			if(name.equalsIgnoreCase(cn))
			{
				return true;
			}
		}
		return false;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getVendor() {
		return vendor;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getDevice() {
		return device;
	}

	public void addCompatible(String compat)
	{
		vCompatible.add(compat);
	}

	public void addCompatibleVersion(String compat)
	{
		vCompatibleVersions.add(compat);
	}
	
	public void setCompatible(Vector<String> vCompatible) {
		this.vCompatible = vCompatible;
	}
	protected int compareVersions(String v1, String v2)
	{
		String[] v1Parts = v1.split("\\.");
		String[] v2Parts = v2.split("\\.");
		int diff = 0;
//		System.out.println("Compare " + v1 + " to " + v2);
		for(int iPart = 0; (iPart<v1Parts.length) && (iPart<v2Parts.length) && (diff==0); iPart++)
		{
//			System.out.println("ComparePart " + v1Parts[iPart] + " to " + v2Parts[iPart]);
			try {
				int num1 = Integer.decode(v1Parts[iPart]);
				int num2 = Integer.decode(v2Parts[iPart]);
				diff = num2-num1;
			} catch (NumberFormatException e)
			{
				//Well then it's characters :)
				diff = v1Parts[iPart].compareToIgnoreCase(v2Parts[iPart]);
			}
		}
		if(diff == 0)
		{
			diff = v2Parts.length - v1Parts.length;
		}
		if(diff == 0)
		{
			diff = v2.length() - v1.length();
		}
		return diff;
	}
	protected String getCompatibleVersion(String version)
	{
		String compat = null;
		for(String bwCompatVersion : vCompatibleVersions)
		{
			if(version.equalsIgnoreCase(bwCompatVersion)) return null;
			if((compat==null)||(compareVersions(bwCompatVersion,compat)<0))
			{
				if(compareVersions(bwCompatVersion,version)>0)
				{
					compat = bwCompatVersion;
				}
			}
		}
		return compat;
	}
	public String getCompatible(String version) {
		String res = "";
		Vector<String> vCompatibles = getCompatibles(version);
		for(String comp : vCompatibles)
		{
			if(!res.isEmpty())
			{
				res += ',';
			}
			res += "\"" + comp + "\"";
		}
		return res;
	}
	public Vector<String> getCompatibles(String version) {
		Vector<String> vRes = new Vector<String>();
		String res = vendor + ',' + device;
		if(version!=null)
		{
			res += '-' + version;
			vRes.add(res);			
			String bwCompatVersion = getCompatibleVersion(version);
			if(bwCompatVersion!=null)
			{
				res = vendor + ',' + device + '-' + bwCompatVersion;
				vRes.add(res);
			}
		} else {
			vRes.add(res);
		}
		vRes.addAll(vCompatible);
		return vRes;
	}

	public void setAutoParams(Vector<SICAutoParam> vAutoParams) {
		this.vAutoParams = vAutoParams;
	}

	public Vector<SICAutoParam> getAutoParams() {
		return vAutoParams;
	}

	public Vector<TransparentInterfaceBridge> getTransparentBridges() {
		return vTransparentBridges;
	}
}
