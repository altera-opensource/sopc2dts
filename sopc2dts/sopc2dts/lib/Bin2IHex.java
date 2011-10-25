package sopc2dts.lib;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;

public class Bin2IHex {
	public enum ByteOrder { LE, BE16, BE32, BE64 };
	public enum HexTypes { I8Hex, I16Hex, I32Hex };
	public enum AddressingMode { AddrMode8, AddrMode16, AddrMode32, AddrMode64 };
	public static String toHex(byte[] in)
	{
		return toHex(in, HexTypes.I8Hex, ByteOrder.LE, 0, AddressingMode.AddrMode8);
	}
	public static String toHex(byte[] in, HexTypes type, ByteOrder order)
	{
		return toHex(in, type, order, 0, AddressingMode.AddrMode8);
	}
	public static String toHex(byte[] in, HexTypes type, ByteOrder order, AddressingMode aMode)
	{
		return toHex(in, type, order, 0, aMode);
	}
	public static String toHex(byte[] in, HexTypes type, ByteOrder order, int address, AddressingMode aMode)
	{
		String res = "";
		int pos = 0;
		//EOF record
		switch(order)
		{
		case LE: {
			//perfect
		} break;
		case BE16: {
			if((in.length%2) !=0 )
			{
				Logger.logln("Chosen BE16 ordering but length is not 16bit aligned. Padding with zeros", LogLevel.WARNING);
				byte tmp[] = new byte[in.length+1];
				System.arraycopy(in, 0, tmp, 0, in.length);
				tmp[in.length] = 0;
				in = tmp;
			}
		} break;
		case BE32: {
			if((in.length%4) !=0 )
			{
				Logger.logln("Chosen BE32 ordering but length is not 32bit aligned. Padding with zeros", LogLevel.WARNING);
				byte tmp[] = new byte[(in.length+3)&0xFFFFFFFC];
				System.arraycopy(in, 0, tmp, 0, in.length);
				for(int i=in.length; i<tmp.length; i++)
				{
					tmp[i] = 0;
				}
				in = tmp;
			}
		} break;
		case BE64: {
			if((in.length%8) !=0 )
			{
				Logger.logln("Chosen BE64 ordering but length is not 64bit aligned. Padding with zeros", LogLevel.WARNING);
				byte tmp[] = new byte[(in.length+7)&0xFFFFFFF8];
				System.arraycopy(in, 0, tmp, 0, in.length);
				for(int i=in.length; i<tmp.length; i++)
				{
					tmp[i] = 0;
				}
				in = tmp;
			}
		} break;
		}
		while(pos<in.length)
		{
			int lineAddress = address;
			int lineSize = 0x10;
			if((pos + lineSize) > in.length)
			{
				lineSize = in.length - pos;
			}
			switch(aMode)
			{
			case AddrMode8:  lineAddress += pos; break;
			case AddrMode16: lineAddress += (pos>>1); break;
			case AddrMode32: lineAddress += (pos>>2); break;
			case AddrMode64: lineAddress += (pos>>3); break;
			}
			res += toHexLine(in, pos, lineSize, lineAddress, order);
			pos += lineSize;
		}
		res += ":00000001FF\n";
		return res;
	}
	public static String toHexLine(byte[] in, int pos, int size, int address, ByteOrder order)
	{
		byte cksum = 0;
		cksum += size;
		cksum += (address & 0xFF);
		cksum += ((address>>8) & 0xFF);
		String res = String.format(":%02X%04X00", size, address & 0xFFFF);
		while(size>0)
		{
			switch(order)
			{
			case LE: {
				cksum += in[pos];
				res += String.format("%02X",in[pos]);
				pos++;
				size--;
			} break;
			case BE16: {
				cksum += in[pos];
				cksum += in[pos+1];
				res += String.format("%02X%02X",in[pos+1],in[pos]);
				pos+=2;
				size -=2;
			} break;
			case BE32: {
				cksum += in[pos];
				cksum += in[pos+1];
				cksum += in[pos+2];
				cksum += in[pos+3];
				res += String.format("%02X%02X%02X%02X",in[pos+3],in[pos+2],in[pos+1],in[pos]);
				pos+=4;
				size -=4;
			} break;
			case BE64: {
				cksum += in[pos];
				cksum += in[pos+1];
				cksum += in[pos+2];
				cksum += in[pos+3];
				cksum += in[pos+4];
				cksum += in[pos+5];
				cksum += in[pos+6];
				cksum += in[pos+7];
				res += String.format("%02X%02X%02X%02X",in[pos+7],in[pos+6],in[pos+5],in[pos+4]);
				res += String.format("%02X%02X%02X%02X",in[pos+3],in[pos+2],in[pos+1],in[pos]);
				pos+=8;
				size -=8;
			} break;
			}
		}
		res += String.format("%02X\n", (-cksum) & 0xFF);
		return res;
	}
}
