package uibk.ac.at.prodiga.ui.controllers;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.services.BookingCategoryService;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.Collection;
import java.util.List;

/**
 * Controller for global (administrative) configuration of possible booking categories
 */
@Component
@Scope("view")
public class BookingCategoryController
{
    private final BookingCategoryService bookingCategoryService;
    private final ProdigaUserLoginManager userLoginManager;

    private String newCategoryName;

    public BookingCategoryController(BookingCategoryService bookingCategoryService, ProdigaUserLoginManager userLoginManager)
    {
        this.bookingCategoryService = bookingCategoryService;
        this.userLoginManager = userLoginManager;
    }

    public Collection<BookingCategory> getAllBookingCategories()
    {
        return bookingCategoryService.findAllCategories();
    }

    public String getNewCategoryName()
    {
        return newCategoryName;
    }

    public void setNewCategoryName(String newCategoryName)
    {
        this.newCategoryName = newCategoryName;
    }

    public void saveNewCategory()
    {

    }

    public void deleteCategory()
    {

    }
}
