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
import org.b3log.latke.Latkes;
import org.b3log.latke.mail.MailService.Message;
import org.testng.annotations.Test;

/**
 * 
 * @author <a href="mailto:jiangzezhou1989@gmail.com">zezhou jiang</a>
 * @version 1.0.0.1, Aug 21, 2011
 */
public final class MailServiceTestCase {

    static {
        Latkes.initRuntimeEnv();
    }

    /**
     * Tests mail sending.
     * 
     * @throws IOException if error
     * @throws InterruptedException s 
     */
    @Test
    public void testSendMail() throws IOException, InterruptedException {
        System.out.println("testSendMail");
        final MailService mailService =
                MailServiceFactory.getMailService();

        final Message message = new Message();
        message.setFrom("b3log.solo@163.com");
        message.setSubject("dd");
        message.setHtmlBody("<htmL><body>测试</body><html>");
        message.addRecipient("jiangzezhou1989@yahoo.com.cn");
        message.addRecipient("DL88250@gmail.com");

        mailService.send(message);
    }
}