package sopc2dts.lib.components;
import java.util.Vector;

public class SopcComponentDescription {
	private String className;
	private String group;
	private String vendor;
	private String device;
	private Vector<String> vCompatible = new Vector<String>();
	Vector<SICAutoParam> vAutoParams = new Vector<SICAutoParam>();
	Vector<SICRequiredParam> vRequiredParams = new Vector<SICRequiredParam>();
	Vector<String> vCompatibleVersions = new Vector<String>();
	
	protected class SICAutoParam {
		String dtsName;
		String sopcInfoName;
		
		public SICAutoParam(String dts, String sopcInfo)
		{
			dtsName = dts;
			sopcInfoName = sopcInfo;
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
	public void addAutoParam(String dtsName, String sopcName) {
		vAutoParams.add(new SICAutoParam(dtsName, sopcName));
	}

	public void addRequiredParam(String dtsName, String sopcName) {
		vRequiredParams.add(new SICRequiredParam(dtsName, sopcName));
	}
	public Vector<SICRequiredParam> getRequiredParams()
	{
		return vRequiredParams;
	}
	public boolean isRequiredParamsOk(SopcInfoComponent comp)
	{
		for(SICRequiredParam rp : vRequiredParams)
		{
			if(comp.getParamValue(rp.name)==null) return false;
			if(!comp.getParamValue(rp.name).getValue().equalsIgnoreCase(rp.value)) return false;
		}
		return true;
	}
	public void setGroup(String group) {
		this.group = group;
	}

	public String getGroup() {
		return group;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
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
		String res = "\"" + vendor + "," + device;
		if(version!=null)
		{
			res += "-" + version + "\"";
			String bwCompatVersion = getCompatibleVersion(version);
			if(bwCompatVersion!=null)
			{
				res += ",\"" + vendor + "," + device + "-" + bwCompatVersion + "\"";
			}
		} else {
			res += "\"";
		}
		for(String comp : vCompatible)
		{
			res += ",\"" + comp + "\"";
		}
		return res;
	}
}
