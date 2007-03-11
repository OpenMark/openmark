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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.swing.JLabel;

/** Image-related utility methods. */
public class ImageUtils
{
	/**
	 * Returns an Image that's fully loaded. Once it's fully loaded you can 
	 * safely call getWidth with null, for instance.
	 * @param abData Data for image
	 * @return New fully loaded Image object
	 * @throws IOException
	 */
	public static Image load(byte[] abData) throws IOException
	{
		Image i=Toolkit.getDefaultToolkit().createImage(abData);
		loadFully(i);
		return i;
	}

	/**
	 * Returns a BufferedImage.
	 * @param abData Data for image
	 * @param bAlpha True if it needs an alpha channel 
	 * @return New BufferedImage object
	 * @throws IOException
	 */
	public static Image loadBuffered(byte[] abData,boolean bAlpha) throws IOException
	{
		Image i=Toolkit.getDefaultToolkit().createImage(abData);
		loadFully(i);
		return getBuffered(i,bAlpha);
	}
	
  /**
   * Waits for an image to load fully.
   * @param i Original image
   * @throws IOException
   */
	public static void loadFully(Image i) throws IOException
	{
		JLabel dummy=new JLabel();

		// Create image and wait for it to finish
		MediaTracker mediaTracker=new MediaTracker(dummy);
		mediaTracker.addImage(i,1);
		try
		{
			mediaTracker.waitForAll();
		}
		catch(InterruptedException e)
		{
			throw new InterruptedIOException();
		}
	}
	
  /**
   * Obtains buffered image from a fully loaded Image.
   * @param i Original image
   * @param bAlpha True if you need an alpha channel
   * @return Fully loaded BufferedImage 
   * @throws IOException
   */
	public static BufferedImage getBuffered(Image i,boolean bAlpha) throws IOException
	{
		// Get size
		JLabel dummy=new JLabel();
		int iWidth=i.getWidth(dummy);
		int iHeight=i.getHeight(dummy);
		if(iWidth<0||iHeight<0)
		{
			throw new IOException("Error loading image: failed to get w/h " +
				"(did you forget to call loadFully?)");
		}
		
		// Make buffered image
		BufferedImage bi=new BufferedImage(iWidth,iHeight,
			bAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
		bi.createGraphics().drawImage(i,0,0,iWidth,iHeight,dummy);
		return bi;
	}	
}
