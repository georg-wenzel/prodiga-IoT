package uibk.ac.at.prodiga.services;
//for cron: https://code.tutsplus.com/tutorials/scheduling-tasks-with-cron-jobs--net-8800
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MailIntervalService {

    private MailService mailService;

    //every first day of the month 12:00pm
    @Scheduled(cron = "0 12 1 * *")
    public void sendMonthlyNotification(){
        //mailService.sendEmailTo(user, "Your monthly Prodiga Statistics", "coming soon");
    }
    //every monday 12:00pm
    @Scheduled(cron = "0 12 * * 1")
    public void sendWeeklyNotification(){
        //mailService.sendEmailTo(user, "Your monthly Prodiga Statistics", "coming soon")

    }

    //every day 12:00pm
    @Scheduled(cron = "0 12 * * *")
    public void sendDailyNotification(){
        //mailService.sendEmailTo(user, "Your monthly Prodiga Statistics", "coming soon")

    }
}
