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
package om.question;

import om.helper.QEngineConfig;

/**
 * Parameters that are passed to a question when it is being initialised.
 * <p>
 * This is represented by a class, rather than including separate parameters,
 * so that we can add necessary initialisation parameters without having to
 * recompile code.
 */
public class InitParams
{
	/**
	 * Value that should be used to seed random number generators used to
	 * determine which question to ask.
	 */
	private long lRandomSeed;

	/** Fixed colours for accessibility */
	private String sFixedColourFG,sFixedColourBG;

	/** Zoom level */
	private double dZoom;

	/** Plain mode */
	private boolean bPlainMode;

	/** ClassLoader for resources */
	private ClassLoader cl;

	/** Variant mode */
	private int iFixedVariant=-1;

	private QEngineConfig qeConfig;
	
	/** Question attempt */
	private int attempt=1;
	
	/** The version of the Om test navigator software that started the attempt. */
	private String navigatorVersion;

	/**
	 * Construct and set values.
	 * @param lRandomSeed Random number seed
	 * @param sFixedColourFG Fixed FG colour #rrggbb or null if not fixed
	 * @param sFixedColourBG Fixed BG colour or null
	 * @param dZoom Zoom factor (1.0 = normal)
	 * @param bPlainMode Plain mode on/off
	 * @param cl ClassLoader for resources
	 * @param iFixedVariant Fixed question variant (-1 for normal)
	 * @param attempt Question attempt
	 * @param navigatorVersion version of the OpenMark test navigator 
	 * @param config 
	 */
	public InitParams(long lRandomSeed,String sFixedColourFG,String sFixedColourBG,
		double dZoom,boolean bPlainMode,ClassLoader cl,int iFixedVariant, QEngineConfig config, 
		int attempt, String navigatorVersion)
	{
		this.lRandomSeed=lRandomSeed;
		this.sFixedColourFG=sFixedColourFG;
		this.sFixedColourBG=sFixedColourBG;
		this.dZoom=dZoom;
		this.bPlainMode=bPlainMode;
		this.cl=cl;
		this.iFixedVariant=iFixedVariant;
		this.qeConfig = config;
		this.attempt = attempt;
		this.navigatorVersion = navigatorVersion;
	}

	/**
	 * @return Value that should be used to seed random number generators used
	 *   to determine which question to ask
	 */
	public long getRandomSeed()
	{
		return lRandomSeed;
	}

	/** @return Fixed FG colour #rrggbb or null if not fixed */
	public String getFixedColourFG()
	{
		return sFixedColourFG;
	}

	/** @return Fixed BG colour #rrggbb or null if not fixed */
	public String getFixedColourBG()
	{
		return sFixedColourBG;
	}

	/** @return Zoom level (default 1.0) */
	public double getZoom()
	{
		return dZoom;
	}

	/** @return True if plain mode (no Javascript etc) is selected */
	public boolean isPlainMode()
	{
		return bPlainMode;
	}

	/** @return ClassLoader that should be used to load resources */
	public ClassLoader getClassLoader()
	{
		return cl;
	}

	/** @return True if the question variant has been fixed */
	public boolean hasFixedVariant()
	{
		return iFixedVariant!=-1;
	}

	/** @return Fixed variant number */
	public int getFixedVariant()
	{
		return iFixedVariant;
	}

	/**
	 * @return the QEngineConfig to get config information from.
	 */
	public QEngineConfig getQEngineConfig() {
		return qeConfig;
	}
	
	/**
	 * @return question attempt number
	 */
	public int getAttempt() {
		return attempt;
	}

	/**
	 * @return navigatorVersion version number of the Om test navigator.
	 */
	public String getNavigatorVersion() {
		return navigatorVersion;
	}	

	// TODO Add information about the jar files for any shared packages so we
	// can go find any additional question component classes (see
	// QComponentManager)
}
