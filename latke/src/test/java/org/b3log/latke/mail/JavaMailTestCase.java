package org.b3log.latke.mail;

import java.io.IOException;

import org.b3log.latke.mail.local.LocalMailService;
import org.b3log.latke.mail.local.MailSender;

public class JavaMailTestCase {
	
	public static void main(String[] args) throws IOException {
		
		MailSender.init("b3log_team", "b3logteam", "utf-8", "smtp.163.com", true);
		//MailSender.init("b3log_team", "b3logteam", "ISO_8859_1", "smtp.163.com");
		//MailSender.init("b3log_team", "b3logteam", "smtp.163.com");
		//MailSender.init("b3log_team", "b3logteam", "smtp.163.com", true);
		
		//Latkes.initRuntimeEnv();
		//MailService mailService = MailServiceFactory.getMailService();
		
		MailService mailService = new LocalMailService();
		MessageHelper messageHelper = new MessageHelper();
		messageHelper.setFrom("b3log_team@163.com");
		messageHelper.setSubjcet("又一次测试,iso_8859_1");
		messageHelper.setHtmlBody("<html><body><font color='red'>红色</font></body></html>");
		messageHelper.addRecipient("jiangzezhou1989@163.com");
		messageHelper.addRecipient("jiangzezhou1989@yahoo.com.cn");
	
		mailService.send(messageHelper.getMessage());
		
	}
}
