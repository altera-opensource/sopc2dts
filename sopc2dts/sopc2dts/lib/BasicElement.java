package sopc2dts.lib;

import java.util.Vector;

public class BasicElement {
	protected Vector<Parameter> vParameters = new Vector<Parameter>();
	
	public boolean addParam(Parameter bp)
	{
		return vParameters.add(bp);
	}
	public boolean removeParam(Parameter bp)
	{
		return vParameters.remove(bp);
	}
	public Parameter getParamByName(String name)
	{
		for(Parameter p : vParameters)
		{
			if(p.getName().equalsIgnoreCase(name))
			{
				return p;
			}
		}
		return null;
	}
	public Vector<Parameter> getParams()
	{
		return vParameters;
	}
	public String getParamValByName(String name)
	{
		Parameter p = getParamByName(name);
		if(p!=null)
		{
			return p.getValue();
		} else {
			return null;
		}
	}
}
