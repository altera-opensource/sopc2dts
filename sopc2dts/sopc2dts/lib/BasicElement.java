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

/** @brief Basic building block for an SOPCInfo hierarchy
 * 
 * This is the base class for all elements in an sopcinfo file we're going to
 * use. This is basically \"an object with optional paramters\".
 * 
 * @see sopc2dts.lib.Parameter
 * @see sopc2dts.lib.components.BasicComponent
 * @see sopc2dts.lib.Connection
 * @see sopc2dts.lib.components.Interface
 * @author Walter Goossens
 * 
 */
public class BasicElement {
	/** @brief The list of Parameters for this object
	 * 
	 * This is a list of Parameter objects belonging to this Object.
	 */
	protected Vector<Parameter> vParameters;

	/** @brief Basic constructor
	 * 
	 * This contructor creates a new empty BasicElement object.
	 */
	protected BasicElement()
	{
		vParameters = new Vector<Parameter>();
	}
	
	/** @brief Copy constructor
	 * 
	 * This contructor creates a new BasicElement object with the same parameters
	 * as be.
	 * 
	 * @param be The BasicElement to clone
	 */
	protected BasicElement(BasicElement be)
	{
		//Shallow copy
		this.vParameters = be.vParameters;
	}
	
	/** @brief Adds a parameter
	 * 
	 * This function adds param to vParameters.
	 * 
	 * @param param The Parameter to add. 
	 * @return true 
	 */
	public boolean addParam(Parameter param)
	{
		return vParameters.add(param);
	}
	/** @brief Remove parameter
	 * 
	 * This  function removes param from vParameters.
	 * 
	 * @param param The Parameter to remove
	 * @return true if the parameter was in vParameters
	 */
	public boolean removeParam(Parameter param)
	{
		return vParameters.remove(param);
	}
	/** @brief Get a Parameter by it's name
	 * 
	 * This function returns the first parameter in vParameters that is named
	 * name. If you're only interested in the value of the parameter you might 
	 * want to use getParamValByName.
	 * 
	 * @see getParamValByName
	 * @param name
	 * @return The Parameter if found, null otherwise
	 */
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
	/** @brief Get the Parameter list.
	 * 
	 * This function directly returns vParameters. In general you should avoid 
	 * using this function and use addParam, removeParam, getParamByName etc.
	 * There are however some cases (iterating over the parameter list for 
	 * instance) where it is justifiable to use the list.
	 * 
	 * @return vParameters
	 */
	public Vector<Parameter> getParams()
	{
		return vParameters;
	}
	/** @brief Get named Parameter value
	 * 
	 * This function uses getParamByName to find a Parameter named name. When 
	 * found it's value is returned.
	 * 
	 * @param name The name of the parameter
	 * @return The parameter value or null if the parameter was not found.
	 */
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
