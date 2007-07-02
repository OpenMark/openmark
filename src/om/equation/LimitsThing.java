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
package om.equation;

import java.awt.Graphics2D;

/** Something that shows limits using super/subscript. */
public abstract class LimitsThing extends Item implements SuperSubHolder.EatsOwnSuperSub
{
	private Item iSuper,iSub;
	
	private int iSuperX=0,iSubX=0,iContentsX=0;
	
	@Override
	public final void render(Graphics2D g2,int iX,int iY)
	{
		renderContents(g2,iX,iY,iContentsX);
		
		if(iSuper!=null)
		{
			iSuper.render(g2,iX+iSuperX,iY);
		}
		if(iSub!=null)
		{
			iSub.render(g2,iX+iSubX,iY+iHeight-iSub.getHeight());
		}
	}
	
	/**
	 * Override to render the actual contents of this. Should render relative
	 * to baseline (which may have been shifted) and with the given offset over
	 * initial position.
	 * @param g2 Graphics context
	 * @param iX X co-ordinate 
	 * @param iY Y co-ordinate 
	 * @param iOffsetX X offset to draw at
	 */
	protected abstract void renderContents(Graphics2D g2,int iX,int iY,int iOffsetX);

	/** @return True if limits go alongside, false if they go above/below */
	protected abstract boolean isAlongside();

	public void attachSuperSub(Item superscript,Item subscript)
	{
		this.iSuper=superscript; this.iSub=subscript;
		
		if(isAlongside())
		{
			// Calculate revised width and offset for where to draw sigma
			int 
				iSuperWidth=superscript==null ? 0 : superscript.getWidth(), 
				iSubWidth=subscript==null ? 0 : subscript.getWidth();
			int iOldWidth=iWidth;
			iWidth=Math.max(iOldWidth+getSuitableGap()+iSuperWidth,iOldWidth+getSuitableGap()+iSubWidth);
			
			iSuperX=iSubX=iOldWidth+getSuitableGap();
			int 
				iSuperHeight=superscript==null ? 0 : superscript.getHeight(),
				iSubHeight=subscript==null ? 0 : subscript.getHeight();
			
			if(iSuperHeight+iSubHeight+getSuitableGap() > iHeight)
			{
				int iDifference=iSuperHeight+iSubHeight+getSuitableGap() - iHeight;
				iBaseline+=iDifference/2;
				iHeight+=(iDifference+1)/2;				
			}
			
			// Looks better with more rightmargin
			iRightMargin=getZoomed(4);
		}
		else
		{
			// Calculate revised width and offset for where to draw sigma
			int 
				iSuperWidth=superscript==null ? 0 : superscript.getWidth(), 
				iSubWidth=subscript==null ? 0 : subscript.getWidth();
			int iOldWidth=iWidth;
			iWidth=Math.max(Math.max(iWidth,iSuperWidth),iSubWidth);
			
			// Calculate revised height and baseline for superscript...
			if(superscript!=null)
			{
				iBaseline+=superscript.getHeight()+getSuitableGap();
				iHeight+=superscript.getHeight()+getSuitableGap();
			}
			if(subscript!=null)
			{
				iHeight+=subscript.getHeight()+getSuitableGap();
			}		
			
			iContentsX=(iWidth-iOldWidth)/2;
			iSuperX=(iWidth-iSuperWidth)/2;
			iSubX=(iWidth-iSubWidth)/2;
		}
	}

	public boolean wantsSuperSub()
	{
		return true;
	}	
}
