/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2011 Walter Goossens <waltergoossens@home.nl>

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package sopc2dts.lib;

import java.util.Vector;

public class BasicElement {
	protected Vector<Parameter> vParameters;

	protected BasicElement()
	{
		vParameters = new Vector<Parameter>();
	}
	protected BasicElement(BasicElement be)
	{
		//Shallow copy
		this.vParameters = be.vParameters;
	}
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
