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
package org.b3log.latke.mail;

import java.io.IOException;


import org.b3log.latke.mail.MailService.Message;
import org.b3log.latke.mail.local.LocalMailService;
import org.b3log.latke.mail.local.MailSender;

public class JavaMailTestCase {

    public static void main(final String[] args) throws IOException {
        MailSender.init("b3log_team", "b3logteam", "utf-8", "smtp.163.com", true);
        //MailSender.init("b3log_team", "b3logteam", "ISO_8859_1", "smtp.163.com");
        //MailSender.init("b3log_team", "b3logteam", "smtp.163.com");
        //MailSender.init("b3log_team", "b3logteam", "smtp.163.com", true);

        //Latkes.initRuntimeEnv();
        //MailService mailService = MailServiceFactory.getMailService();

        MailService mailService = new LocalMailService();
        final Message message = new Message();
        message.setFrom("b3log_team@163.com");
        message.setSubject("又一次测试,iso_8859_1");
        message.setHtmlBody(
                "<html><body><font color='red'>沮丧</font></body></html>");
        message.addRecipient("jiangzezhou1989@163.com");
        message.addRecipient("jiangzezhou1989@yahoo.com.cn");
        mailService.send(message);
        mailService.send(message);

    }
}