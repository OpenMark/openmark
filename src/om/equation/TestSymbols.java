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

import java.awt.Font;
import java.awt.GridLayout;
import java.util.Map;

import javax.swing.*;

/**
 * Class for testing the equation compontent.
 */
public class TestSymbols extends JFrame
{
	private final static Font FONT = new Font("Lucida Sans Unicode", Font.PLAIN, 14);
	/**
	 * @param args not used.
	 */
	public static void main(String[] args)
	{
		try {
			new TestSymbols();
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	TestSymbols() throws Exception
	{
		super("Equation symbol test");

		int iCols=8;

		((JComponent)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		getContentPane().setLayout(new GridLayout(0,iCols,10,10));
		for (Map.Entry<String, String> item : SimpleNode.STANDARDSYMBOLS.entrySet()) {
			getContentPane().add(getSymbol(item.getKey(),item.getValue()));
		}
		pack();
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	private JLabel getSymbol(String name, String character) throws Exception
	{
		JLabel l = new JLabel(name + ": " + character);
		l.setFont(FONT);
		return l;
		
	}
}
