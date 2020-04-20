package uibk.ac.at.prodiga.services;
//https://www.baeldung.com/spring-email
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import uibk.ac.at.prodiga.model.User;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;
    public void sendMail(String toAddress, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toAddress);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    /**
     * sends an email to a given user iff the user has an email adress
     * @param user user who should receive the email
     * @param subject subject of the email
     * @param text text of the email
     */
    public void sendEmailTo(User user, String subject, String text) {
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            sendMail(user.getEmail(), subject, text);
        }
    }

}
