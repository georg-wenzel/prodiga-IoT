package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Booking;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.services.BookingCategoryService;
import uibk.ac.at.prodiga.services.BookingService;
import uibk.ac.at.prodiga.services.DiceService;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;
import uibk.ac.at.prodiga.utils.SnackbarHelper;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

@Component
@Scope("view")
public class BookingController implements Serializable
{
    private Booking booking;
    private User user;
    private Collection<Booking> userBookings;

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
     * Returns whether or not a booking is editable
     * @return true if the last booking of the user is longer than 2 days ago
     */
    public Boolean getLastBookingLongerThan2DaysAgo() {
        return bookingService.isBookingLongerThan2DaysAgo(user);
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

    /**
     * Saves the current booking stored in the controller
     * @throws ProdigaGeneralExpectedException If saving the booking causes an exception (propagated by the service)
     */
    public void doSaveBooking() throws ProdigaGeneralExpectedException
    {
        //set fields if not already present
        if(this.booking.getUser() == null)
            this.booking.setUser(user);
        if(this.booking.getTeam() == null)
            this.booking.setTeam(user.getAssignedTeam());
        if(this.booking.getDept() == null)
            this.booking.setDept(user.getAssignedDepartment());

        this.booking = bookingService.saveBooking(this.booking);
        SnackbarHelper.getInstance().showSnackBar("Booking saved successfully!", MessageType.INFO);
    }

    /**
     * Deletes given booking
     * @param booking The booking to delete
     * @throws ProdigaGeneralExpectedException If deleting the booking causes an exception (propagated by the service)
     */
    public void deleteBooking(Booking booking) throws ProdigaGeneralExpectedException
    {
        bookingService.deleteBooking(booking, false);
        userBookings = null;
    }

    //////GETTERS & SETTERS
    public User getUser()
    {
        return user;
    }

    public Dice getDice()
    {
        if(user == null) return null;
        return diceService.getDiceByUser(user);
    }

    public Collection<Booking> getUserBookings()
    {
        if(userBookings == null) userBookings = bookingService.getAllBookingsByUser(user);
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

    public Date getBookingMaxDate()
    {
        return new Date();
    }

    public boolean getEditing()
    {
        return booking != null && !booking.isNew();
    }

    public void setBookingCategory(Long id)
    {
        if(this.booking == null) this.booking = new Booking();
        booking.setBookingCategory(bookingCategoryService.findById(id));
    }

    public Long getBookingCategory()
    {
        if(this.booking == null) this.booking = new Booking();
        if(booking.getBookingCategory() == null) return (long) 0;
        return booking.getBookingCategory().getId();
    }
}
