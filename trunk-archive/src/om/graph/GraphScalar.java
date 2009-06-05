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
package om.graph;

/**
 * Scalar value used in graph drawing that contains two components: the position
 * in world co-ordinates, and an offset in pixel co-ordinates.
 */
public class GraphScalar
{
	/** Position in world co-ordinate */
	private double dWorldPosition;

	/** Offset in pixels */
	private int iPixelOffset;

	/** Zero point */
	public final static GraphScalar ZERO=new GraphScalar(0.0,0);

	/** One point */
	public final static GraphScalar ONE=new GraphScalar(1.0,0);

	/**
	 * Converts from string of the form world:pixel, or just plain world.
	 * @param s String to convert
	 * @throws NumberFormatException If there's anything wrong with the string
	 */
	GraphScalar(String s) throws NumberFormatException
	{
		int iColon=s.indexOf(":");
		if(iColon==-1)
		{
			iPixelOffset=0;
			dWorldPosition=Double.parseDouble(s);
		}
		else
		{
			iPixelOffset=Integer.parseInt(s.substring(iColon+1));
			dWorldPosition=Double.parseDouble(s.substring(0,iColon));
		}
	}

	/**
	 * @param dWorldPosition Position in world co-ordinates
	 * @param iPixelOffset Offset in pixels
	 */
	public GraphScalar(double dWorldPosition,int iPixelOffset)
	{
		this.dWorldPosition=dWorldPosition;
		this.iPixelOffset=iPixelOffset;
	}

	/**
	 * Offsets an existing co-ordinate.
	 * @param iPixels Number of pixels to offset by
	 * @return New co-ordinate
	 */
	public GraphScalar offset(int iPixels)
	{
		return new GraphScalar(dWorldPosition,iPixelOffset+iPixels);
	}

	/**
	 * Offsets co-ordinate.
	 * @param gsOffset World and pixel offset
	 * @return New co-ordinate
	 */
	public GraphScalar offset(GraphScalar gsOffset)
	{
		return new GraphScalar(
			dWorldPosition+gsOffset.getWorldPosition(),
			iPixelOffset+gsOffset.getPixelOffset());
	}

	/** @return Position in world co-ordinate */
	public double getWorldPosition()
	{
		return dWorldPosition;
	}

	/** @return Offset in pixels */
	public int getPixelOffset()
	{
		return iPixelOffset;
	}
}
