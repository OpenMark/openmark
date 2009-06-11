package om.equation;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import util.misc.Fonts;

/**
 * Displays Contour Integral sign. Works same as Integral.
 */
public class ContourIntegral extends LimitsThing implements SuperSubHolder.EatsOwnSuperSub
{
	private final static char CONTOURINTEGRAL='\u222e';
	
	private int iYOffset;

	@Override
	protected void renderContents(Graphics2D g2,int iX,int iY,int iOffsetX)
	{
		Color cForeground=getForeground();	
		g2.setColor(cForeground);
		Font f=getBigFont();
		g2.setFont(f);
		g2.drawString(CONTOURINTEGRAL+"",iX+iOffsetX+Fonts.getLeftOverlap(f,CONTOURINTEGRAL),iY+iYOffset+iBaseline);
	}

	/**
	 * @param f ItemFactory to register this class with.
	 */
	public static void register(ItemFactory f)
	{
		f.addItemClass("oint",new ItemCreator()
			{	public Item newItem()	 {	return new ContourIntegral();	}	});
	}

	@Override
	protected void internalPrepare()
	{
		// Cannot completely prepare because the super/sub aren't attached yet.
		// But, because there may be none coming, we need to do additional
		// preparation.

		Font f=getBigFont();
		int
			iAscent=Fonts.getAscent(f,CONTOURINTEGRAL),
			iDescent=Fonts.getDescent(f,CONTOURINTEGRAL);

		iHeight=iAscent+iDescent;
		iWidth=Fonts.getRightExtent(f,CONTOURINTEGRAL)+Fonts.getLeftOverlap(f,CONTOURINTEGRAL);
		iBaseline=
			(getTextSize()==TEXTSIZE_DISPLAY) ? (iAscent*3)/4 : (iAscent*5)/6;
		iYOffset=iAscent-iBaseline;

		iLeftMargin=iRightMargin=getZoomed(2);
	}

	@Override
	protected boolean isAlongside()
	{
		return true;
	}

	private Font getBigFont()
	{
		// Bigger than sigmas in display mode but not otherwise
		if(getTextSize()==TEXTSIZE_DISPLAY)
			return new Font(SPECIAL_CHAR_FONTFAMILY,Font.PLAIN,(getZoomed(convertTextSize(getTextSize()))*5)/2);
		else
			return new Font(SPECIAL_CHAR_FONTFAMILY,Font.PLAIN,(getZoomed(convertTextSize(getTextSize()))*3)/2);
	}
}

