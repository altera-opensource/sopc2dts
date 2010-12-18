
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.xml.sax.InputSource;

import sopc2dts.Logger;
import sopc2dts.generators.DTSGenerator;
import sopc2dts.generators.KernelHeadersGenerator;
import sopc2dts.generators.SopcCreateHeaderFilesImitator;
import sopc2dts.lib.SopcInfoSystem;
import sopc2dts.lib.components.SopcInfoComponent;


public class Sopc2DTS {
	protected Vector<CommandLineOption> vOptions = new Vector<CommandLineOption>();
	protected CLParameter showHelp = new CLParameter(""+false);
	protected CLParameter showVersion = new CLParameter("" + false);
	protected CLParameter verbose = new CLParameter("" + false);
	protected CLParameter mimicAlteraTools = new CLParameter("" + false);
	protected CLParameter inputFileName = new CLParameter("");
	protected CLParameter outputFileName = new CLParameter("");
	protected CLParameter outputType = new CLParameter("dts");
	protected CLParameter pov = new CLParameter("");
	
	protected String programName;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Sopc2DTS s2d = new Sopc2DTS("sopc2dts");
		s2d.parseCmdLine(args);
		s2d.go();
	}
	
	public Sopc2DTS(String pName) {
		programName = pName;
		vOptions.add(new CommandLineOption("help"		,"h", showHelp,			false,false,"Show this usage info and exit",null));
		vOptions.add(new CommandLineOption("verbose"	,"v", verbose,			false,false,"Show Lots of debugging info", null));
		vOptions.add(new CommandLineOption("version"	,null,showVersion,		false,false,"Show version information and exit", null));
		vOptions.add(new CommandLineOption("mimic-sopc-create-header-files"	,"m", mimicAlteraTools,		false,false,"Try to (mis)behave like sopc-create-header-files does", null));
		vOptions.add(new CommandLineOption("input", 	"i", inputFileName, 	true, true, "The sopcinfo file (if not supplied the current dir is scanned for one)", "sopcinfo file"));
		vOptions.add(new CommandLineOption("output",	"o", outputFileName,	true, false,"The output filename","filename"));
		vOptions.add(new CommandLineOption("pov", 		"p", pov,		 		true, false,"The point of view to generate from. Defaults to the first cpu found", "component name"));
		vOptions.add(new CommandLineOption("type", 		"t", outputType, 		true, false,"The type of output to generate", "{dts,uboot,kernel,kernel-full}"));
	}
	protected void go()
	{
		if(inputFileName.value.length()==0)
		{
			System.out.println("No input file specified!");
			printUsage();
		}
		File f = new File(inputFileName.value);
		if(f.exists())
		{
			try {
				SopcInfoSystem sys = new SopcInfoSystem(new InputSource(new BufferedReader(new FileReader(f))));
				if(pov.value.length()==0)
				{
					for(int i=0; (i<sys.getSystemComponents().size()) && (pov.value.length()==0); i++)
					{
						if(sys.getSystemComponents().get(i).getScd().getGroup().equalsIgnoreCase("cpu"))
						{
							pov.value = sys.getSystemComponents().get(i).getInstanceName();
						}
					}
				}
				if(Boolean.parseBoolean(mimicAlteraTools.value)) {
					SopcCreateHeaderFilesImitator fake = new SopcCreateHeaderFilesImitator(sys);
					Vector<SopcInfoComponent> vMasters = sys.getMasterComponents();
					for(SopcInfoComponent master : vMasters)
					{
						BufferedWriter out = new BufferedWriter(new FileWriter(master.getInstanceName()+".h"));
						out.write(fake.getOutput(master.getInstanceName()));
						out.close();
					}
				} else {
					String generatedData = null;
					if(outputType.value.equalsIgnoreCase("dts"))
					{
						DTSGenerator dGen = new DTSGenerator(sys);
						generatedData = dGen.getOutput(pov.value);
					} else if(outputType.value.equalsIgnoreCase("uboot"))
					{
						generatedData = "Whoops, I guess I was bluffing. uboot support is not yet done";
					} else if(outputType.value.equalsIgnoreCase("kernel"))
					{
						KernelHeadersGenerator kGen = new KernelHeadersGenerator(sys);
						generatedData = kGen.getOutput(null);
					} else if(outputType.value.equalsIgnoreCase("kernel-full"))
					{
						SopcCreateHeaderFilesImitator fake = new SopcCreateHeaderFilesImitator(sys);
						generatedData = fake.getOutput(pov.value);
					} else {
						System.out.println("Unsupported output type: " + outputType.value);
					}
					if(generatedData!=null)
					{
						if(outputFileName.value.length()==0)
						{
							System.out.println(generatedData);
						} else {
							BufferedWriter out = new BufferedWriter(new FileWriter(outputFileName.value));
							out.write(generatedData);
							out.close();
						}
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("Inputfile " + inputFileName.value + " not found");
		}
	}
	
	protected void parseCmdLine(String[] args)
	{
		int argPos = 0;
		int oldPos = 0;
		while(argPos<args.length)
		{
			oldPos = argPos;
			for(int iHandler=0; (iHandler<vOptions.size()) && (argPos<args.length); iHandler++)
			{
				argPos = vOptions.get(iHandler).scanCmdLine(args, argPos);
			}
			if(oldPos == argPos)
			{
				//No parser found!!!
				System.out.println("Option " + args[argPos] +" not understood.");
				printUsage();
				System.exit(-1);
			}
		}
		if(Boolean.parseBoolean(showHelp.value))
		{
			printUsage();
			System.exit(0);
		}
		if(Boolean.parseBoolean(showVersion.value))
		{
			printVersion();
			System.exit(0);
		}
		Logger.setVerbose(Boolean.parseBoolean(verbose.value));
	}

	protected void printUsage()
	{
		printVersion();
		System.out.println("Usage: " + programName + " <arguments>");
		System.out.println("Required Arguments:");
		String longOpts = "";
		String shortOpts = "";
		for(int i=0; i<vOptions.size(); i++)
		{
			if(vOptions.get(i).isRequired())
			{
				longOpts += vOptions.get(i).getDesc();
				shortOpts += vOptions.get(i).getShortDesc();
			}
		}
		System.out.println(longOpts);
		System.out.println(shortOpts);
		System.out.println("Optional Arguments:");
		longOpts = "";
		shortOpts = "";
		for(int i=0; i<vOptions.size(); i++)
		{
			if(!vOptions.get(i).isRequired())
			{
				longOpts += vOptions.get(i).getDesc();
				shortOpts += vOptions.get(i).getShortDesc();
			}
		}
		System.out.println(longOpts);
		System.out.println(shortOpts);
	}
	
	public void printVersion()
	{
		System.out.println(programName + " - 0.1");
	}
	
	protected class CLParameter
	{
		public String value;
		public CLParameter(String val)
		{
			value = val;
		}
	}
	protected class CommandLineOption
	{
		String option;
		String shortOption;
		String helpDesc;
		CLParameter parameter;
		String parameterDesc;
		boolean isRequired;
		boolean hasValue;
		boolean scanned = false;
		public CommandLineOption(String opt, String shortOpt, CLParameter param, boolean hasVal, boolean req, String help, String paramDesc)
		{
			option = opt;
			shortOption = shortOpt;
			helpDesc = help;
			parameter = param;
			parameterDesc = paramDesc;
			isRequired = req;
			hasValue = hasVal;
			if(isRequired)
			{
				helpDesc += (" (Required)");
			} else {
				helpDesc += (" (Optional)");
			}
		}
		int scanCmdLine(String[] opts, int index) throws RuntimeException
		{
			if(opts[index].charAt(0)!='-')
			{
				//Assume it's the sopcinfo file
				inputFileName.value = opts[index];
				scanned = true;
				index++;
			} else {
				String[] tmpOpts = opts[index].split("=");
				if((tmpOpts[0].equals("--" + option)) || 
						((shortOption!=null)&&(tmpOpts[0].equals("-" + shortOption))))
				{
					scanned = true;
					if(parameter!=null)
					{
						if(hasValue)
						{
							String val;
							if(tmpOpts.length==1)
							{
								if(index<(opts.length-1))
								{
									index++;
								} else {
									throw new RuntimeException("Missing argument");
								}
								val = opts[index];
							} else {
								val = tmpOpts[1];
							}
							parameter.value = val;
						} else {
							parameter.value = "" + true;
						}
					}
					if(Logger.isVerbose())
					{
						System.out.print("Scanned option " + option + "(" + shortOption + ") with");
						if(hasValue)
						{
							System.out.println(" value " + parameter.value);
						} else {
							System.out.println("out value.");
						}
					}
					index++;
				}
			}
			return index;
		}
		boolean isRequired()
		{
			return isRequired;
		}
		boolean isScanned()
		{
			return scanned;
		}
		String getDesc()
		{
			return "  --" + option + ((!hasValue ?"\t\t" : " <" + parameterDesc + ">\t")) + helpDesc + "\n";
		}
		String getShortDesc()
		{
			if(shortOption!=null)
			{
				return "  -" + shortOption + ((!hasValue ? "\t\t" : " <" + parameterDesc + ">\t")) + "Short for --" + option + "\n";
			} else {
				return "";
			}
		}
	}	
}
