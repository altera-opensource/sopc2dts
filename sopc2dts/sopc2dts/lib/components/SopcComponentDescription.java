package sopc2dts.lib.components;
import java.util.Vector;


public class SopcComponentDescription {
	private String className;
	private String group;
	private String vendor;
	private String device;
	private Vector<String> vCompatible = new Vector<String>();
	Vector<SICAutoParam> vAutoParams = new Vector<SICAutoParam>();

	protected class SICAutoParam {
		String dtsName;
		String sopcInfoName;
		
		public SICAutoParam(String dts, String sopcInfo)
		{
			dtsName = dts;
			sopcInfoName = sopcInfo;
		}
	}

	public void addAutoParam(String dtsName, String sopcName) {
		vAutoParams.add(new SICAutoParam(dtsName, sopcName));
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
