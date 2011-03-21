package sopc2dts.lib.components.base;

import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.SopcComponentDescription;

public class SCDSelfDescribing extends SopcComponentDescription {
	
	public SCDSelfDescribing(BasicComponent comp)
	{
		String dev = comp.getParamValByName("embeddedsw.dts.name");
		this.setClassName(comp.getScd().getClassName());
		if(dev == null) dev = getClassName();
		this.setDevice(dev);
		this.setGroup(comp.getParamValByName("embeddedsw.dts.group"));
		this.setVendor(comp.getParamValByName("embeddedsw.dts.vendor"));
	}
	
	/*
	 * Scans a BasicComponent for parameters given through the _hw.tcl file that
	 * describe the usage of that component in the device-tree.
	 * Those parameters are named embeddedsw.dts.* and some are mandatory and 
	 * some are optional. When there are enough parameters in the component, 
	 * those parameters will be used instead of the ones defined in the 
	 * sopc_component_*.xml files.
	 * In the long run this will remove the need for those xml-files.
	 */
	public static boolean isSelfDescribing(BasicComponent comp) {
		String pVal = comp.getParamValByName("embeddedsw.dts.vendor");
		if((pVal == null) || (pVal.length() == 0)) {
			//Vendor info is mandatory
			return false;
		}
		pVal = comp.getParamValByName("embeddedsw.dts.group");
		if((pVal == null) || (pVal.length() == 0)) {
			//group/type info is mandatory
			return false;
		}
		return true;
	}
}
