package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.LogInformation;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.LogInformationRepository;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.Date;
import java.util.List;

@Component
@Scope("application")
public class LogInformationService {

    private final LogInformationRepository logInformationRepository;
    private final ProdigaUserLoginManager prodigaUserLoginManager;

    public LogInformationService(LogInformationRepository logInformationRepository, ProdigaUserLoginManager prodigaUserLoginManager) {
        this.logInformationRepository = logInformationRepository;
        this.prodigaUserLoginManager = prodigaUserLoginManager;
    }

    /**
     * Finds all log entries
     * @return a list with entries
     */
    public List<LogInformation> findAll() {
        return Lists.newArrayList(logInformationRepository.findAll());
    }

    /**
     * Returns all entries for the given user
     * @param user The user
     * @return A list with log entries
     */
    public List<LogInformation> findAllForUser(String user) {
        return Lists.newArrayList(logInformationRepository
                .findAllByInsertUserNameContainingOrderByLogDateDesc(user));
    }

    /**
     * Returns all entries for the given user which are before the given date
     * @param user The user
     * @param date The date
     * @return A list of log entries
     */
    public List<LogInformation> findAllForUserBeforeDate(String user, Date date) {
        return Lists.newArrayList(logInformationRepository
                .findAllByInsertUserNameContainingAndLogDateBeforeOrderByLogDateDesc(user, date));
    }

    /**
     * Returns all entries for the given user which are after the given date
     * @param user The user
     * @param date The date
     * @return A list of log entries
     */
    public List<LogInformation> findAllForUserAfterDate(String user, Date date) {
        return Lists.newArrayList(logInformationRepository
                .findAllByInsertUserNameContainingAndLogDateAfterOrderByLogDateDesc(user, date));
    }

    /**
     * Returns all entries for the given user which are between both given dates
     * @param user The user
     * @param startDate The start date
     * @param endDate The end date
     * @return A list of log entries
     */
    public List<LogInformation> findAllForUserBetweenDates(String user, Date startDate, Date endDate) {
        return Lists.newArrayList(logInformationRepository
                .findAllByInsertUserNameContainingAndLogDateAfterAndLogDateBeforeOrderByLogDateDesc(user, startDate, endDate));
    }

    /**
     * Returns all entries which are before the given date
     * @param date The date
     * @return A list of log entries
     */
    public List<LogInformation> findAllBeforeDate(Date date) {
        return Lists.newArrayList(logInformationRepository
                .findAllByLogDateBeforeOrderByLogDateDesc(date));
    }

    /**
     * Returns all entries which are after the given date
     * @param date The date
     * @return A list of log entries
     */
    public List<LogInformation> findAllAfterDate(Date date) {
        return Lists.newArrayList(logInformationRepository
                .findAllByLogDateAfterOrderByLogDateDesc(date));
    }

    /**
     * Returns all entries which are between the given dates
     * @param startDate The start date
     * @param endDate The end date
     * @return A list of log entries
     */
    public List<LogInformation> findAllBetweenDates(Date startDate, Date endDate) {
        return Lists.newArrayList(logInformationRepository
                .findAllByLogDateAfterAndLogDateBeforeOrderByLogDateDesc(startDate, endDate));
    }

    /**
     * Logs the given string for the current logged in user
     * @param text The log string
     */
    public void logForCurrentUser(String text) {
        String user = null;
        User u = null;
        try {
            u = prodigaUserLoginManager.getCurrentUser();

        } catch (Exception e) {
            // Ignored
            // We can't get a user for e.g. cron jobs any system stuff
        }
        if(u != null) {
            user = u.getUsername();
        } else {
            user = "System";
        }

        log(text, user);
    }

    /**
     * Logs the specific string for the specifc raspi
     * @param text The log string
     * @param raspberryPi The raspi
     */
    public void logForRaspi(String text, RaspberryPi raspberryPi) {
        String user = "RaspberryPi ";
        if(raspberryPi != null) {
            user += raspberryPi.getInternalId();
        } else {
            user += "not defined";
        }

        log(text, user);
    }

    private void log(String text, String user) {
        LogInformation log = new LogInformation();
        log.setInsertUserName(user);
        log.setText(text);
        log.setLogDate(new Date());

        logInformationRepository.save(log);
    }
}
