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
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This test program lists all the fonts that contain a certain character.
 */
public class TestListFontsWithChar {

	/**
	 * Main method.
	 * @param ignored not used
	 */
	public static void main(String[] ignored) {
		try {
			String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().
					getAvailableFontFamilyNames();
			while (true) {
				System.out.print("\nCharacter to test: ");
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String input = br.readLine().trim();
				int c = input.codePointAt(0);
				for (int i = 0; i < fonts.length; i++) {
					Font f = new Font(fonts[i], Font.PLAIN, 16);
					if (f.canDisplay(c)) {
						System.out.println(fonts[i]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
