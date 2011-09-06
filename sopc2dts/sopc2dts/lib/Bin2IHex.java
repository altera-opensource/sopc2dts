package sopc2dts.lib;

public class Bin2IHex {
	public enum HexTypes { I8Hex, I16Hex, I32Hex };
	
	public static String toHex(byte[] in)
	{
		return toHex(in, HexTypes.I8Hex, 0);
	}
	public static String toHex(byte[] in, HexTypes type)
	{
		return toHex(in, type, 0);
	}
	public static String toHex(byte[] in, HexTypes type, int address)
	{
		String res = "";
		int pos = 0;
		//EOF record
		while(pos<in.length)
		{
			int lineSize = 0x10;
			if((pos + lineSize) > in.length)
			{
				lineSize = in.length - pos;
			}
			res += toHexLine(in, pos, lineSize, address + pos);
			pos += lineSize;
		}
		res += ":00000001FF\n";
		return res;
	}
	public static String toHexLine(byte[] in, int pos, int size, int address)
	{
		byte cksum = 0;
		cksum += size;
		cksum += (address & 0xFFFF);
		String res = String.format(":%02X%04X00", size, address & 0xFFFF);
		while(size>0)
		{
			cksum += in[pos];
			res += String.format("%02X",in[pos]);
			pos++;
			size--;
		}
		res += String.format("%02X\n", (~cksum) & 0xFF);
		return res;
	}
}
