/* OpenMark online assessment system
   Copyright (C) 2007 The Open University

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package om.stdquestion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import om.OmDeveloperException;
import om.OmUnexpectedException;
import om.question.InitParams;
import om.stdcomponent.ComponentRegistry;

/** Manages list of available component classes */
public class QComponentManager
{
	/** Map of String (tag name) -> Class (QComponent subclass) */
	private Map<String,Class<? extends QComponent> > mComponents=
			new HashMap<String,Class<? extends QComponent> >();

	/**
	 * Constructs with standard components plus anything from jar files specified
	 * in InitParams.
	 * @param ip Question initialisation parameters
	 * @throws OmDeveloperException If the component registry throws a wobbly
	 */
	QComponentManager(InitParams ip) throws OmDeveloperException
	{
		ComponentRegistry.fill(this);

		// TODO Use InitParams
	}

	/**
	 * Registers a given class that provides QComponents for a tag name.
	 * @param cComponent Java class to create objects from
	 * @throws OmDeveloperException
	 * @throws IllegalArgumentException If the tag name is already in use
	 */
	public void register(Class<?> cComponent) throws OmDeveloperException
	{
		String sTagName;
		try
		{
			Method m=cComponent.getMethod("getTagName",new Class[]{});
			sTagName=(String)m.invoke(null);
		}
		catch(ClassCastException cce)
		{
			throw new OmDeveloperException(cComponent.getName()+
				".getTagName() did not return a String");
		}
		catch(NoSuchMethodException e)
		{
			throw new OmDeveloperException(cComponent.getName()+
				"does not contain public String getTagName() method");
		}
		catch(IllegalAccessException e)
		{
			throw new OmDeveloperException(cComponent.getName()+
				".getTagName() method could not be accessed");
		}
		catch(InvocationTargetException e)
		{
			throw new OmDeveloperException(cComponent.getName()+
				".getTagName() method threw an exception");
		}

		if(mComponents.containsKey(sTagName))
			throw new OmDeveloperException("Attempt to re-register component <"+sTagName+">");

		if(!QComponent.class.isAssignableFrom(cComponent))
			throw new OmDeveloperException("Class "+cComponent+" cannot be " +
				"registered as a component because it isn't a QComponent subclass");

		mComponents.put(sTagName,cComponent.asSubclass(QComponent.class));
	}

	/**
	 * Creates an instance of the given component.
	 * @param sTagName the xml tag.
	 * @return the new QComponent instance
	 * @throws OmDeveloperException If the tag name doesn't exist, or if the class
	 *   exists but is not public.
	 * @throws OmUnexpectedException
	 */
	QComponent create(String sTagName) throws OmDeveloperException,OmUnexpectedException
	{
		Class<?> c=mComponents.get(sTagName);
		if(c==null) throw new OmDeveloperException(
			"Attempt to create component <"+sTagName+"> which has not been registered");
		try
		{
			return (QComponent)c.newInstance();
		}
		catch(InstantiationException e)
		{
			throw new OmDeveloperException(
				"Creating component <"+sTagName+"> ("+c+") failed (InstantiationException). " +
				"Check the class is not abstract, or an interface, etc. and that it has " +
				"a public empty constructor");
		}
		catch(IllegalAccessException e)
		{
			throw new OmDeveloperException(
				"Creating component <"+sTagName+"> ("+c+") was prohibited via Java " +
				"access permissions. Check that the component class is public and " +
				"that it contains a public empty constructor");
		}
	}

}
