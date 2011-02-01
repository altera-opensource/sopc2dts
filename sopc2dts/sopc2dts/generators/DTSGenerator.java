package sopc2dts.generators;

import java.util.Vector;

import sopc2dts.lib.SopcInfoConnection;
import sopc2dts.lib.SopcInfoSystem;
import sopc2dts.lib.components.SopcInfoComponent;
import sopc2dts.lib.components.SopcInfoInterface;
import sopc2dts.lib.components.SopcInfoMemoryBlock;

public class DTSGenerator extends AbstractSopcGenerator {
	Vector<SopcInfoComponent> vHandled = new Vector<SopcInfoComponent>();
	public DTSGenerator(SopcInfoSystem s) {
		super(s);
	}

	@Override
	public String getExtension() {
		return "dts";
	}

	@Override
	public String getOutput(String pov) {
		return getOutput(pov, SopcInfoComponent.parameter_action.NONE, null);
	}
	public String getOutput(String pov, SopcInfoComponent.parameter_action paramAction, String bootArgs) {
		int numCPUs = 0;
		int indentLevel = 0;
		vHandled.clear();
		String res = copyRightNotice + "/dts-v1/;\n"
			+ "/ {\n"
			+ indent(++indentLevel) + "model = \"altera," + sys.getSystemName() + "\";\n"
			+ indent(indentLevel) + "compatible = \"altera," + sys.getSystemName() + "\";\n"
			+ indent(indentLevel) + "#address-cells = <1>;\n"
			+ indent(indentLevel) + "#size-cells = <1>;\n";
		for(SopcInfoComponent comp : sys.getSystemComponents())
		{
			if(comp.getScd().getGroup().equalsIgnoreCase("cpu"))
			{
				if(numCPUs==0)
				{
					res += indent(indentLevel++) + "cpus {\n"
						+ indent(indentLevel) + "#address-cells = <1>;\n"
						+ indent(indentLevel) + "#size-cells = <0>;\n";
					if(pov==null) pov = comp.getInstanceName();
				}
				comp.setAddr(numCPUs);
				res += comp.toDts(indentLevel, paramAction);
				vHandled.add(comp);
				numCPUs++;
			}
		}
		if(numCPUs>0) {
			res += indent(--indentLevel) + "};\n";
		}
		SopcInfoComponent povComp = getComponentByName(pov);
		if(povComp!=null)
		{
			res += getDTSMemoryFrom(povComp, indentLevel);
			res += indent(indentLevel++) + "sopc@0 {\n" +
					indent(indentLevel) + "#address-cells = <1>;\n" +
					indent(indentLevel) + "#size-cells = <1>;\n" +
					indent(indentLevel) + "device_type = \"soc\";\n" +
					indent(indentLevel) + "compatible = \"altr,avalon\",\"simple-bus\";\n" +
					indent(indentLevel) + "ranges ;\n" +
					indent(indentLevel) + "bus-frequency = < " + povComp.getClockRate() + " >;\n";
			res += getDTSBusFrom(povComp, paramAction, indentLevel);
			res += indent(--indentLevel) + "}; //sopc\n";
		}
		if((bootArgs==null)||(bootArgs.length()==0))
		{
			bootArgs="debug console=ttyAL0,115200";
		} else {
			bootArgs = bootArgs.replaceAll("\"", "");
		}
		res += indent(indentLevel++) + "chosen {\n" +
				indent(indentLevel) + "bootargs = \"" + bootArgs + "\";\n" +
				indent(--indentLevel) + "};\n";
		res += indent(--indentLevel) + "};\n";
		return res;
	}
	String getDTSMemoryFrom(SopcInfoComponent master, int indentLevel)
	{
		String res = "";
		Vector<String> vMemoryMapped = new Vector<String>();
		if(master!=null)
		{
			for(SopcInfoInterface intf : master.getInterfaces())
			{
				if(intf.isMemoryMaster())
				{
					for(SopcInfoMemoryBlock mem : intf.getMemoryMap())
					{
						if(!vMemoryMapped.contains(mem.getModule()))
						{
							SopcInfoComponent comp = getComponentByName(mem.getModule());
							if(comp!=null)
							{
								if(comp.getScd().getGroup().equalsIgnoreCase("memory"))
								{
									if(res.length()==0)
									{
										res = indent(indentLevel++) + "memory@0 {\n" +
												indent(indentLevel) + "device_type = \"memory\";\n" +
												indent(indentLevel) + "reg = <" + 
													String.format("0x%08X 0x%08X", mem.getBase(), mem.getSize());
									} else {
										res += "\n" + indent(indentLevel) + 
											String.format("\t0x%08X 0x%08X", mem.getBase(), mem.getSize());
									}
									vMemoryMapped.add(mem.getModule());
									vHandled.add(comp);
								}
							}		
						}
					}
				}
			}
			if(res.length()>0) {
				res += ">;\n" + indent(--indentLevel) + "};\n";
			}
		}
		return res;
	}

	String getDTSBusFrom(SopcInfoComponent master, 
				SopcInfoComponent.parameter_action paramAction, int indentLevel)
	{
		String res = "";
		if(master!=null)
		{
			for(SopcInfoInterface intf : master.getInterfaces())
			{
				if(intf.isMemoryMaster())
				{
					res += indent(indentLevel) + "//Port " + intf.getName() + " of " + master.getInstanceName() + "\n";
					for(SopcInfoConnection conn : intf.getConnections())
					{
						SopcInfoComponent slave = conn.getSlaveModule();						
						if(slave!=null)
						{
							if(!vHandled.contains(slave))
							{
								res += slave.toDts(indentLevel,paramAction, conn,false);
								vHandled.add(slave);
								if(slave.getScd().getGroup().equalsIgnoreCase("bridge"))
								{
									res += "\n" + getDTSBusFrom(slave, paramAction, ++indentLevel);
									indentLevel--;
								}
								res += indent(indentLevel) + "}; //end "+slave.getScd().getGroup()+" (" + slave.getInstanceName() + ")\n\n";
							}
						}
					}
				}
			}
		}
		return res;
	}

	public SopcInfoComponent getComponentByName(String name)
	{
		for(SopcInfoComponent c : sys.getSystemComponents())
		{
			if(c.getInstanceName().equalsIgnoreCase(name))
			{
				return c;
			}
		}
		return null;
	}
}
