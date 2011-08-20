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



/**
 * 
 * @author <a href="mailto:jiangzezhou1989@gmail.com">zezhou jiang</a>
 * @version 1.0.0.0, Aug 16, 2011
 */
public final class JavaMailTestCase {

    /**
     * Test localMailService.
     * 
     * @param args string array
     * @throws IOException IOException from java.io
     */
    public static void main(final String[] args) throws IOException {
        final Message message = new Message();
        message.setFrom("jiangzezhou1989@163.com");
        message.setSubject("dd");
        message.setHtmlBody("<htmL><body>测试</body><html>");
        message.addRecipient("jiangzezhou1989@yahoo.com.cn");
        new LocalMailService().send(message);
    }

    
    /**
     * Private default constructor.
     */
    private JavaMailTestCase() {
    }
}