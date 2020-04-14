package uibk.ac.at.prodiga.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.Vacation;

import java.util.Collection;
import java.util.Date;

/**
 * DB Repository for managing Vacations
 */
public interface VacationRepository extends AbstractRepository<Vacation, Long>
{
    //Magic methods
    Vacation findFirstById(Long id);
    Collection<Vacation> findAllByUser(User user);

    /**
     * Gets all vacations of a given user in a given year
     * @param user The user to get the vacations of
     * @param year The year to get the vacations from
     * @return A collection of vacations for this user  that either start or end in the given year
     */
    @Query("SELECT v FROM Vacation v WHERE v.user = :user AND (year(v.beginDate) = :year OR year(v.endDate) = :year)")
    Collection<Vacation> findUsersYearlyVacations(@Param("user") User user, @Param("year") int year);

    /**
     * Gets all vacations of a given user in a given timespan, specifically:
     *  - Any vacation where beginDate = v.beginDate
     *  - Any vacation where endDate = v.beginDate
     *  - Any vacation where beginDate = v.endDate
     *  - Any vacation where endDate = v.endDate
     *  - Any vacation where beginDate is between the vacation beginning and end
     *  - Any vacation where endDate is between the vacation beginning and end
     *  - Any vacation where v.beginDate is between beginDate and endDate
     *  - Any vacation where v.endDate is between beginDate and endDate
     * @param user The user to check
     * @param beginDate The timespan beginDate
     * @param endDate The timespan endDate
     * @return All matching vacations
     */
    @Query("Select v FROM Vacation v WHERE " +
            "v.user = :user AND (" +
            "v.beginDate = :beginDate OR " +
            "v.endDate = :beginDate OR " +
            "v.beginDate = :endDate OR " +
            "v.endDate = :endDate OR " +
            "(:beginDate < v.endDate AND :beginDate > v.beginDate) OR " +
            "(:endDate < v.endDate AND :endDate > v.beginDate) OR " +
            "(v.beginDate < :endDate AND v.beginDate > :beginDate) OR " +
            "(v.endDate < :endDate AND v.endDate > :beginDate)" +
            ")")
    Collection<Vacation> findUsersVacationInRange(@Param ("user") User user, @Param("beginDate") Date beginDate, @Param("endDate") Date endDate);

}
