package uibk.ac.at.prodiga.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.LogInformation;
import uibk.ac.at.prodiga.repositories.LogInformationRepository;
import uibk.ac.at.prodiga.utils.ProdigaThreadPool;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.Date;
import java.util.concurrent.Future;

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
     * Logs the given text for the current logged in user
     * @param text The text to log
     * @return A future for awaiting the task
     */
    public Future<Void> logAsync(String text) {
        return ProdigaThreadPool.getInstance().getCachedPool().submit(() -> {
            log(text);
            // Need to return something otherwise java doesn't get the type lol
            return null;
        });
    }

    /**
     * Logs the given text for the current logged in user
     * @param text The text to log
     */
    public void log(String text) {
        LogInformation log = new LogInformation();
        log.setObjectedCreatedDateTime(new Date());
        log.setObjectedUpdatedDateTime(new Date());
        log.setObjectedCreatedUser(prodigaUserLoginManager.getCurrentUser());
        log.setObjectedUpdatedUser(prodigaUserLoginManager.getCurrentUser());
        log.setText(text);

        logInformationRepository.save(log);
    }
}
