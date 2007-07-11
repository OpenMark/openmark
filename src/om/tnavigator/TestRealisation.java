/* OpenMark online assessment system
 * Copyright (C) 2007 The Open University
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package om.tnavigator;

import om.OmFormatException;

import org.w3c.dom.Element;

import util.xml.XML;

/**
 * This class stores the structure of a test, as realised for a particular 
 * user. That means that, in section which pick questions at random, 
 * the particular questions used have been chosen.
 */
public class TestRealisation {
	// Random seed for session.
	private long randomSeed;
	
	// If not -1, this indicates a fixed variant has been selected.
	private int fixedVariant=-1;

	// ID of in-progress test deployment.
	private String testId = null;
	
	// The top level test group of the test.
	private TestGroup rootTestGroup;
	
	// Test items, linearised.
	private TestLeaf[] testLeavesInOrder;
	
	private TestRealisation(TestGroup rootTestGroup, TestLeaf[] testLeavesInOrder,
			long randomSeed, int fixedVariant, String testId) {
		this.rootTestGroup = rootTestGroup;
		this.testLeavesInOrder = testLeavesInOrder;
		this.randomSeed = randomSeed;
		this.fixedVariant = fixedVariant;
		this.testId = testId;
	}

	static TestRealisation realiseTest(TestDefinition def, long lRandomSeed, int iFixedVariant,
			String testId) throws OmFormatException {
		TestGroup testGroup = def.getResolvedContent(lRandomSeed);
		return new TestRealisation(testGroup, testGroup.getLeafItems(), lRandomSeed, iFixedVariant, testId);
	}
	
	static TestRealisation realiseSingleQuestion(String question, long lRandomSeed,
			int iFixedVariant, String testId) throws OmFormatException {
		TestLeaf[] leaves = new TestLeaf[1];
		Element e=XML.createDocument().createElement("question");
		e.setAttribute("id",question);
		leaves[0] = new TestQuestion(null,e);

		return new TestRealisation(null, leaves, lRandomSeed, iFixedVariant, testId);
	}
	
	/**
	 * @param tg the tg to set
	 */
	void setRootTestGroup(TestGroup tg) {
		this.rootTestGroup = tg;
	}

	/**
	 * @return the tg
	 */
	TestGroup getRootTestGroup() {
		return rootTestGroup;
	}

	/**
	 * @param atl the atl to set
	 */
	void setTestLeavesInOrder(TestLeaf[] atl) {
		this.testLeavesInOrder = atl;
	}

	/**
	 * @return the atl
	 */
	TestLeaf[] getTestLeavesInOrder() {
		return testLeavesInOrder;
	}

	/**
	 * @return the sTestID
	 */
	public String getTestId() {
		return testId;
	}

	/**
	 * @return the lRandomSeed
	 */
	long getRandomSeed() {
		return randomSeed;
	}

	/**
	 * @param fixedVariant the iFixedVariant to set.
	 */
	void setFixedVariant(int fixedVariant) {
		this.fixedVariant = fixedVariant;
	}

	/**
	 * @return the iFixedVariant
	 */
	int getFixedVariant() {
		return fixedVariant;
	}
}
