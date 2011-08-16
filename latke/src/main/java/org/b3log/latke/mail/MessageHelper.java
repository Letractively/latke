package org.b3log.latke.mail;

/**
 * Message htlper.
 * Helper class for populating a {@link MailService.Message}.
 * @author <a href="mailto:jiangzezhou1989@gmail.com">zezhou jiang</a>
 *
 */
public class MessageHelper {
	
	private  MailService.Message message;
	
	/**
	 * public constructor 
	 */
	public MessageHelper(){
		message = new MailService.Message();
	}
	
	/**
	 * 
	 * @return Mail message
	 */
	public MailService.Message getMessage(){
		return message;
	} 
	
	/**
	 * 
	 * @param From 
	 */
	public void setFrom(String From){
		message.setFrom(From);
	}
	
	/**
	 * 
	 * @param subject
	 */
	public void setSubjcet(String subject){
		message.setSubject(subject);
	}
	
	/**
	 * 
	 * @param htmlBody
	 */
	public void setHtmlBody(String htmlBody){
		message.setHtmlBody(htmlBody);
	}
	
	/**
	 * 
	 * @param htmlBodyBuffer
	 */
	public void setHtmlBody(StringBuffer htmlBodyBuffer){
		message.setHtmlBody(htmlBodyBuffer.toString());
	}
	
	/**
	 * 
	 * @param recipient
	 */
	public void addRecipient(String recipient){
		message.addRecipient(recipient);
	}
}
