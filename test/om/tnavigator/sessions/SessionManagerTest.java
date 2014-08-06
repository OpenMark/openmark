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
package om.tnavigator.sessions;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.servlet.http.Cookie;

import om.Log;
import om.OmException;
import om.tnavigator.MockHttpServletRequest;
import om.tnavigator.MockHttpServletResponse;
import om.tnavigator.NavigatorConfig;
import om.tnavigator.auth.Authentication;
import om.tnavigator.auth.simple.MockAuthentication;
import om.tnavigator.sessions.ClaimedUserDetails.Status;

import org.junit.Assert;
import org.junit.Test;

import util.misc.StopException;

/**
 * Unit tests for SessionManager.
 */
public class SessionManagerTest
{

	protected File loadFileFromClassPath(String name) {
		URL url = ClassLoader.getSystemResource(name);
		if (null == url) {
			return null;
		}
		return new File(url.getPath().replace("%20", " "));
	}

	@Test
	public void testAlreadyLoggedInWithSessionCanAccessWorldAccessibleTest() throws IOException, StopException, OmException
	{
		NavigatorConfig nc = new NavigatorConfig();
		Log l = new Log(new File(System.getProperty("java.io.tmpdir")),
				"SessionManagerTest", true);
		SessionManager sessionManager = new SessionManager(nc, l);
		UserSession session = new UserSession("1234567");
		session.usernameTestIdKey = "name-1234567";
		session.cookieCreated = true;
		sessionManager.addSession(session);

		MockAuthentication authentication = new MockAuthentication();
		authentication.setTestUser("1234567", "name", "name@example.com", "tnavigator_session_valid-test-mu120.module5");
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addCookie(new Cookie("tnavigator_session_valid-test-mu120.module5", "1234567"));
		MockHttpServletResponse response = new MockHttpServletResponse();
		String testId = "valid-test-mu120.module5";

		ClaimedUserDetails claimedDetails = sessionManager.tryToFindUserSession(authentication,
				request, response, false, testId, loadFileFromClassPath("valid-test-mu120.module5.deploy.xml"));

		Assert.assertEquals(Status.OK, claimedDetails.status);
		Assert.assertEquals(0, response.getNumCookies());
	}

	@Test
	public void testAlreadyLoggedInNoTestCookieRedirects() throws IOException, StopException, OmException
	{
		NavigatorConfig nc = new NavigatorConfig();
		Log l = new Log(new File(System.getProperty("java.io.tmpdir")),
				"SessionManagerTest", true);
		SessionManager sessionManager = new SessionManager(nc, l);

		MockAuthentication authentication = new MockAuthentication();
		authentication.setTestUser("1234567", "name", "name@example.com", "tnavigator_session_valid-test-mu120.module5");
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestUri("http://example.com/valid-test-mu120.module5/");
		MockHttpServletResponse response = new MockHttpServletResponse();
		String testId = "valid-test-mu120.module5";

		ClaimedUserDetails claimedDetails = sessionManager.tryToFindUserSession(authentication,
				request, response, false, testId, loadFileFromClassPath("valid-test-mu120.module5.deploy.xml"));
		UserSession us = claimedDetails.us;

		Assert.assertEquals(Status.REDIRECTING, claimedDetails.status);
		Assert.assertEquals("name-valid-test-mu120.module5", us.usernameTestIdKey);
		Assert.assertEquals(1, response.getNumCookies());
		Assert.assertEquals(us.sCookie,
				response.getCookieValue("tnavigator_session_valid-test-mu120.module5"));
	}

	@Test
	public void testNotLoggedInInitialisesCorrectly() throws IOException, StopException, OmException
	{
		NavigatorConfig nc = new NavigatorConfig();
		Log l = new Log(new File(System.getProperty("java.io.tmpdir")),
				"SessionManagerTest", true);
		SessionManager sessionManager = new SessionManager(nc, l);

		Authentication authentication = new MockAuthentication();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addCookie(new Cookie("tnavigator_xid", "_abcdefg"));
		request.setRequestUri("http://example.com/valid-test-mu120.module5/");
		MockHttpServletResponse response = new MockHttpServletResponse();

		String testId = "valid-test-mu120.module5";

		ClaimedUserDetails claimedDetails = sessionManager.tryToFindUserSession(authentication,
				request, response, false, testId, loadFileFromClassPath("valid-test-mu120.module5.deploy.xml"));
		UserSession us = claimedDetails.us;

		Assert.assertEquals(Status.REDIRECTING, claimedDetails.status);
		Assert.assertEquals("_abcdefg-valid-test-mu120.module5", us.usernameTestIdKey);
		Assert.assertEquals(1, response.getNumCookies());
		Assert.assertEquals(us.sCookie,
				response.getCookieValue("tnavigator_session_valid-test-mu120.module5"));
	}

	@Test
	public void testNotLoggedNoFakeUsernameYet() throws IOException, StopException, OmException
	{
		NavigatorConfig nc = new NavigatorConfig();
		Log l = new Log(new File(System.getProperty("java.io.tmpdir")),
				"SessionManagerTest", true);
		SessionManager sessionManager = new SessionManager(nc, l);

		Authentication authentication = new MockAuthentication();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestUri("http://example.com/valid-test-mu120.module5/");
		MockHttpServletResponse response = new MockHttpServletResponse();
		String testId = "valid-test-mu120.module5";

		ClaimedUserDetails claimedDetails = sessionManager.tryToFindUserSession(authentication,
				request, response, false, testId, loadFileFromClassPath("valid-test-mu120.module5.deploy.xml"));
		UserSession us = claimedDetails.us;

		Assert.assertEquals(Status.REDIRECTING, claimedDetails.status);
		Assert.assertEquals('_', us.sOUCU.charAt(0));
		Assert.assertEquals(us.sOUCU + "-valid-test-mu120.module5", us.usernameTestIdKey);
		Assert.assertEquals(2, response.getNumCookies());
		Assert.assertEquals(us.sOUCU, response.getCookieValue("tnavigator_xid"));
		Assert.assertEquals(us.sCookie,
				response.getCookieValue("tnavigator_session_valid-test-mu120.module5"));
	}
}
