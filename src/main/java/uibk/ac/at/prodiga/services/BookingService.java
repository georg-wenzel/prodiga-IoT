package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.repositories.BookingRepository;
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
    private final ProdigaUserLoginManager userLoginManager;

    public BookingService(BookingRepository bookingRepository, ProdigaUserLoginManager userLoginManager)
    {
        this.bookingRepository = bookingRepository;
        this.userLoginManager = userLoginManager;
    }

    /**
     * Returns a collection of all bookings for a dice.
     * @return A collection of all bookings for the given dice.
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public Collection<Booking> getAllBookingsByDice(Dice dice)
    {
        if(!userLoginManager.getCurrentUser().getDice().equals(dice)) throw new RuntimeException("Illegal attempt to load dice data from other user.");
        return Lists.newArrayList(bookingRepository.findAllByDice(dice));
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
        return Lists.newArrayList(bookingRepository.findAllByDepartment(department));
    }

    /**
     * Saves or updates a booking.
     * @param booking The booking to save.
     * @return The booking after storing it in the database.
     * @throws ProdigaGeneralExpectedException Is thrown when users are trying to modify the bookings of others, or modify old bookings without appropriate permissions.
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public Booking saveBooking(Booking booking) throws ProdigaGeneralExpectedException
    {
        User u = userLoginManager.getCurrentUser();

        //check fields
        if(booking.getActivityEndDate().before(booking.getActivityStartDate()))
        {
            throw new ProdigaGeneralExpectedException("Activity cannot start before ending.", MessageType.ERROR);
        }
        if(booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime() > 1000 * 60 * 8)
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
