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
package util.misc;

import java.net.URLConnection;
import java.security.cert.*;

import javax.net.ssl.*;

/**
 * HTTPS Utilities.
 */
public class HTTPS
{
	/** Synch for SSL certs */
	private static Object oSSLSynch=new Object();	

	/** SSL context that doesn't use certs */
	private static SSLContext scSSLNoCerts=null;
	
	/**
	 * Allows SSL connections to consider all certificates valid. You do need to also allow different
	 * server names too (if they might be wrong).
	 * @param uc Connection to ignore certs on (if not HTTPS, does nothing)
	 */
	public static void considerCertificatesValid(URLConnection uc) 
	{
		if(!(uc instanceof HttpsURLConnection)) return;
	
		try
		{
			synchronized(oSSLSynch)
			{		
				if(scSSLNoCerts==null)
				{
					scSSLNoCerts=SSLContext.getInstance("TLS");
					scSSLNoCerts.init(null,new TrustManager[] {new X509TrustManager()
					{
						public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
						{
						}
		
						public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
						{
						}
		
						public X509Certificate[] getAcceptedIssuers()
						{
							return new X509Certificate[0];
						}
					}},null);
				}
				
				SSLSocketFactory ssf=scSSLNoCerts.getSocketFactory();
				((HttpsURLConnection)uc).setSSLSocketFactory(ssf);
			}
		}
		catch(Exception e)
		{
			// Can't really happen unless server is set up very, very wrong
			throw new Error(e);
		}
	}

	/**
	 * For use when a connection might be https; allows it to keep working
	 * even if the certificate is for a different-named server. Theoretically
	 * this reduces security, but bleh.
	 * @param uc Connection (can be http, in which case nothing happens,
	 *   or https)
	 */
	public static void allowDifferentServerNames(URLConnection uc)
	{
		if(!(uc instanceof HttpsURLConnection)) return;
		
		((HttpsURLConnection)uc).setHostnameVerifier(new HostnameVerifier()
			{
				public boolean verify(String arg0, SSLSession arg1)
				{
					return true;
				}				
			});
	}
}
