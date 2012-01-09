/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2012 Walter Goossens <waltergoossens@home.nl>

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
package sopc2dts.lib.devicetree;

import java.util.Vector;

public class DTBlob {
	public static final int DTB_VERSION = 17;
	public static final int DTB_COMPAT_VERSION = 16;
	public static final long OF_DT_HEADER = 0xD00DFEED;
	public static final int OF_DT_END = 0x09;
	static final int HEADER_SIZE = 40;
	long nextPHandle = 1;
	Vector<String> vStrings = new Vector<String>();
	DTNode rootNode;
	
	public byte[] getBytes()
	{
		byte[] mrm = getMemReserveMap();
		byte[] dt = getDT();
		byte[] strings = getStrings();
		int totalSize = HEADER_SIZE + mrm.length + dt.length + strings.length;
		int pos = 0;
		byte[] dtb = new byte[totalSize];
		//Magic
		pos = putU32(OF_DT_HEADER, dtb, pos);
		//TotalSize
		pos = putU32(totalSize, dtb, pos);
		//off_dt_struct
		pos = putU32(HEADER_SIZE + mrm.length, dtb, pos);
		//off_dt_strings
		pos = putU32(HEADER_SIZE + mrm.length + dt.length, dtb, pos);
		//off_mem_rsvmap
		pos = putU32(HEADER_SIZE, dtb, pos);
		pos = putU32(DTB_VERSION, dtb, pos);
		pos = putU32(DTB_COMPAT_VERSION, dtb, pos);
		//boot_cpuid_phys
		pos = putU32(0xFEEDBEEF, dtb, pos);
		pos = putU32(strings.length, dtb, pos);
		pos = putU32(dt.length, dtb, pos);
		System.arraycopy(mrm, 0, dtb, pos, mrm.length);
		pos += mrm.length;
		System.arraycopy(dt, 0, dtb, pos, dt.length);
		pos += dt.length;
		System.arraycopy(strings, 0, dtb, pos, strings.length);
		pos += strings.length;
		return dtb;
	}
	protected byte[] getDT() {
		setPHandles(rootNode);
		byte[] dt = rootNode.getBytes(this);
		byte[] dt2 = new byte[dt.length + 4];
		System.arraycopy(dt, 0, dt2, 0, dt.length);
		putU32(OF_DT_END, dt2, dt.length);
		return dt2;
	}
	protected byte[] getMemReserveMap() {
		//Not used right now. Just add an empty entry
		byte[] mrm = new byte[16];
		putU64(0L, mrm, 0);	//Addr
		putU64(0L, mrm, 8); //Size
		return mrm;
	}
	protected byte[] getStrings() {
		byte[] strblock;
		int size = 0;
		int pos = 0;
		for(String str : vStrings)
		{
			size += str.length() + 1;
		}
		strblock = new byte[size];
		for(String str : vStrings)
		{
			System.arraycopy(str.getBytes(), 0, strblock, pos, str.length());
			pos += str.length();
			strblock[pos] = 0;
			pos++;
		}
		return strblock;
	}
	public static int putU32(long val, byte[] buff, int off)
	{
		buff[off++] = (byte)((val>>24)&0xFF);
		buff[off++] = (byte)((val>>16)&0xFF);
		buff[off++] = (byte)((val>> 8)&0xFF);
		buff[off++] = (byte)((val    )&0xFF);
		return off;
	}
	public static int putU64(long val, byte[] buff, int off)
	{
		off = putU32(val>>32, buff, off);
		return putU32(val, buff, off);
	}
	public static int putStringAligned(String val, byte[] buff, int off)
	{
		System.arraycopy(val.getBytes(), 0, buff, off, val.length());
		return off + (val.length() + 4) & (~0x03);
	}
	public DTNode getRootNode() {
		return rootNode;
	}
	public void setRootNode(DTNode rootNode) {
		this.rootNode = rootNode;
	}
	public int registerString(String str)
	{
		int pos=0;
		for(String s : vStrings)
		{
			if(s.equals(str))
			{
				return pos;
			} else {
				pos += s.length() + 1;
			}
		}
		vStrings.add(str);
		return pos;
	}
	public long getPHandle(String label) {
		return getPHandle(label,rootNode);
	}
	long getPHandle(String label,DTNode node) {
		long ph = 0;
 		if(label.equals(node.getLabel()))
		{
			DTPropNumber dtpn = (DTPropNumber)node.getPropertyByName("linux,phandle");
			if(dtpn==null)
			{
				dtpn = new DTPropNumber("linux,phandle",nextPHandle);
				ph = nextPHandle;
				nextPHandle++;
				node.addProperty(dtpn);
				return ph;
			} else {
				return dtpn.getValues().firstElement();
			}
		} else {
			for(DTNode child : node.getChildren())
			{
				ph = getPHandle(label, child);
				if(ph!=0) {
					return ph;
				}
			}
		}
		return ph;
	}
	
	void setPHandles(DTNode node) {
		for(DTProperty prop : node.getProperties())
		{
			if(prop instanceof DTPropPHandle)
			{
				DTPropPHandle dtph = (DTPropPHandle)prop;
				dtph.setpHandle(getPHandle(dtph.getValue()));
			}
		}
		for(DTNode child : node.getChildren())
		{
			setPHandles(child);
		}		
	}
}
