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

	public void setCompatible(Vector<String> vCompatible) {
		this.vCompatible = vCompatible;
	}

	public Vector<String> getCompatible() {
		return vCompatible;
	}
}
