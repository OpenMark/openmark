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
package om.tnavigator;

import java.util.*;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;

/** Utilities for sending email. */
public class Mail
{
	/** text/plain MIME type */
	public final static String TEXTPLAIN="text/plain";
	
	/** SMTP server */
	private static String smtpHost=null;
	
	/**
	 * Sets the SMTP host used for sending mail
	 * @param smtpHost Host name
	 */
	static void setSMTPHost(String smtpHost)
	{
		Mail.smtpHost=smtpHost;
	}
	
	/**
	 * Sends the given mail message with a given MIME format.
	 * @param from Sender address
	 * @param replyTo Reply-to address
	 * @param to Array of target addresses
	 * @param cc Array of CC addresses - or null
	 * @param subject Subject
	 * @param message Message
	 * @param mimeType Mime-type of the message; if null, sends text/plain.
	 * @throws MessagingException If any failure occurs in contacting mail server
	 *         etc.
	 */
	public static void send(String from,String replyTo,String[] to,
		String[] cc,String subject,String message, String mimeType) throws MessagingException
	{
		MimeMessage mm=createMessage(from,replyTo,to,cc, subject);
		if(mimeType==null)
		{
			mm.setText(message,"UTF-8");
		}
		else
		{
			mm.setDataHandler(new DataHandler(message,mimeType));
		}
		Transport.send(mm);
	}

	/**
	 * Create an empty MimeMessage object with all properties set
	 * 
	 * @param from Sender address
	 * @param replyTo Reply-to address (null to omit)
	 * @param to Array of target addresses
	 * @param cc Array of CC addresses - or null
	 * @param subject Subject
	 */
	private static MimeMessage createMessage(String from,String replyTo,
		String[] to,String[] cc, String subject) throws MessagingException
	{
		Properties p=new Properties();
		p.setProperty("mail.transport.protocol","smtp");
		p.setProperty("mail.smtp.host",smtpHost);
		p.setProperty("mail.from",from);
		Session s=Session.getInstance(p);
		MimeMessage mm=new MimeMessage(s);

		InternetAddress[] aiaTo=new InternetAddress[to.length];
		for(int i=0;i<aiaTo.length;++i)
		{
			aiaTo[i]=new InternetAddress(to[i]);
		}
		mm.addRecipients(Message.RecipientType.TO,aiaTo);

		if(cc!=null)
		{
			InternetAddress[] aiaCC=new InternetAddress[cc.length];
			for(int i=0;i<aiaCC.length;++i)
			{
				aiaCC[i]=new InternetAddress(cc[i]);
			}
			mm.addRecipients(Message.RecipientType.CC,aiaCC);
		}
		
		if(replyTo!=null)
		{
			InternetAddress[] aiaReplyTo=new InternetAddress[1];
			aiaReplyTo[0]=new InternetAddress(replyTo);
			mm.setReplyTo(aiaReplyTo);
		}

		mm.setFrom(new InternetAddress(from));
		mm.setSubject(subject,"UTF-8");
		mm.setSentDate(new Date());

		return mm;
	}
}
