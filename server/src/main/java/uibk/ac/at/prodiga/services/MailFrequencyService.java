package uibk.ac.at.prodiga.services;
//for shedule: http://websystique.com/spring/spring-job-scheduling-with-scheduled-enablescheduling-annotations/
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uibk.ac.at.prodiga.model.FrequencyType;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.MailRepository;

@Service
public class MailFrequencyService {

    @Autowired
    private final MailRepository mailRepository;
    @Autowired
    private final MailService mailService;
    @Autowired
    private final ProductivityAnalysisService productivityAnalysisService;

    public MailFrequencyService(MailRepository mailRepository, MailService mailService, ProductivityAnalysisService productivityAnalysisService) {
        this.mailRepository = mailRepository;
        this.mailService = mailService;
        this.productivityAnalysisService = productivityAnalysisService;
    }

    /**
     * sends every first day of the month at 12:00pm
     * a notification to all users who have the frequency type set to monthly
     */
    @Scheduled(cron = "0 0 12 1 * ?")
    public void sendMonthlyNotification(){
        for(User user : mailRepository.findUserByFrequencyType(FrequencyType.MONTHLY)){
            productivityAnalysisService.createJSON(FrequencyType.MONTHLY, user);
            mailService.sendEmailTo(user, "Your monthly Prodiga Statistics", "Hello " + user.getFirstName() + " " + user.getLastName() + "!\n\n" + "Your monthly productivity statistic can be found in the appendix.\n\n" + "Best Regards\nThe Prodiga System Managers", FrequencyType.MONTHLY);
        }
    }

    /**
     * sends every monday at 12:00pm
     * a notification to all users who have the frequency type set to monthly
     */
    @Scheduled(cron = "0 0 12 * * MON")
    public void sendWeeklyNotification(){
        for(User user : mailRepository.findUserByFrequencyType(FrequencyType.WEEKLY)) {
            productivityAnalysisService.createJSON(FrequencyType.WEEKLY, user);
            mailService.sendEmailTo(user, "Your weekly Prodiga Statistics", "Hello " + user.getFirstName() + " " + user.getLastName() + "!\n\n" + "Your weekly productivity statistic can be found in the appendix.\n\n" + "Best Regards\nThe Prodiga System Managers", FrequencyType.WEEKLY);
        }
    }

    /**
     * sends every weekday at 12:00pm
     * a notification to all users who have the frequency type set to monthly
     */
    @Scheduled(cron = "0 0 12 * * MON-FRI")
    public void sendDailyNotification(){
        for(User user : mailRepository.findUserByFrequencyType(FrequencyType.DAILY)) {
            productivityAnalysisService.createJSON(FrequencyType.DAILY, user);
            mailService.sendEmailTo(user, "Your daily Prodiga Statistics", "Hello " + user.getFirstName() + " " + user.getLastName() + "!\n\n" + "Your daily productivity statistic can be found in the appendix.\n\n" + "Best Regards,\nThe Prodiga System Managers", FrequencyType.DAILY);
        }
    }



}
