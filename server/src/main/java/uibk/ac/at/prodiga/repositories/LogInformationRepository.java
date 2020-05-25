package uibk.ac.at.prodiga.repositories;

import uibk.ac.at.prodiga.model.LogInformation;

import java.util.Collection;
import java.util.Date;

public interface LogInformationRepository extends AbstractRepository<LogInformation, Long> {

    Collection<LogInformation> findAllByInsertUserNameContainingAndLogDateAfterAndLogDateBeforeOrderByLogDateDesc(String user, Date startDate, Date endDate);
    Collection<LogInformation> findAllByInsertUserNameContainingAndLogDateAfterOrderByLogDateDesc(String user, Date startDate);
    Collection<LogInformation> findAllByInsertUserNameContainingAndLogDateBeforeOrderByLogDateDesc(String user, Date endDate);
    Collection<LogInformation> findAllByLogDateAfterAndLogDateBeforeOrderByLogDateDesc(Date startDate, Date endDate);
    Collection<LogInformation> findAllByLogDateAfterOrderByLogDateDesc(Date startDate);
    Collection<LogInformation> findAllByLogDateBeforeOrderByLogDateDesc(Date endDate);
    Collection<LogInformation> findAllByInsertUserNameContainingOrderByLogDateDesc(String user);

}
