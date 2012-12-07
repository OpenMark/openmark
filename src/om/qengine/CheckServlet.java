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
package om.qengine;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import om.OmVersion;
import util.misc.IO;
import util.misc.NameValuePairs;

/** Servlet just to provide the automated check page */
public class CheckServlet extends HttpServlet
{
	/** Required by the Serializable interface. */
	private static final long serialVersionUID = 2396435665590375099L;
	private final static int MB=1024*1024;
	private final static int FREEMEMORYREQ=3*MB;

	@Override
	protected void doGet(HttpServletRequest request,HttpServletResponse response)
		throws ServletException,IOException
	{
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		PrintWriter pw=response.getWriter();

		try
		{
			String sInfo="";

			// 1. Check free memory
			Runtime r=Runtime.getRuntime();
			if(r.freeMemory() < FREEMEMORYREQ)
			{
				System.gc();
				if(r.freeMemory() < FREEMEMORYREQ)
				{
					System.gc();
					if(r.freeMemory() < FREEMEMORYREQ)
					{
						throw new Exception("QEFAIL1. Memory low ("+
							((r.totalMemory()-r.freeMemory())/MB)+"MB used; "+(r.freeMemory()/MB)+"MB free)");
					}
				}
			}
			sInfo+="Memory used "+((r.totalMemory()-r.freeMemory())/MB)+"MB; free "+(r.freeMemory()/MB)+"MB. ";

			// 2. Check QE service exists, create if needed
			OmService os=OmService.getStatic();
			if(os==null)
			{
				// Read from URL
				try
				{
					URL u=new URL(request.getRequestURL().toString().replaceAll(
						"/check","/services/Om?method=getEngineInfo"));
					IO.eat(u.openStream());
					sInfo+="Inited service. ";
				}
				catch(Throwable t)
				{
					throw new Exception("QEFAIL2. Attempt to request initial service failed ("+t.getMessage()+")");
				}
				os=OmService.getStatic();
				if(os==null)
					throw new Exception("QEFAIL3. Attempt to request initial service gave no error, but failed nonetheless.");
			}

			// 3. Try to run a 'question session'
			try
			{
				long lStart=System.currentTimeMillis();
				NameValuePairs nvp=new NameValuePairs();
				nvp.add("randomseed","1");
				StartReturn sr=os.start("!test","0.0","",nvp.getNames(),nvp.getValues(),null);
				os.process(sr.getQuestionSession(),null,null);
				os.stop(sr.getQuestionSession());
				long lTime=System.currentTimeMillis()-lStart;
				sInfo+="Start/process time: "+lTime+"ms. ";
			}
			catch(Throwable t)
			{
				throw new Exception("QEFAIL4. Attempt to run test question session failed ("+t.getMessage()+")");
			}

			// Add version
			sInfo+=OmVersion.getVersion()+" ("+OmVersion.getBuildDate()+").";

			// OK then, set status to OK (default, but anyway)
			response.setStatus(HttpServletResponse.SC_OK);
			pw.println("OK. "+sInfo);
		}
		catch(Throwable t)
		{
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			pw.print(t.getMessage()==null ? t.getClass().toString() : t.getMessage());
		}
		finally
		{
			pw.close();
		}

	}

}
