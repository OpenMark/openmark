package om.equation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

/**
 * Puts a wide hat(circumflex) on top of child item.
 */
public class Hat extends Item
{	
	/** Space between hat and text */
	private int iGap;
	
	@Override
	public void render(Graphics2D g2, int iX, int iY) 
	{
		Item i=getChildren()[0];
		GeneralPath gp=new GeneralPath();

		Color cForeground=getForeground();
		g2.setColor(cForeground);

		float iYPos=iWidth<iBaseline ? 0 : iWidth/6f;

		gp.moveTo(iX,iY+iWidth/3f);
		gp.lineTo(iX+iWidth/2f,iY+iYPos);
		gp.lineTo(iX+iWidth,iY+iWidth/3f);

		g2.setStroke(new BasicStroke(getZoom()*3/4));
		g2.draw(gp);
		
		i.render(g2,iX,iY+iWidth/3+iGap);
	}
	
	@Override
	protected void internalPrepare() 
	{
		Item i=getChildren()[0];
		iGap=getZoomed(2);
		iHeight=i.getHeight();
		iWidth=i.getWidth();
		iBaseline=i.getBaseline()+iWidth/3+iGap;
	}
	
	/**
	 * @param f ItemFactory to register this class with.
	 */
	public static void register(ItemFactory f)
	{
		f.addItemClass("hat",new ItemCreator()
			{	public Item newItem()	 {	return new Hat();	}	});
	}	

}
