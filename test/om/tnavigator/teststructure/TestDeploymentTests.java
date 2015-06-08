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
package om.tnavigator.teststructure;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import om.OmException;
import om.tnavigator.auth.simple.TestUserCreator;

import org.junit.Test;
import org.w3c.dom.Document;

import util.xml.XML;
import util.xml.XMLException;

public class TestDeploymentTests
{
	TestDeployment makeExampleDeploy() throws OmException, XMLException
	{
		Document d = XML.parse(
				"<deploy>" +
					"<definition>test</definition>" +
					"<access>" +
						"<users>" +
							"<authid>group1</authid>" +
							"<oucu>userA</oucu>" +
						"</users>" +
						"<admins>" +
							"<authid>group2</authid>" +
							"<oucu>userB</oucu>" +
							"<authid reports='yes'>group3</authid>" +
							"<oucu reports='yes'>userC</oucu>" +
							"</admins>" +
						"</access>" +
						"<dates><open>yes</open></dates>" +
					"</deploy>");
		return new TestDeployment(d, new File(""), "test deploy");
	}

	@Test
	public void testHasAccess() throws OmException, XMLException
	{
		TestDeployment deploy = makeExampleDeploy();

		assertTrue(deploy.hasAccess(TestUserCreator.createTestUser("userA")));
		assertTrue(deploy.hasAccess(TestUserCreator.createTestUser("userB")));
		assertTrue(deploy.hasAccess(TestUserCreator.createTestUser("userC")));
		assertFalse(deploy.hasAccess(TestUserCreator.createTestUser("userD")));

		assertTrue(deploy.hasAccess(TestUserCreator.createTestUser("userD", new String[] {"group1"})));
		assertTrue(deploy.hasAccess(TestUserCreator.createTestUser("userD", new String[] {"group2"})));
		assertTrue(deploy.hasAccess(TestUserCreator.createTestUser("userD", new String[] {"group3"})));
		assertTrue(deploy.hasAccess(TestUserCreator.createTestUser("userD", new String[] {"group2", "group3"})));
	}

	@Test
	public void testIsAdmin() throws OmException, XMLException
	{
		TestDeployment deploy = makeExampleDeploy();

		assertFalse(deploy.isAdmin(TestUserCreator.createTestUser("userA")));
		assertTrue(deploy.isAdmin(TestUserCreator.createTestUser("userB")));
		assertTrue(deploy.isAdmin(TestUserCreator.createTestUser("userC")));
		assertFalse(deploy.isAdmin(TestUserCreator.createTestUser("userD")));

		assertFalse(deploy.isAdmin(TestUserCreator.createTestUser("userD", new String[] {"group1"})));
		assertTrue(deploy.isAdmin(TestUserCreator.createTestUser("userD", new String[] {"group2"})));
		assertTrue(deploy.isAdmin(TestUserCreator.createTestUser("userD", new String[] {"group3"})));
		assertTrue(deploy.isAdmin(TestUserCreator.createTestUser("userD", new String[] {"group2", "group3"})));
	}

	@Test
	public void testAllowReports() throws OmException, XMLException
	{
		TestDeployment deploy = makeExampleDeploy();

		assertFalse(deploy.allowReports(TestUserCreator.createTestUser("userA")));
		assertFalse(deploy.allowReports(TestUserCreator.createTestUser("userB")));
		assertTrue(deploy.allowReports(TestUserCreator.createTestUser("userC")));
		assertFalse(deploy.allowReports(TestUserCreator.createTestUser("userD")));

		assertFalse(deploy.allowReports(TestUserCreator.createTestUser("userD", new String[] {"group1"})));
		assertFalse(deploy.allowReports(TestUserCreator.createTestUser("userD", new String[] {"group2"})));
		assertTrue(deploy.allowReports(TestUserCreator.createTestUser("userD", new String[] {"group3"})));
		assertTrue(deploy.allowReports(TestUserCreator.createTestUser("userD", new String[] {"group2", "group3"})));
	}
}
