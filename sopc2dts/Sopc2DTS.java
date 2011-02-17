
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sopc2dts.Logger;
import sopc2dts.generators.DTSGenerator;
import sopc2dts.generators.KernelHeadersGenerator;
import sopc2dts.generators.SopcCreateHeaderFilesImitator;
import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.SopcInfoSystem;
import sopc2dts.lib.components.SopcInfoComponent;


public class Sopc2DTS {
	protected Vector<CommandLineOption> vOptions = new Vector<CommandLineOption>();
	protected CLParameter showHelp = new CLParameter(""+false);
	protected CLParameter showVersion = new CLParameter("" + false);
	protected CLParameter verbose = new CLParameter("" + false);
	protected CLParameter mimicAlteraTools = new CLParameter("" + false);
	protected CLParameter inputFileName = new CLParameter("");
	protected CLParameter boardFileName = new CLParameter("");
	protected CLParameter outputFileName = new CLParameter("");
	protected CLParameter outputType = new CLParameter("dts");
	protected CLParameter pov = new CLParameter("");
	protected CLParameter bootargs = new CLParameter("");
	protected CLParameter sopcParameters = new CLParameter("none");
	SopcInfoComponent.parameter_action dumpParameters = SopcInfoComponent.parameter_action.NONE;

	protected String programName;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Sopc2DTS s2d = new Sopc2DTS("sopc2dts");
		try {
			if(s2d.parseCmdLine(args))
			{
				s2d.go();
			}
		} catch(Exception e) {
			System.err.println(e);
			s2d.printUsage();
		}
	}
	
	public Sopc2DTS(String pName) {
		programName = pName;
		vOptions.add(new CommandLineOption("board", 	"b", boardFileName, 	true, false,"The board description file", "boardinfo file"));
		vOptions.add(new CommandLineOption("help"		,"h", showHelp,			false,false,"Show this usage info and exit",null));
		vOptions.add(new CommandLineOption("verbose"	,"v", verbose,			false,false,"Show Lots of debugging info", null));
		vOptions.add(new CommandLineOption("version"	,null,showVersion,		false,false,"Show version information and exit", null));
		vOptions.add(new CommandLineOption("mimic-sopc-create-header-files"	,"m", mimicAlteraTools,		false,false,"Try to (mis)behave like sopc-create-header-files does", null));
		vOptions.add(new CommandLineOption("input", 	"i", inputFileName, 	true, true, "The sopcinfo file (if not supplied the current dir is scanned for one)", "sopcinfo file"));
		vOptions.add(new CommandLineOption("output",	"o", outputFileName,	true, false,"The output filename","filename"));
		vOptions.add(new CommandLineOption("pov", 		"p", pov,		 		true, false,"The point of view to generate from. Defaults to the first cpu found", "component name"));
		vOptions.add(new CommandLineOption("type", 		"t", outputType, 		true, false,"The type of output to generate", "{dts,uboot,kernel,kernel-full}"));
		vOptions.add(new CommandLineOption("bootargs", 	null,bootargs,	 		true, false,"Default kernel arguments for the \"chosen\" section of the DTS", "kernel-args"));
		vOptions.add(new CommandLineOption("sopc-parameters", 	null,sopcParameters, true, false,"What sopc-parameters to include in DTS: none, cmacro or all.", "choice"));
	}
	protected void go()
	{
		BoardInfo bInfo = null;
		File f;
		if(inputFileName.value.length()==0)
		{
			System.out.println("No input file specified!");
			printUsage();
		}
		if(boardFileName.value.length()>0)
		{
			f = new File(boardFileName.value);
			if(f.exists())
			{
				try {
					bInfo = new BoardInfo(new InputSource(new BufferedReader(new FileReader(f))));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			f = null;
		}
		if(bInfo==null)
		{
			bInfo = new BoardInfo();
		}
		if(pov.value.length()>0)
		{
			bInfo.setPov(pov.value);
		}
		f = new File(inputFileName.value);
		if(f.exists())
		{
			try {
				SopcInfoSystem sys = new SopcInfoSystem(new InputSource(new BufferedReader(new FileReader(f))));
				if(bInfo.getPov().length()==0)
				{
					for(int i=0; (i<sys.getSystemComponents().size()) && (bInfo.getPov().length()==0); i++)
					{
						if(sys.getSystemComponents().get(i).getScd().getGroup().equalsIgnoreCase("cpu"))
						{
							bInfo.setPov(sys.getSystemComponents().get(i).getInstanceName());
						}
					}
				}
				if(Boolean.parseBoolean(mimicAlteraTools.value)) {
					SopcCreateHeaderFilesImitator fake = new SopcCreateHeaderFilesImitator(sys);
					Vector<SopcInfoComponent> vMasters = sys.getMasterComponents();
					for(SopcInfoComponent master : vMasters)
					{
						BufferedWriter out = new BufferedWriter(new FileWriter(master.getInstanceName()+".h"));
						bInfo.setPov(master.getInstanceName());
						out.write(fake.getOutput(bInfo));
						out.close();
					}
				} else {
					String generatedData = null;
					if(bootargs.value.length()>0)
					{
						bInfo.setBootArgs(bootargs.value);
					}
					if(outputType.value.equalsIgnoreCase("dts"))
					{
						DTSGenerator dGen = new DTSGenerator(sys);
						generatedData = dGen.getOutput(bInfo, dumpParameters);
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
						generatedData = fake.getOutput(bInfo);
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
	protected String[] intelliSplit(String inp, int splitChar)
	{
		Vector<String> vRes = new Vector<String>();
		boolean escape=false;
		boolean quoted=false;
		String tmp = "";
		int i=0;
		while(i<inp.length())
		{
			if(inp.charAt(i) == '\"') {
				if(!escape) quoted = !quoted;
				tmp+=inp.charAt(i);
				escape=false;
			} else if(inp.charAt(i) == '\\') {
				escape=true;
				tmp += inp.charAt(i);
			} else if((inp.charAt(i) == splitChar)&& !escape && !quoted) {
				if(tmp.length()>0)
				{
					vRes.add(tmp);
					tmp = "";
				}
			} else {
				tmp += inp.charAt(i);
				escape=false;
			}		
			i++;
		}
		if(tmp.length()>0)
		{
			vRes.add(tmp);
		}
		return vRes.toArray(new String[0]);
	}
	protected boolean parseCmdLine(String[] args) throws Exception
	{
		int argPos = 0;
		int oldPos = 0;
		String cmdLine = "";
		while(argPos<args.length)
		{
			cmdLine += args[argPos++] + ' '; 
		}
		argPos=0;
		args=intelliSplit(cmdLine, ' ');
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
				return false;
			}
		}
		if(Boolean.parseBoolean(showHelp.value))
		{
			printUsage();
			return false;
		}
		if(Boolean.parseBoolean(showVersion.value))
		{
			printVersion();
			return false;
		}
		Logger.setVerbose(Boolean.parseBoolean(verbose.value));
		if(sopcParameters.value.equalsIgnoreCase("none"))
		{
			dumpParameters = SopcInfoComponent.parameter_action.NONE;
		} else if(sopcParameters.value.equalsIgnoreCase("cmacro"))
		{
			dumpParameters = SopcInfoComponent.parameter_action.CMACRCO;
		} else if(sopcParameters.value.equalsIgnoreCase("all"))
		{
			dumpParameters = SopcInfoComponent.parameter_action.ALL;
		}
		return true;
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
		int scanCmdLine(String[] opts, int index) throws Exception
		{
			if(opts[index].charAt(0)!='-')
			{
				//Assume it's the sopcinfo file
				inputFileName.value = opts[index];
				index++;
			} else {
				String[] tmpOpts = intelliSplit(opts[index],'=');
				if((tmpOpts[0].equals("--" + option)) || 
						((shortOption!=null)&&(tmpOpts[0].equals('-' + shortOption))))
				{
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
									throw new Exception("Missing argument");
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
					if(parameter==verbose)
					{
						Logger.setVerbose(Boolean.parseBoolean(parameter.value));
					}
					Logger.log("Scanned option " + option + '(' + shortOption + ") with");
					if(hasValue)
					{
						Logger.logln(" value " + parameter.value);
					} else {
						Logger.logln("out value.");
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
