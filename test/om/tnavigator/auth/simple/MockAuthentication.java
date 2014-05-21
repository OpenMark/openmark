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
package om.tnavigator.auth.simple;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import om.OmException;
import om.tnavigator.auth.Authentication;
import om.tnavigator.auth.UncheckedUserDetails;
import om.tnavigator.auth.UserDetails;
import om.tnavigator.auth.simple.SimpleUser;
import om.tnavigator.auth.simple.SimpleUncheckedUser;

public class MockAuthentication implements Authentication
{

	@Override
	public UserDetails getUserDetails(HttpServletRequest request, HttpServletResponse response,
			boolean bRequireLogin) throws IOException
	{
		return new SimpleUser("1234567", "name", "name@example.com");
	}

	@Override
	public UncheckedUserDetails getUncheckedUserDetails(HttpServletRequest request)
			throws OmException
	{
		return new SimpleUncheckedUser("1234567", "name", "tnavigator_session_valid-test-mu120.module5");
	}

	@Override
	public void becomeTestUser(HttpServletResponse response, String suffix)
	{
	}

	@Override
	public void close()
	{
	}

	@Override
	public void redirect(HttpServletRequest request, HttpServletResponse response)
			throws IOException
	{
	}

	@Override
	public String sendMail(String username, String personID, String email, int emailType)
			throws IOException
	{
		return null;
	}

	@Override
	public String getLoginOfferXHTML(HttpServletRequest request) throws IOException
	{
		return null;
	}

	@Override
	public void obtainPerformanceInfo(Map<String, Object> m)
	{
	}

	@Override
	public boolean handleRequest(String subPath, HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{
		return false;
	}
}
