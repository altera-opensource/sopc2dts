package sopc2dts;

public class Logger {
	static boolean bVerbose = false;
	
	public static void setVerbose(boolean verbose)
	{
		bVerbose = verbose;
	}
	public static boolean isVerbose()
	{
		return bVerbose;
	}
	public static void logln(String log)
	{
		if(bVerbose)
		{
			System.out.println(log);
		}
	}
}
