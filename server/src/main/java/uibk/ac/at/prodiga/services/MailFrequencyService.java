package uibk.ac.at.prodiga.services;
//for cron: https://code.tutsplus.com/tutorials/scheduling-tasks-with-cron-jobs--net-8800
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uibk.ac.at.prodiga.model.FrequencyType;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.MailRepository;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

@Service
public class MailFrequencyService {

    @Autowired
    private final MailRepository mailRepoitory;
    @Autowired
    private final MailService mailService;

    public MailFrequencyService(MailRepository mailRepoitory, MailService mailService) {
        this.mailRepoitory = mailRepoitory;
        this.mailService = mailService;
    }

    //every first day of the month 12:00pm
    @Scheduled(cron = "0 12 1 * *")
    public void sendMonthlyNotification(){
        for(User user : mailRepoitory.findUserByFrequencyType(FrequencyType.MONTHLY)){
            mailService.sendEmailTo(user, "Your monthly Prodiga Statistics", "coming soon");
        }
    }

    //every monday 12:00pm
    @Scheduled(cron = "0 12 * * 1")
    public void sendWeeklyNotification(){
        for(User user : mailRepoitory.findUserByFrequencyType(FrequencyType.WEEKLY)) {
            mailService.sendEmailTo(user, "Your weekly Prodiga Statistics", "coming soon");
        }
    }

    //every day 12:00pm
    @Scheduled(cron = "0 12 * * *")
    public void sendDailyNotification(){
        for(User user : mailRepoitory.findUserByFrequencyType(FrequencyType.DAILY)) {
            mailService.sendEmailTo(user, "Your daily Prodiga Statistics", "coming soon");
        }
    }



}
