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
package om.tnavigator;

import java.lang.reflect.Method;
import java.util.*;

/** 
 * Allows classes to be notified when the webapp is shut down so that they can
 * null any relevant static member fields that might be keeping things from 
 * being properly GCed.
 */
public class ShutdownManager
{
	/** Set of classes that have requested notification */
	private static Set<Class<?> > sShutdownListeners=new HashSet<Class<?> >();
	
	/**
	 * Call from within classes that need notification 
	 * @param c class to be notified when the servlet is shut down.
	 */
	public static void requestShutdownNotification(Class<?> c)
	{
		sShutdownListeners.add(c);
	}
	
	/** Call when webapp is shut down */
	public static void shutdown()
	{
		// Call shutdown on each class
		for(Class<?> c : sShutdownListeners)
		{
			try
			{
				Method m=c.getMethod("shutdown");
				m.invoke(null);
			}
			catch(Exception e)
			{
				// Ignore errors at this stage
				e.printStackTrace();
			}
		}
		
		// Remove our own list so that the classes can be collected
		sShutdownListeners=null;
	}
}
