package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Booking;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.services.BookingCategoryService;
import uibk.ac.at.prodiga.services.BookingService;
import uibk.ac.at.prodiga.services.DiceService;
import uibk.ac.at.prodiga.services.UserService;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.io.Serializable;
import java.util.Collection;

@Component
@Scope("view")
public class BookingController implements Serializable
{
    private Booking booking;
    private User user;
    private Collection<Booking> userBookings;
    private boolean updateTeam;

    private BookingService bookingService;
    private BookingCategoryService bookingCategoryService;
    private DiceService diceService;

    public BookingController(ProdigaUserLoginManager userLoginManager, BookingService bookingService, BookingCategoryService bookingCategoryService, DiceService diceService)
    {
        this.user = userLoginManager.getCurrentUser();
        this.bookingService = bookingService;
        this.bookingCategoryService = bookingCategoryService;
        this.diceService = diceService;
    }

    /**
     * Returns whether or not user may edit a booking
     * @param booking The booking to check
     * @return Returns true when the booking is not before the previous week or the user has permissions to edit data before the previous week, otherwise false
     */
    public boolean isBookingEditable(Booking booking)
    {
        return user.getMayEditHistoricData() || !bookingService.isEarlierThanLastWeek(booking.getActivityStartDate());
    }

    /**
     * Returns integer number of full hours this activity takes, e.g. an activity that takes 5 hours 48 minutes would return 5
     * @param booking The booking to check the activity time for
     * @return Number of full hours for this activity
     */
    public int getFullHours(Booking booking)
    {
        return (int) Math.floorDiv(booking.getActivityEndDate().toInstant().toEpochMilli() - booking.getActivityStartDate().toInstant().toEpochMilli(), 1000 * 60 * 60);
    }

    /**
     * Returns integer number of remaining minutes this activity takes after subtracting full hours, e.g. an activity that takes 5 hours 48 minutes would return 48
     * @param booking The booking to check the activity time for
     * @return Number of remaining hours for this activity
     */
    public int getRemainingMinutes(Booking booking)
    {
        return (int) (Math.floorDiv(booking.getActivityEndDate().toInstant().toEpochMilli() - booking.getActivityStartDate().toInstant().toEpochMilli(), 1000 * 60) - getFullHours(booking) * 60);
    }

    public void saveBooking()
    {

    }

    public void deleteBooking(Booking booking) throws ProdigaGeneralExpectedException
    {
        bookingService.deleteBooking(booking);
    }

    public User getUser()
    {
        return user;
    }

    public Collection<Booking> getUserBookings()
    {
        if(userBookings == null) userBookings = bookingService.getAllBookingsByDice(diceService.getDiceByUser(user));
        return userBookings;
    }

    public void setBookingById(Long id)
    {
        this.booking = bookingService.loadBooking(id);
        if(booking == null)
            this.booking = new Booking();
    }

    public Collection<BookingCategory> getAvailableCategories()
    {
        if(user.getAssignedTeam() != null)
            return bookingCategoryService.findAllCategoriesByTeam();

        return bookingCategoryService.findAllCategories();
    }

    public Long getBookingById()
    {
        if(this.booking == null)
            this.booking = new Booking();

        return this.booking.getId();
    }

    public Booking getBooking() {
        if(this.booking == null)
            this.booking = new Booking();

        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public boolean getEditing()
    {
        return booking != null && !booking.isNew();
    }
}
