/*
 * Copyright (c) 2009, 2010, 2011, B3log Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.latke.mail.local;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.b3log.latke.mail.MailService;
import org.b3log.latke.service.ServiceException;

/**
 * Email sender.
 * 
 * @author <a href="mailto:jiangzezhou1989@gmail.com">zezhou jiang</a>
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 */
public final class MailSender implements Runnable {

	/** The default encoding: 'utf-8' */
	public static final String DEFAULT_ENCODING = "utf-8";

	/** The default debugSession: false */
	public static final boolean DEFAULT_SESSION_DEBUG = false;

	/**
	 * set default encoding
	 */
	private String encoding = DEFAULT_ENCODING;

	/**
	 * set default sessionDebug
	 */
	private boolean sessionDebug = DEFAULT_SESSION_DEBUG;

	private String mailHost;

	/**
	 * username of the mailHost account
	 */
	private String username;

	/**
	 * password of the mailHost account
	 */
	private String password;

	private MailService.Message message;

	private static MailSender mailSender;

	/**
	 * private constructor
	 */
	private MailSender() {

	}

	/**
	 * Assign values ​​to variables
	 * 
	 * @param username
	 * @param password
	 * @param encoding
	 * @param mailHost
	 * @param debugSession
	 */
	public static void init(final String username, final String password,
			final String encoding, final String mailHost,
			final boolean sessionDebug) {
		mailSender = new MailSender();
		mailSender.username = username;
		mailSender.password = password;
		mailSender.encoding = encoding == null ? DEFAULT_ENCODING : encoding;
		mailSender.mailHost = mailHost;
		mailSender.sessionDebug = sessionDebug == false ? DEFAULT_SESSION_DEBUG
				: sessionDebug;

	}

	/**
	 * Assign values ​​to variables
	 * 
	 * @param username
	 * @param password
	 * @param mailHost
	 */
	public static void init(final String username, final String password,
			final String mailHost) {
		init(username, password, null, mailHost, false);
	}

	/**
	 * Assign values ​​to variables
	 * 
	 * @param username
	 * @param password
	 * @param mailHost
	 * @param debugSession
	 */
	public static void init(final String username, final String password,
			final String mailHost, boolean debugSession) {
		init(username, password, null, mailHost, debugSession);
	}

	/**
	 * Assign values ​​to variables
	 * 
	 * @param username
	 * @param password
	 * @param encoding
	 * @param mailHost
	 */
	public static void init(final String username, final String password,
			final String encoding, final String mailHost) {
		init(username, password, encoding, mailHost, false);

	}

	/**
	 * Gets mailSender.
	 * 
	 * @param message
	 * @return mailSender
	 * @throws ServiceException
	 */
	protected static MailSender getInstance(MailService.Message message)
			throws ServiceException {
		if (mailSender == null) {
			throw new ServiceException("MailSender is not init...");
		}
		mailSender.message = message;
		return mailSender;
	}

	/**
	 * create java.mail.Message
	 * 
	 * @return java.mail.Message
	 * @throws MessagingException
	 */
	public javax.mail.Message createMessage() throws MessagingException {

		/*
		 * Properties used to construct a email sending connection protocal.
		 */

		final Properties props = new Properties();
		props.put("mail.smtp.host", this.mailHost);
		props.put("mail.smtp.auth", "true");

		final Authenticator auth = new SMTPAuthenticator();
		Session session = Session.getDefaultInstance(props, auth);
		session.setDebug(this.sessionDebug);
		final MimeMessage mimeMessage = new MimeMessage(session);
		mimeMessage.setFrom(new InternetAddress(message.getFrom()));
		mimeMessage.setSubject(message.getSubject());
		mimeMessage.setContent(message.getHtmlBody(), "text/html;charset="
				+ this.encoding);

		mimeMessage.addRecipients(RecipientType.TO,
				transformRecipients(message.getRecipients()));

		return mimeMessage;
	}

	/**
	 * transport recipients to InternetAddress[]
	 * 
	 * @param recipients
	 * @return
	 * @throws MessagingException
	 */
	private InternetAddress[] transformRecipients(Set<String> recipients)
			throws MessagingException {
		if (recipients.size() == 0) {
			throw new MessagingException(
					"recipient for Mail should not be null");
		}
		InternetAddress[] realRecipients = new InternetAddress[recipients
				.size()];
		int i = 0;
		for (String recipient : recipients) {
			realRecipients[i++] = new InternetAddress(recipient);
		}
		return realRecipients;
	}

	/**
	 * Sends email.
	 * 
	 * @throws MessagingException
	 *             message exception
	 */
	public void sendMail() throws MessagingException {

		Transport.send(createMessage());

	}

	@Override
	public void run() {
		try {
			sendMail();
		} catch (final MessagingException ex) {
			Logger.getLogger(MailSender.class.getName())
					.severe(ex.getMessage());
		}
	}

	/**
	 * Inner class for Authenticator.
	 */
	private class SMTPAuthenticator extends Authenticator {

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(MailSender.this.username,
					MailSender.this.password);
		}
	}

}
