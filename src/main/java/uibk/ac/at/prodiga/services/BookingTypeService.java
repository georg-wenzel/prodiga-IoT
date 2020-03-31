package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import uibk.ac.at.prodiga.model.BookingType;
import uibk.ac.at.prodiga.repositories.BookingTypeRepository;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

;import java.util.Collection;
import java.util.Date;

/**
 * Service for accessing and manipulating booking types.
 */
public class BookingTypeService
{
    private final BookingTypeRepository bookingTypeRepository;
    private final ProdigaUserLoginManager userLoginManager;

    public BookingTypeService(BookingTypeRepository bookingTypeRepository, ProdigaUserLoginManager userLoginManager)
    {
        this.bookingTypeRepository = bookingTypeRepository;
        this.userLoginManager = userLoginManager;
    }

    /**
     * Returns a collection of all booking types
     * @return A collection of all booking types.
     */
    public Collection<BookingType> getAllBookingTypes()
    {
        return Lists.newArrayList(bookingTypeRepository.findAll());
    }

    /**
     * Returns a collection of all booking types with the isActive flag set.
     * @return A collection of all active booking types.
     */
    public Collection<BookingType> getAllActiveBookingTypes()
    {
        return Lists.newArrayList(bookingTypeRepository.findAllActive());
    }

    /**
     * Returns the current active booking type for the specific dice side
     * @param side The side to find the booking type for.
     * @return The active booking type for this side.
     */
    public BookingType getActiveBookingForSide(int side)
    {
        return bookingTypeRepository.findActiveCategoryForSide(side);
    }

    public BookingType saveBookingType(BookingType bookingType) throws ProdigaGeneralExpectedException
    {
        //check fields
        if(bookingType.getActivityName().length() > 64 || bookingType.getActivityName().length() < 2)
        {
            throw new ProdigaGeneralExpectedException("Activity name must be between 2 and 64 characters.", MessageType.ERROR);
        }

        //set appropriate fields
        if(bookingType.isNew())
        {
            bookingType.setObjectCreatedDateTime(new Date());
            bookingType.setObjectCreatedUser(userLoginManager.getCurrentUser());
        }
        else
        {
            bookingType.setObjectChangedDateTime(new Date());
            bookingType.setObjectChangedUser(userLoginManager.getCurrentUser());
        }

        //if there is already a bookingtype with this side active, and this bookingtype is also active, then overwrite the old bookingtype's state
        if(bookingType.isActive())
        {
            BookingType oldBookingType = bookingTypeRepository.findActiveCategoryForSide(bookingType.getSide());
            if(!oldBookingType.equals(bookingType))
            {
                oldBookingType.setActive(false);
                bookingTypeRepository.save(oldBookingType);
            }
        }

        return bookingTypeRepository.save(bookingType);
    }

    public BookingType loadBookingType(long id)
    {
        return bookingTypeRepository.findFirstById(id);
    }
}
