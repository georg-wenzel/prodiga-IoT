package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.repositories.BookingRepository;
import uibk.ac.at.prodiga.repositories.DiceRepository;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Service for accessing and manipulating bookings.
 */
@Component
@Scope("application")
public class BookingService
{
    private final BookingRepository bookingRepository;
    private final DiceRepository diceRepository;
    private final ProdigaUserLoginManager userLoginManager;

    public BookingService(BookingRepository bookingRepository, ProdigaUserLoginManager userLoginManager, DiceRepository diceRepository)
    {
        this.bookingRepository = bookingRepository;
        this.userLoginManager = userLoginManager;
        this.diceRepository = diceRepository;
    }

    /**
     * Returns a collection of all bookings for a dice.
     * @return A collection of all bookings for the given dice.
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public Collection<Booking> getAllBookingsByDice(Dice dice)
    {
        if(!diceRepository.findFirstByUser(userLoginManager.getCurrentUser()).equals(dice)) throw new RuntimeException("Illegal attempt to load dice data from other user.");
        return Lists.newArrayList(bookingRepository.findAllByDice(dice));
    }

    /**
     * Returns the last booking for the given dice
     * @param d The dice
     * @return
     */
    public Booking getLastBookingForDice(Dice d) {
        return bookingRepository.findFirstByDiceOrderByObjectCreatedDateTimeDesc(d);
    }

    /**
     * Returns a collection of all bookings for a team.
     * @return A collection of all bookings for the given team.
     */
    @PreAuthorize("hasAuthority('TEAMLEADER')")
    public Collection<Booking> getAllBookingsByTeam(Team team)
    {
        if(!userLoginManager.getCurrentUser().getAssignedTeam().equals(team)) throw new RuntimeException("Illegal attempt to load dice data from other team.");
        return Lists.newArrayList(bookingRepository.findAllByTeam(team));
    }

    /**
     * Returns a collection of all bookings for a department.
     * @return A collection of all bookings for the given department.
     */
    @PreAuthorize("hasAuthority('DEPARTMENTLEADER')")
    public Collection<Booking> getAllBookingsByDepartment(Department department)
    {
        if(!userLoginManager.getCurrentUser().getAssignedDepartment().equals(department)) throw new RuntimeException("Illegal attempt to load dice data from other department.");
        return Lists.newArrayList(bookingRepository.findAllByDept(department));
    }

    /**
     * Returns the number of bookings using a certain category
     * @param cat The booking category
     * @return the number (int) of bookings using this category
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public int getNumberOfBookingsWithCategory(BookingCategory cat)
    {
        return bookingRepository.findAllByBookingCategory(cat).size();
    }

    @PreAuthorize("hasAuthority('TEAMLEADER')")
    public int getNumberOfTeamBookingsWithCategory(BookingCategory cat)
    {
        return bookingRepository.findAllByBookingCategoryAndTeam(cat, userLoginManager.getCurrentUser().getAssignedTeam()).size();
    }

    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public Booking saveBooking(Booking booking) throws ProdigaGeneralExpectedException {
        return saveBooking(booking, userLoginManager.getCurrentUser());
    }

    /**
     * Saves or updates a booking.
     * @param booking The booking to save.
     * @return The booking after storing it in the database.
     * @throws ProdigaGeneralExpectedException Is thrown when users are trying to modify the bookings of others, or modify old bookings without appropriate permissions.
     */
    public Booking saveBooking(Booking booking, User u) throws ProdigaGeneralExpectedException
    {
        //check fields
        if(booking.getActivityEndDate().before(booking.getActivityStartDate()))
        {
            throw new ProdigaGeneralExpectedException("Activity cannot start before ending.", MessageType.ERROR);
        }
        if(booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime() > 1000 * 60 * 60 * 8)
        {
            throw new ProdigaGeneralExpectedException("Activity may not last longer than 8 hours at once.", MessageType.ERROR);
        }
        if(booking.getActivityEndDate().after(new Date()))
        {
            throw new ProdigaGeneralExpectedException("Cannot set activity data for the future.", MessageType.ERROR);
        }
        if(!booking.getDice().getUser().equals(u))
        {
            throw new RuntimeException("User may only modify his own activities.");
        }
        //if activity start date is before the previous week, check historic data flag
        if(isEarlierThanLastWeek(booking.getActivityStartDate()) && !u.mayEditHistoricData())
        {
            throw new ProdigaGeneralExpectedException("User is not allowed to edit data from before the previous week.", MessageType.ERROR);
        };

        //set appropriate fields
        if(booking.isNew())
        {
            //set dept and team to users current dept and team (only on creation)
            booking.setDept(u.getAssignedDepartment());
            booking.setTeam(u.getAssignedTeam());

            booking.setObjectCreatedDateTime(new Date());
            booking.setObjectCreatedUser(userLoginManager.getCurrentUser());
        }
        else
        {
            Booking db_booking = bookingRepository.findFirstById(booking.getId());
            //If the database activity started in the week before the previous one, user must have appropriate permissions to change it.
            if(isEarlierThanLastWeek(db_booking.getActivityStartDate()) && !u.mayEditHistoricData())
            {
                throw new ProdigaGeneralExpectedException("User is not allowed to edit data from before the previous week.", MessageType.ERROR);
            };
            if(!db_booking.getDice().equals(booking.getDice()))
            {
                throw new RuntimeException("The dice of an activity may not be changed, as it is tied to the user.");
            }

            booking.setObjectChangedDateTime(new Date());
            booking.setObjectChangedUser(userLoginManager.getCurrentUser());
        }

        return bookingRepository.save(booking);
    }

    /**
     * Laods a booking by ID
     * @param id the ID
     * @return The booking with this Id
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public Booking loadBooking(long id)
    {
        Booking b = bookingRepository.findFirstById(id);
        if(b.getDice().getUser().equals(userLoginManager.getCurrentUser())) return b;
        throw new RuntimeException("Illegal attempt to load booking from other user.");
    }

    /**
     * Deletes a booking
     * @param booking The booking to delete
     * @throws ProdigaGeneralExpectedException Thrown if the user is trying to delete a booking from longer than 2 weeks ago, but does not have permissions.
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public void deleteBooking(Booking booking) throws ProdigaGeneralExpectedException
    {
        User u = userLoginManager.getCurrentUser();

        booking = bookingRepository.findFirstById(booking.getId());
        if(booking == null) return;

        if(!booking.getDice().getUser().equals(u))
        {
            throw new RuntimeException("User cannot delete other user's bookings.");
        }

        if(isEarlierThanLastWeek(booking.getActivityStartDate()) && !u.mayEditHistoricData())
        {
            throw new ProdigaGeneralExpectedException("User cannot delete bookings from earlier than 2 weeks ago.", MessageType.ERROR);
        }

        bookingRepository.delete(booking);
    }

    /**
     * For a given date, returns if this date is in the same week or previous week as the current date.
     * @param date The date to check
     * @return True if date is in current or last week, false otherwise.
     */
    private boolean isEarlierThanLastWeek(Date date)
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int WoY = calendar.get(Calendar.WEEK_OF_YEAR);
        int yr = calendar.get(Calendar.YEAR);
        calendar.setTime(new Date());
        //check if Date WoY - WoY is 0 or 1
        if(calendar.get(Calendar.WEEK_OF_YEAR) - WoY == 0 || calendar.get(Calendar.WEEK_OF_YEAR) - WoY == 1) return false;
        //check if current week of year is 1 and previous week is max week for that year
        if(calendar.get(Calendar.WEEK_OF_YEAR) == 1)
        {
            calendar.set(yr, Calendar.DECEMBER, 31);
            if(calendar.get(Calendar.WEEK_OF_YEAR) == WoY) return false;
        }
        return true;
    }
}
