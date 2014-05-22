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
		session.sCheckedOUCUKey = "name-1234567";
		session.cookieCreated = true;
		sessionManager.addSession(session);

		Authentication authentication = new MockAuthentication();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addCookie(new Cookie("tnavigator_session_valid-test-mu120.module5", "1234567"));
		MockHttpServletResponse response = new MockHttpServletResponse();
		long requestStartTime = System.currentTimeMillis();
		String testId = "valid-test-mu120.module5";

		ClaimedUserDetails claimedDetails = sessionManager.tryToFindUserSession(authentication,
				request, response, requestStartTime, testId);

		UserSession us = claimedDetails.us;
		synchronized (us) {
			boolean sessionOK = sessionManager.verifyUserSession(authentication,
					request, response, claimedDetails, "valid-test-mu120.module5",
					loadFileFromClassPath("valid-test-mu120.module5.deploy.xml"));
			Assert.assertTrue(sessionOK);
			Assert.assertEquals(0, response.getNumCookies());
		}
	}

	@Test
	public void testAlreadyLoggedInNoTestCookieRedirects() throws IOException, StopException, OmException
	{
		NavigatorConfig nc = new NavigatorConfig();
		Log l = new Log(new File(System.getProperty("java.io.tmpdir")),
				"SessionManagerTest", true);
		SessionManager sessionManager = new SessionManager(nc, l);

		Authentication authentication = new MockAuthentication();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestUri("http://example.com/valid-test-mu120.module5/");
		MockHttpServletResponse response = new MockHttpServletResponse();
		long requestStartTime = System.currentTimeMillis();
		String testId = "valid-test-mu120.module5";

		ClaimedUserDetails claimedDetails = sessionManager.tryToFindUserSession(authentication,
				request, response, requestStartTime, testId);

		UserSession us = claimedDetails.us;
		synchronized (us) {
			boolean sessionOK = sessionManager.verifyUserSession(authentication,
					request, response, claimedDetails, "valid-test-mu120.module5",
					loadFileFromClassPath("valid-test-mu120.module5.deploy.xml"));
			Assert.assertFalse(sessionOK);
			Assert.assertEquals(1, response.getNumCookies());
			Assert.assertEquals(us.sCookie,
					response.getCookieValue("tnavigator_session_valid-test-mu120.module5"));
		}
	}
}
