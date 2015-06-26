/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 - 2015 Walter Goossens <waltergoossens@home.nl>

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

public class BICEthernet extends BoardInfoComponent {
	public static final String TAG_NAME = "Ethernet";
	Integer miiID = null;
	Integer phyID = null;
	int[] mac = { 0, 0, 0, 0, 0, 0 };

	public BICEthernet(String iName) {
		super(TAG_NAME, iName);
	}
	public BICEthernet(String tag, Attributes atts) {
		super(tag, atts);
		String sVal = atts.getValue("mii_id"); 
		if(sVal != null)
		{
			miiID = Integer.decode(sVal);
		}
		sVal = atts.getValue("phy_id"); 
		if(sVal != null)
		{
			phyID = Integer.decode(sVal);
		}
		setMac(atts.getValue("mac")); 
	}

	public String getXml() {
		String res = '<' + TAG_NAME + " name=\"" + instanceName + "\" mac=\"" + getMacString() + "\"";
		if(miiID!=null)
		{
			res += " mii_id=\"" + miiID + "\"";
		}
		if(phyID!=null)
		{
			res += " phy_id=\"" + phyID + "\"";
		}
		res +="></" + TAG_NAME + ">\n";
		return res;
	}
	public String getMacString()
	{
		return String.format("%02x:%02x:%02x:%02x:%02x:%02x", mac[0], mac[1],
				mac[2], mac[3], mac[4], mac[5]);
	}

	public Integer getMiiID() {
		return miiID;
	}

	public void setMiiID(Integer miiID) {
		this.miiID = miiID;
	}

	public Integer getPhyID() {
		return phyID;
	}

	public void setPhyID(Integer phyID) {
		this.phyID = phyID;
	}

	public int[] getMac() {
		return mac;
	}

	public void setMac(int[] mac) {
		if(mac.length == 6)
		{
			this.mac = mac;
		}
	}
	public void setMac(String sVal)
	{
		if(sVal != null)
		{
			String[] sMac = sVal.split(":");
			if(sMac.length==6)
			{
				for(int i=0; i<6; i++)
				{
					mac[i] = Integer.parseInt(sMac[i], 16);
				}
			}
		}
	}
}
