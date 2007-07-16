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

import java.awt.Point;
import java.awt.geom.Point2D;

/** Represents a point in graph co-ordinate space */
public class GraphPoint
{
	/** Co-ordinates */
	private GraphScalar gsX,gsY;

	/** Zero point */
	public final static GraphPoint ZERO=new GraphPoint(GraphScalar.ZERO,GraphScalar.ZERO);

	/** 1,1 point */
	public final static GraphPoint ONE=new GraphPoint(GraphScalar.ONE,GraphScalar.ONE);


	/**
	 * Constructs with world co-ordinates only (no pixel offset).
	 * @param dX X co-ordinate
	 * @param dY Y co-ordinate
	 */
	public GraphPoint(double dX,double dY)
	{
		this(new GraphScalar(dX,0),new GraphScalar(dY,0));
	}

	/**
	 * @param gsX X co-ordinate
	 * @param gsY Y co-ordinate
	 */
	public GraphPoint(GraphScalar gsX,GraphScalar gsY)
	{
		this.gsX=gsX;
		this.gsY=gsY;
	}

	/**
	 * @param dX X co-ordinate
	 * @param iXPixels X pixel offset
	 * @param dY Y co-ordinate
	 * @param iYPixels Y pixel offset
	 */
	public GraphPoint(double dX,int iXPixels,double dY,int iYPixels)
	{
		this(new GraphScalar(dX,iXPixels),new GraphScalar(dY,iYPixels));
	}

	/** @return X co-ordinate */
	public GraphScalar getX()
	{
		return gsX;
	}

	/** @return Y co-ordinate */
	public GraphScalar getY()
	{
		return gsY;
	}

	/**
	 * @param gsNewX New X value
	 * @return New point with that x value
	 */
	GraphPoint newX(GraphScalar gsNewX)
	{
		return new GraphPoint(gsNewX,gsY);
	}

	/**
	 * @param gsNewY New Y value
	 * @return New point with that y value
	 */
	GraphPoint newY(GraphScalar gsNewY)
	{
		return new GraphPoint(gsX,gsNewY);
	}

	/**
	 * Offsets the point by a number of pixels.
	 * @param iX X pixel offset
	 * @param iY Y pixel offset
	 * @return New point
	 */
	public GraphPoint offset(int iX,int iY)
	{
		return new GraphPoint(gsX.offset(iX),gsY.offset(iY));
	}

	/**
	 * Offsets the point by all four elements (x, y, + pixel versions)
	 * @param gpOffset Values to add to this point
	 * @return New point
	 */
	public GraphPoint offset(GraphPoint gpOffset)
	{
		return new GraphPoint(
			gsX.offset(gpOffset.getX()),gsY.offset(gpOffset.getY()));
	}

	/**
	 * Converts to pixel co-ordinates.
	 * @param w World co-ordinate system
	 * @return Corresponding point in pixels
	 */
	public Point convert(World w)
	{
		return new Point(
			w.convertX(gsX),w.convertY(gsY));
	}

	/**
	 * Converts to pixel floating-point co-ordinates.
	 * @param w World co-ordinate system
	 * @return Corresponding point in pixels
	 */
	public Point2D.Float convertFloat(World w)
	{
		return new Point2D.Float(
			w.convertXFloat(gsX.getWorldPosition())+gsX.getPixelOffset(),
			w.convertYFloat(gsY.getWorldPosition())+gsX.getPixelOffset());
	}
}
