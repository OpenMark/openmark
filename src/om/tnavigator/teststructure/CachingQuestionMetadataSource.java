/* OpenMark online assessment system
   Copyright (C) 2015 The Open University

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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import om.OmException;
import om.axis.qengine.Score;
import om.tnavigator.NavigatorServlet.RequestTimings;
import util.misc.QuestionName;
import util.misc.QuestionVersion;

/**
 * Implementation of QuestionMetadataSource that caches results of the actual
 * calls (implemented by NavigatorServlet or another QuestionMetadataSource)
 * for the lifetime of this object.
 */
public class CachingQuestionMetadataSource implements QuestionMetadataSource
{
	/**
	 * The Navigator servlet we are associated with.
	 */
	protected QuestionMetadataSource metadataSource;

	/**
	 * Stores the latest version available for each questionId / majorVersion.
	 * In the array keys, in the version part of QuestionName, only the major version
	 * is significant. Minor version is always 0.
	 */
	protected Map<QuestionName, QuestionVersion> questionLatestVersions = new HashMap<QuestionName, QuestionVersion>();

	/**
	 * Stores the latest version available for each questionId.
	 */
	protected Map<QuestionName, Score[]> questionMaxScores = new HashMap<QuestionName, Score[]>();

	/**
	 * Constructor
	 * @param ns the Navigator servlet we are associated with.
	 */
	public CachingQuestionMetadataSource(QuestionMetadataSource metadataSource) {
		this.metadataSource = metadataSource;
	}

	@Override
	public QuestionVersion getLatestVersion(String questionId, int majorVersion) throws OmException
	{
		QuestionName idAndMajorVersion = new QuestionName(questionId, new QuestionVersion(majorVersion, 0));

		QuestionVersion cached = questionLatestVersions.get(idAndMajorVersion);
		if (cached != null)
		{
			return cached;
		}

		QuestionVersion qv = metadataSource.getLatestVersion(questionId, majorVersion);
		questionLatestVersions.put(idAndMajorVersion, qv);
		return qv;
	}

	@Override
	public Score[] getMaximumScores(RequestTimings rt, QuestionName question) throws RemoteException, IOException
	{
		Score[] cached = questionMaxScores.get(question);
		if (cached != null)
		{
			return cached;
		}

		Score[] scores = metadataSource.getMaximumScores(rt, question);
		questionMaxScores.put(question, scores);
		return scores;
	}
}
