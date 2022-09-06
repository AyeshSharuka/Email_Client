package Assignment1.EmailClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;


public class MailReceiver implements Runnable {
	
	private MyBlockingQueue queue;
	private static final String email_id = "ylclient19@gmail.com";
    private static final String password = "Client@123";
    private Properties properties;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
	private Observable[] observers;
	
	public MailReceiver(MyBlockingQueue queue, Observable[] observers) {
		this.queue = queue;
		this.observers = observers;
		properties = new Properties();
		
		properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.host", "imap.gmail.com");
        properties.put("mail.imaps.port", "993");
	}
	

	@Override
	public void run() {
		while (true) {
			try {
				Session session = Session.getDefaultInstance(properties, null);

				Store store = session.getStore("imaps");

				store.connect(email_id, password);

				Folder inbox = store.getFolder("inbox");
				inbox.open(Folder.READ_WRITE);

				if (inbox.getUnreadMessageCount() > 0) {
					for (Observable observer : observers) {
						observer.notify(dtf.format(LocalDateTime.now()));
					}

					javax.mail.Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

					for (javax.mail.Message message : messages) {
						Email mail = null;
						try {
							mail = new Email(message.getFrom()[0].toString(), "ylclient19@gmail.com",
									message.getSubject(), message.getContent().toString());
						} catch (IOException e) {
							// TODO Auto-generated catch block
						}
						
						queue.addToQueue(mail);
						message.setFlags(new Flags(Flags.Flag.SEEN), true);

					}

				}
			} catch (MessagingException e) {
			} 
		}
	}
	
	

}
