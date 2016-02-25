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

/** Basic information from cookie without needing to authenticate */
public interface UncheckedUserDetails
{
	/** @return User OUCU (not validated at this stage) or null if none */
	public String getUsername();

	/** @return Entire auth cookie or null if none */
	public String getCookie();

	/** @return Student PI or staff number or whatever */
	public String getPersonId();

	/**  @return cookie name null if none */
	public String getsCookieName();
}
