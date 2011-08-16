package org.b3log.latke.mail.local;

import java.io.IOException;

import java.util.logging.Logger;

import org.b3log.latke.mail.MailService;
import org.b3log.latke.service.ServiceException;

/**
 * Production implementation of the {@link MailService} interface,
 * 
 * @author <a href="mailto:jiangzezhou1989@gmail.com">zezhou jiang</a>
 * 
 */
public class LocalMailService implements MailService {

	@Override
	public void send(Message message) throws IOException {
		try {
			new Thread(MailSender.getInstance(message)).start();
		} catch (final ServiceException se) {
			Logger.getLogger(LocalMailService.class.getName()).severe(
					se.getMessage());
		}

	}

}