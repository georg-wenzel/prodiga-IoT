package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
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
@Component
@Scope("application")
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
    @PreAuthorize("hasAuthority('ADMIN')")
    public Collection<BookingType> getAllBookingTypes()
    {
        return Lists.newArrayList(bookingTypeRepository.findAll());
    }

    /**
     * Returns a collection of all booking types with the isActive flag set.
     * @return A collection of all active booking types.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Collection<BookingType> getAllActiveBookingTypes()
    {
        return Lists.newArrayList(bookingTypeRepository.findAllActiveCategories());
    }


    /**
     * Returns the current active booking type for the specific dice side
     * @param side The side to find the booking type for.
     * @return The active booking type for this side.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public BookingType getActiveBookingForSide(int side)
    {
        return bookingTypeRepository.findActiveCategoryForSide(side);
    }

    /**
     * Saves a booking type. If an active booking type for this dice side already exists, overwrite the existing booking type's active flag.
     * @param bookingType The booking type to save.
     * @return The booking type after storing it in the database.
     * @throws ProdigaGeneralExpectedException Is thrown when activity name does not fit the criteria (2-64 characters)
     */
    @PreAuthorize("hasAuthority('ADMIN')")
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
            BookingType db_bt = bookingTypeRepository.findFirstById(bookingType.getId());

            //cant change active to false because this would leave the category without an active label
            if(!bookingType.isActive() && db_bt.isActive())
            {
                throw new ProdigaGeneralExpectedException("Cannot set active flag to false as this would leave the category without a label.", MessageType.ERROR);
            }

            //cant change side because this would introduce inconsistencies
            //maybe allow swapping at some point?
            if(bookingType.getSide() != db_bt.getSide())
            {
                throw new ProdigaGeneralExpectedException("Cannot change dice of a booking type.", MessageType.ERROR);
            }

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

    /**
     * Laods a booking type by ID
     * @param id the ID
     * @return The booking type with this Id
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public BookingType loadBookingType(long id)
    {
        return bookingTypeRepository.findFirstById(id);
    }
}
