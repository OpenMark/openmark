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
package om.tnavigator.auth;

/** Information about a user */
public interface UserDetails
{
	/** Default user details for user who isn't logged in */
	public static UserDetails NULLDETAILS = new NullUserDetails();

	/** @return Username or empty string if user is not logged in */
	public String getUsername();

	/** @return Person ID or empty string if user isn't logged on */
	public String getPersonID();

	/** @return Authentication cookie or empty string if user is not logged in */
	public String getCookie();

	/** @return whether user is logged in at all */
	public boolean isLoggedIn();

	/** @return True if user should receive emails confirming submission
	 *  (where enabled in test) i.e. you can return false if somebody isn't
	 *  an actual student, if you like. Anyone marked admin automatically doesn't
	 *  get the emails. */
	public boolean shouldReceiveTestMail();

	/**
	 * @param sAuthId the authid to check.
	 * @return True if user has given authid
	 */
	public boolean hasAuthID(String sAuthId);

	/**
	 * Intended for use in XSL; converts authids to a space-separated list.
	 * @return Space-separated list of all authids.
	 */
	public String getAuthIDsAsString();
}
