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
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import org.b3log.latke.mail.MailService;
import org.b3log.latke.util.PropertyReader;

/**
 * Email sender.
 * 
 * @author <a href="mailto:jiangzezhou1989@gmail.com">zezhou jiang</a>
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 0.0.0.2, Aug 20, 2011
 */
public final class MailSender {
    
    /**
     * Mail property.
     */
    private final Properties mailProperty;
    
    /**
     * Protected constructor.
     * Get mail properties of the sender
     * <p>mail properties example</p>
     * <pre>
     *     <code>mail.username=username</code>
     *     <code>mail.password=password</code>
     *     <code>mail.smtp.host=smtp.xxx.com</code>
     *     <code>mail.smtp.auth=true</code>
     *     <code>mail.debug=false</code>
     *  </pre>
     *  mail properties should be in the directory WEB-INF/classes/mail.properties
     */
    protected MailSender() {
        mailProperty = PropertyReader.getProperties("mail.properties");
        
    }

   /**
    * Create session based on the mail properties.
    * 
    * @return session session from mail properties
    */
    private Session getSession() {
        final Properties props = new Properties();
        props.setProperty("mail.smtp.host", getHost());
        props.setProperty("mail.smtp.auth", getAuth());
        final Session session = Session.getDefaultInstance(props
                , new SMTPAuthenticator());
        session.setDebug(getDebug());
        return session;
    }
    
    /**
     * Get session debug from mail properties.
     * 
     * @return session debug
     */
    private boolean getDebug() {
        String debugStr = mailProperty.getProperty("mail.debug");
        if (debugStr == null) {
            return false;
        }
        debugStr = debugStr.trim();
        if (debugStr.equals("")) {
            return false;
        }
        final boolean debug = Boolean.valueOf(debugStr);
        
        return debug;
       
    }
    
    /**
     * Get mail smtp host form mail properties.
     * 
     * @return mail host
     */
    private String getHost() {
        String host = "";
        try {
            host =  mailProperty.getProperty("mail.smtp.host").trim();
        } catch (final NullPointerException ex) {
            Logger.getLogger(MailSender.class.getName())
            .severe(ex.getMessage());
        }
        
        return host;
    }
    
    /**
     * Get mail smtp auth from mail properties.
     * 
     * @return mail smtp auth
     */
    private String getAuth() {
        String auth = "";
        try {
            auth =  mailProperty.getProperty("mail.smtp.auth").trim();
        } catch (final NullPointerException ex) {
            Logger.getLogger(MailSender.class.getName())
            .severe(ex.getMessage());
        }
                
        return auth;
    }
    
    /**
     * Get mail username form mail properties.
     * 
     * @return mail username
     */
    private String getUsername() {
        String username = "";
        try {
            username =  mailProperty.getProperty("mail.username").trim();
        } catch (final NullPointerException ex) {
            Logger.getLogger(MailSender.class.getName())
            .severe(ex.getMessage());
        }
        return username;
    }
    
    /**
     * Gets mail password from mail properties.
     * 
     * @return mail password
     */
    private String getPassword() {
        String password = "";
        try {
            password = mailProperty.getProperty("mail.password").trim();
        } catch (final NullPointerException ex) {
            Logger.getLogger(MailSender.class.getName())
            .severe(ex.getMessage());
        }
        return password;
    }

    /**
     * Create java.mail.Message with the specified message.
     * 
     * @param message  the specified message
     * @return java.mail.Message mimeMessage
     * @throws MessagingException messageException from javax.mail
     */
    public javax.mail.Message createMessage(final MailService.Message message)
            throws MessagingException {
        if (message == null) {
            throw new MessagingException("message should not be null");
        }
        final MimeMessage mimeMessage = new MimeMessage(getSession());
        mimeMessage.setFrom(new InternetAddress(message.getFrom()));
        final String subject = message.getSubject();                   
        mimeMessage.setSubject(subject != null
                ? subject : ""
               );
        final String htmlBody = message.getHtmlBody();
        mimeMessage.setContent(htmlBody != null
                ? htmlBody :""
                , "text/html;charset=UTF-8");
        mimeMessage.addRecipients(RecipientType.TO,
                                  transformRecipients(message.getRecipients()));

        return mimeMessage;
    }

    /**
     * Transport recipients to InternetAddress array.
     * 
     * @param recipients the set of all recipients
     * @return  InternetAddress array of all recipients internetAddress
     * @throws MessagingException messagingException from javax.mail
     */
    private InternetAddress[] transformRecipients(final Set<String> recipients)
            throws MessagingException {
        if (recipients.isEmpty()) {
            throw new MessagingException(
                    "recipient for Mail should not be null");
        }
        final InternetAddress[] realRecipients =
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
     * @param message  the specified message
     * @throws MessagingException message exception
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
            return new PasswordAuthentication(MailSender.this.getUsername(),
                    MailSender.this.getPassword());
        }
    }
}
