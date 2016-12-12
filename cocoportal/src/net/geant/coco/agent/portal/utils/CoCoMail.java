package net.geant.coco.agent.portal.utils;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import net.geant.coco.agent.portal.dao.User;

// This is a helper class to send emails via google mail, over smtp
// NOTE: the account to be used needs to enable "less secure Apps"
// NOTE: coco.communityconnection@gmail.com is configured correctly already
public class CoCoMail {

	// TODO put into properties file
	private static String MAILADRESS = "coco.communityconnection@gmail.com";
	private static String USER = "coco.communityconnection@gmail.com";
	private static String PASSWORD = "cocorules!";
	
	private static String EMAIL_TITLE = "CoCo VPN request from [sender]";
	private static String EMAIL_TEXT = "Dear [receiver], \n\r\n\r"
			+ "This is a VPN request from [sender]. \n\r\n\r"
			+ "[Personal_Text]"
			+ "Please follow the following link to participate in the VPN.\n\r"
			+ "[link]\n\r\n\r"
			+ "Best regards,\n\r"
			+ "CoCo";
	
	// String text = EMAIL_TEXT.replace('[sender]', '[sender]')
	// text = text.replace('[receiver]', '[sender]')
	// text = text.replace('[Personal_Text]', '') // add \n\r\n\r at the end if set
	// text = text.replace('[link]', '[link]')
	
	static public boolean sendCoCoMail(User sender, User receiver, String text, String link){
		String mail_title = EMAIL_TITLE.replace("[sender]", sender.getName());
		String mail_text = EMAIL_TEXT.replace("[sender]", sender.getName());
		mail_text = mail_text.replace("[receiver]", receiver.getName());
		
		if ( text != null &&  !text.equals("")){
			mail_text = mail_text.replace("[Personal_Text]", text + "\n\r\n\r"); 
		} else {
			mail_text = mail_text.replace("[Personal_Text]", "");  
		}
		mail_text = mail_text.replace("[link]", link);  
		
		return CoCoMail.sendMail(receiver.getEmail(), mail_title, mail_text);
	}
	
	
	static public boolean sendMail(String recipient, String mail_subject, String mail_message){
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");

		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(CoCoMail.USER, CoCoMail.PASSWORD);
				}
			});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(CoCoMail.MAILADRESS));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(recipient));
			
			message.setSubject(mail_subject);
			message.setText(mail_message);
			
			/*message.setSubject("Testing Subject");
			message.setText("Dear Mail Crawler," +
					"\n\n No spam to my email, please!");*/

			Transport.send(message);

			//System.out.println("Done");

		} catch (MessagingException e) {
			return false;
			//throw new RuntimeException(e);
		}

	  return true;
	}
	
}
