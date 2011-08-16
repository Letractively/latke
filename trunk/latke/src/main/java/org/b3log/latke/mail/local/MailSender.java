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

import java.util.Properties;
import java.util.Set;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import org.b3log.latke.mail.MailService;

/**
 * Email sender.
 * 
 * @author <a href="mailto:jiangzezhou1989@gmail.com">zezhou jiang</a>
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 16, 2011
 */
public final class MailSender {

    /**
     * set default sessionDebug.
     */
    private boolean sessionDebug = false;
    /**
     * mail host.
     */
    private String mailHost;
    /**
     * username of the mailHost account.
     */
    private String username;
    /**
     * password of the mailHost account.
     */
    private String password;
    /**
     * mailSender.
     */
    private static MailSender mailSender;

    /**
     * private constructor.
     */
    private MailSender() {
    }

    /**
     * Assign values ​​to variables.
     * 
     * @param username
     * @param password
     * @param encoding
     * @param mailHost
     * @param sessionDebug
     */
    public static void init(final String username, final String password,
                            final String encoding, final String mailHost,
                            final boolean sessionDebug) {
        mailSender = new MailSender();
        mailSender.username = username;
        mailSender.password = password;
        mailSender.mailHost = mailHost;
        mailSender.sessionDebug = sessionDebug;
    }

    /**
     * Assign values ​​to variables.
     * 
     * @param username
     * @param password
     * @param mailHost
     * @param debugSession
     */
    public static void init(final String username, final String password,
                            final String mailHost, final boolean debugSession) {
        init(username, password, null, mailHost, debugSession);
    }

    /**
     * Gets mailSender.
     * 
     * @return mailSender
     * @throws MessagingException  
     */
    protected static MailSender getInstance()
            throws MessagingException {
        if (mailSender == null) {
            throw new MessagingException("MailSender is not init...");

        }

        return mailSender;
    }

    /**
     * create java.mail.Message.
     * 
     * @param message 
     * @return java.mail.Message
     * @throws MessagingException
     */
    public javax.mail.Message createMessage(final MailService.Message message)
            throws MessagingException {

        /*
         * Properties used to construct a email.
         * sending connection protocal
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
        mimeMessage.setContent(message.getHtmlBody(), "text/html;charset=UTF-8");
        mimeMessage.addRecipients(RecipientType.TO,
                                  transformRecipients(message.getRecipients()));

        return mimeMessage;
    }

    /**
     * transport recipients to InternetAddress[].
     * 
     * @param recipients
     * @return
     * @throws MessagingException
     */
    private InternetAddress[] transformRecipients(final Set<String> recipients)
            throws MessagingException {
        if (recipients.isEmpty()) {
            throw new MessagingException(
                    "recipient for Mail should not be null");
        }
        InternetAddress[] realRecipients =
                new InternetAddress[recipients.size()];
        int i = 0;
        for (String recipient : recipients) {
            realRecipients[i++] = new InternetAddress(recipient);
        }
        return realRecipients;
    }

    /**
     * Sends email.
     * 
     * @param message 
     * @throws MessagingException
     *             message exception
     */
    public void sendMail(final MailService.Message message)
            throws MessagingException {
        Transport.send(createMessage(message));
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
