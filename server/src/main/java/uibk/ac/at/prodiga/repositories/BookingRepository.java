package uibk.ac.at.prodiga.repositories;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uibk.ac.at.prodiga.model.*;

import java.util.Collection;
import java.util.Date;

/**
 * DB Repository for managing Booking Types
 */
public interface BookingRepository extends AbstractRepository<Booking, Long>
{
    //Magic methods
    Booking findFirstById(Long id);
    Collection<Booking> findAllByDice(Dice d);
    Collection<Booking> findAllByTeam(Team team);
    Collection<Booking> findAllByDept(Department department);
    Collection<Booking> findAllByBookingCategory(BookingCategory category);
    Collection<Booking> findAllByBookingCategoryAndTeam(BookingCategory category, Team team);
    Booking findFirstByDiceOrderByObjectCreatedDateTimeDesc(Dice d);

    /**
     * Gets all bookings of a given user in a given timespan, specifically, considering the timespans should be start of days
     *  - Any booking which lies between beginDate and endDate
     * @param user The user to check
     * @param beginDate The timespan beginDate
     * @param endDate The timespan endDate
     * @return All matching bookings
     */
    @Query("Select b FROM Booking b WHERE " +
            "b.dice.user = :user AND b.activityStartDate > :beginDate AND b.activityEndDate < :endDate")
    Collection<Booking> findUsersBookingInRange(@Param("user") User user, @Param("beginDate") Date beginDate, @Param("endDate") Date endDate);

    @Query("Select b FROM Booking b WHERE " +
            "b.bookingCategory = :category AND b.activityStartDate > :beginDate AND b.activityEndDate < :endDate")
    Collection<Booking> findBookingWithCategoryInRange(@Param("category") BookingCategory category, @Param("beginDate") Date beginDate, @Param("endDate") Date endDate);
}