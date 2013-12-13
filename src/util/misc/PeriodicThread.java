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
package util.misc;

/**
 * Base class for a thread that does something at regular intervals.
 */
public abstract class PeriodicThread extends Thread
{
	/** Delay between runs */
	private int iDelay;

	/** Control flags */
	private boolean bQuit,bHasQuit;

	/**
	 * Starts thread.
	 * @param iDelay Delay between runs
	 */
	public PeriodicThread(int iDelay)
	{
		this.iDelay=iDelay;
		start();
	}

	/**
	 * Method that gets called each time around
	 * the loop
	 */
	protected abstract void tick();

	/** Run method can't be overridden */
	@Override
	public final void run()
	{
		while(true)
		{
			synchronized(this)
			{
				if(bQuit)
				{
					bHasQuit=true;
					notifyAll();
					return;
				}

				try
				{
					wait(iDelay);
				}
				catch(InterruptedException ie)
				{
				}

				if(bQuit)
				{
					bHasQuit=true;
					notifyAll();
					return;
				}
			}

			try
			{
				tick();
			}
			catch(Throwable t)
			{
				error(t);
			}
		}
	}

	/**
	 * Called when an error occurs in the loop. Base version does nothing.
	 * @param t Error
	 */
	protected void error(Throwable t)
	{
	}

	/**
	 * Closes the thread and waits until it has exited.
	 */
	public synchronized void close()
	{
		bQuit=true;
		notifyAll();
		while(!bHasQuit)
		{
			try
			{
				wait();
			}
			catch(InterruptedException e)
			{
			}
		}
	}


}
