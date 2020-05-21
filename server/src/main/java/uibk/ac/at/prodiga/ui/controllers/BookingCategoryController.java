package uibk.ac.at.prodiga.ui.controllers;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.services.BookingCategoryService;
import uibk.ac.at.prodiga.services.BookingService;
import uibk.ac.at.prodiga.utils.Constants;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.io.Serializable;
import java.util.*;

/**
 * Controller for global (administrative) configuration of possible booking categories
 */
@Component
@Scope("view")
public class BookingCategoryController implements Serializable
{
    private static final long serialVersionUID = 5325687687692577565L;

    private final BookingCategoryService bookingCategoryService;
    private final BookingService bookingService;
    private final ProdigaUserLoginManager userLoginManager;

    private String newCategoryName;
    private String editCategoryName;
    private long editCategoryId;
    private BookingCategory deleteCategory;
    private Collection<BookingCategory> bookingCategories;
    private boolean isEditing = false;
    private Map<BookingCategory, Integer> usedInBookings = new HashMap<BookingCategory, Integer>();

    public BookingCategoryController(BookingCategoryService bookingCategoryService, ProdigaUserLoginManager userLoginManager, BookingService bookingService)
    {
        this.bookingService = bookingService;
        this.bookingCategoryService = bookingCategoryService;
        this.userLoginManager = userLoginManager;
    }

    /**
     * Saves a new booking category with the name given in newCategoryName
     * @throws ProdigaGeneralExpectedException Thrown if saving the category produces an internal error
     */
    public void saveNewCategory() throws ProdigaGeneralExpectedException
    {
        BookingCategory cat = new BookingCategory();
        cat.setName(newCategoryName);
        bookingCategoryService.save(cat);
        newCategoryName = "";
    }

    /**
     * deletes the category marked for deletion
     * @throws ProdigaGeneralExpectedException Thrown if deleting the category produces an internal error, e.g. category is still in use.
     */
    public void doDeleteCategory() throws ProdigaGeneralExpectedException
    {
        bookingCategoryService.delete(deleteCategory);
    }

    /**
     * Sets the editing flag and the editing ID, as well as the default name
     * @param categoryId The ID of the category to be edited
     */
    public void editCategory(long categoryId)
    {
        this.isEditing = true;
        this.editCategoryId = categoryId;
        this.editCategoryName = bookingCategoryService.findById(categoryId).getName();
    }

    /**
     * Saves currently selected booking under currently typed name
     * @throws ProdigaGeneralExpectedException Thrown if saving the category produces an internal error
     */
    public void saveEditedCategory() throws ProdigaGeneralExpectedException
    {
        this.isEditing = false;
        BookingCategory cat = bookingCategoryService.findById(editCategoryId);
        cat.setName(editCategoryName);
        bookingCategoryService.save(cat);

        //force category update
        bookingCategories = null;
    }

    public Collection<BookingCategory> getAllBookingCategories()
    {
        if(bookingCategories == null) bookingCategories = bookingCategoryService.findAllCategories();
        return bookingCategories;
    }

    public String getNewCategoryName()
    {
        return newCategoryName;
    }

    public void setNewCategoryName(String newCategoryName)
    {
        this.newCategoryName = newCategoryName;
    }

    public String getEditCategoryName() {
        return editCategoryName;
    }

    public void setEditCategoryName(String editCategoryName) {
        this.editCategoryName = editCategoryName;
    }

    public long getEditCategoryId() {
        return editCategoryId;
    }

    public void setEditCategoryId(long editCategoryId) {
        this.editCategoryId = editCategoryId;
    }

    public boolean getIsEditing() {
        return isEditing;
    }

    public void setIsEditing(boolean editing) {
        isEditing = editing;
    }

    public BookingCategory getDeleteCategory() {
        return deleteCategory;
    }

    /**
     * Returns the ID of the category which may not be changed or removed.
     * @return The ID of the category which may not be changed or removed.
     */
    public long getMandatoryCategory()
    {
        return Constants.DO_NOT_BOOK_BOOKING_CATEGORY_ID;
    }

    public void setDeleteCategory(BookingCategory deleteCategory) {
        this.deleteCategory = deleteCategory;
    }

    public int getUsedInBookings(BookingCategory category)
    {
        if(!usedInBookings.containsKey(category)) usedInBookings.put(category, bookingService.getNumberOfBookingsWithCategory(category));
        return usedInBookings.get(category);
    }
}
