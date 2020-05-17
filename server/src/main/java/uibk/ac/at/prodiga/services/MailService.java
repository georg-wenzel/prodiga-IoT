package uibk.ac.at.prodiga.services;
//https://www.baeldung.com/spring-email
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailParseException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import uibk.ac.at.prodiga.model.BadgeDB;
import uibk.ac.at.prodiga.model.FrequencyType;
import uibk.ac.at.prodiga.model.User;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.Objects;

@Service
public class MailService {

    private final BadgeDBService badgesDBService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private LogInformationService logInformationService;

    public MailService(BadgeDBService badgesDBService) {
        this.badgesDBService = badgesDBService;
    }

    public void sendMail(String toAddress, String subject, String text, User user, FrequencyType frequencyType) {

        MimeMessage message = mailSender.createMimeMessage();

        try{
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(text);

            FileSystemResource file = new FileSystemResource("src/main/java/uibk/ac/at/prodiga/utils/charts/"+user.getUsername()+"-"+frequencyType.getLabel().toLowerCase()+".html");
            helper.addAttachment(Objects.requireNonNull(file.getFilename()), file);

            Collection<BadgeDB> badges = badgesDBService.getLastWeeksBadgesByUser(user);

            for(BadgeDB badgeDB : badges){
                FileSystemResource f = new FileSystemResource("src/main/webapp/resources/ecuador-layout/images/"+badgeDB.getBadgeName()+".png");
                helper.addAttachment(Objects.requireNonNull(f.getFilename()), f);
            }

        }catch (MessagingException e) {
            throw new MailParseException(e);
        }
        mailSender.send(message);

        logInformationService.logForCurrentUser("Mail sent to " + toAddress);
    }

    public void sendMailWithoutStatistic(String toAddress, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toAddress);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    /**
     * sends an email to a given user iff the notifications are enabled and if
     * the user has a valid email adress
     * @param user user who should receive the email
     * @param subject subject of the email
     * @param text text of the email
     */
    public void sendEmailTo(User user, String subject, String text, FrequencyType frequencyType) {
        if(user.getNotificationsEnabled()) {
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                sendMail(user.getEmail(), subject, text, user, frequencyType);
            }
        }
    }

    public void sendNotificationTo(User user, String subject, String text) {
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            sendMailWithoutStatistic(user.getEmail(), subject, text);
        }
    }

}
