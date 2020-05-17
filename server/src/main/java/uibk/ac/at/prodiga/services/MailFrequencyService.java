package uibk.ac.at.prodiga.services;
//for shedule: http://websystique.com/spring/spring-job-scheduling-with-scheduled-enablescheduling-annotations/
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uibk.ac.at.prodiga.model.FrequencyType;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.MailRepository;

@Service
public class MailFrequencyService {


    private final MailRepository mailRepository;
    private final MailService mailService;
    private final ProductivityAnalysisService productivityAnalysisService;
    private final LogInformationService logInformationService;
    private final UserService userService;
    private final BookingService bookingService;

    public MailFrequencyService(MailRepository mailRepository, MailService mailService, ProductivityAnalysisService productivityAnalysisService, LogInformationService logInformationService, UserService userService, BookingService bookingService) {
        this.mailRepository = mailRepository;
        this.mailService = mailService;
        this.productivityAnalysisService = productivityAnalysisService;
        this.logInformationService = logInformationService;
        this.userService = userService;
        this.bookingService = bookingService;
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

            logInformationService.logForCurrentUser("Monthly report send to user " + user.getUsername());
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

            logInformationService.logForCurrentUser("Weekly report send to user " + user.getUsername());
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
            mailService.sendEmailTo(user, "Your daily Prodiga Statistics", "Hello " + user.getFirstName() + " " + user.getLastName() + "!\n\n" + "Your daily productivity statistic can be found in the appendix.\nPlease download the appendix folder and unzip it before you open the html file.\n\n" + "Best Regards,\nThe Prodiga System Managers", FrequencyType.DAILY);

            logInformationService.logForCurrentUser("Daily report send to user " + user.getUsername());
        }
    }

    /**
     * sends every weekday at 12:00pm
     * a notification to all users whose last booking is longer than 2 days ago
     */
    @Scheduled(cron = "0 0 12 * * *")
    public void sendBookingNotification(){
        for(User user : userService.getAllUsersUnauthorized()) {
            if(bookingService.isBookingLongerThan2DaysAgo(user)) {
                mailService.sendNotificationTo(user, "Prodiga Information", "Hello " + user.getFirstName() + " " + user.getLastName() + "!\n\n" + "We want to inform you that your last booking is longer than 2 days ago.\n\n" + "Best Regards,\nThe Prodiga System Managers");
            }
        }
    }



}
