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
    Booking findFirstByDiceOrderByActivityEndDateDesc(Dice d);

    /**
     * Gets all bookings of a given user in a given timespan, specifically any overlapping between start/end-date and the booking
     * @param user The user to check
     * @param beginDate The timespan beginDate
     * @param endDate The timespan endDate
     * @return All matching bookings
     */
    @Query("Select b FROM Booking b WHERE " +
            "b.dice.user = :user AND " +
            "(" +
            //activity is fully covered by timespan
            "(b.activityStartDate >= :beginDate AND b.activityEndDate <= :endDate) OR " +
            //timespan is fully covered by activity
            "(:beginDate >= b.activityStartDate AND :endDate <= b.activityEndDate) OR " +
            //timespan starts somewhere in the activity
            "(:beginDate >= b.activityStartDate AND :beginDate <= b.activityEndDate) OR " +
            //timespan ends somewhere in the activity
            "(:endDate >= b.activityStartDate AND :endDate <= b.activityEndDate) OR " +
            //activity begins somewhere in the timespan
            "(b.activityStartDate >= :beginDate AND b.activityStartDate <= :endDate) OR " +
            //activity ends somewhere in the timespan
            "(b.activityEndDate >= :beginDate AND b.activityEndDate <= :endDate)" +
            ")")
    Collection<Booking> findUsersBookingInRange(@Param("user") User user, @Param("beginDate") Date beginDate, @Param("endDate") Date endDate);

    @Query("Select b FROM Booking b WHERE " +
            "b.bookingCategory = :category AND b.activityStartDate > :beginDate AND b.activityEndDate < :endDate")
    Collection<Booking> findBookingWithCategoryInRange(@Param("category") BookingCategory category, @Param("beginDate") Date beginDate, @Param("endDate") Date endDate);

    @Query("Select b FROM Booking b WHERE " +
            "b.dice.user = :user AND b.bookingCategory = :category AND b.activityStartDate > :beginDate AND b.activityEndDate < :endDate")
    Collection<Booking> findUsersBookingWithCategoryInRange(@Param("user") User user, @Param("category") BookingCategory category, @Param("beginDate") Date beginDate, @Param("endDate") Date endDate);
}