package uibk.ac.at.prodiga.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.BookingCategoryRepository;
import uibk.ac.at.prodiga.repositories.BookingRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.services.BookingCategoryService;
import uibk.ac.at.prodiga.services.BookingService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.ui.controllers.BookingCategoryController;
import uibk.ac.at.prodiga.utils.Constants;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.Collection;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class BookingCategoryControllerTest
{
    @Autowired
    UserRepository userRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    BookingCategoryRepository bookingCategoryRepository;

    @Autowired
    BookingCategoryService bookingCategoryService;

    @Autowired
    ProdigaUserLoginManager userLoginManager;

    @Autowired
    BookingService bookingService;

    User admin;
    BookingCategoryController controller;
    BookingCategory cat1, cat2;

    @BeforeEach
    public void init_each()
    {
        admin = DataHelper.createAdminUser("admin", userRepository);

        //create 2 booking categories
        cat1 = DataHelper.createBookingCategory("testcat1", admin, bookingCategoryRepository);
        cat2 = DataHelper.createBookingCategory("testcat2", admin, bookingCategoryRepository);

        controller = new BookingCategoryController(bookingCategoryService, bookingService);
    }

    /**
     * tests getting booking categories and saving them
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void save_and_get_categories() throws ProdigaGeneralExpectedException
    {
        Collection<BookingCategory> categories = controller.getAllBookingCategories();

        Assertions.assertTrue(categories.contains(cat1), "Category cat1 not found within booking category collection.");
        Assertions.assertTrue(categories.contains(cat2), "Category cat2 not found within booking category collection.");

        controller.setNewCategoryName("testcat3");
        controller.saveNewCategory();

        Assertions.assertEquals("", controller.getNewCategoryName(), "New category name field was not reset.");
        Assertions.assertTrue(controller.getAllBookingCategories().stream().anyMatch(x -> x.getName().equals("testcat3")), "New category was not found in booking category collection.");
    }

    /**
     * tests deleting booking categories
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void delete_categories() throws ProdigaGeneralExpectedException
    {
        controller.setDeleteCategory(cat1);
        controller.doDeleteCategory();

        Assertions.assertFalse(controller.getAllBookingCategories().contains(cat1), "Booking category is still in listing of categories.");
        Assertions.assertFalse(bookingCategoryRepository.findById(cat1.getId()).isPresent(), "Booking category is still present in the database.");
    }

    /**
     * tests editing booking categories
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"EMPLOYEE", "ADMIN"})
    public void edit_categories() throws ProdigaGeneralExpectedException
    {
        if(cat1.getId().equals(Constants.DO_NOT_BOOK_BOOKING_CATEGORY_ID)) cat1 = DataHelper.createBookingCategory("testcat4", admin, bookingCategoryRepository);
        controller.editCategory(cat1.getId());
        controller.setEditCategoryName("testcat3");
        controller.saveEditedCategory();

        cat1 = bookingCategoryRepository.findById(cat1.getId()).orElse(null);

        Assertions.assertNotNull(cat1, "Could not find booking category in DB after editing.");
        Assertions.assertEquals("testcat3", cat1.getName(), "Name was not updated properly in DB.");
        Assertions.assertEquals("testcat3", controller.getAllBookingCategories().stream().filter(x -> x.getId().equals(cat1.getId())).findFirst().get().getName(), "Name was not updated properly in booking category list.");
    }

    /**
     * tests amount of bookings per category
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"EMPLOYEE", "ADMIN"})
    public void get_used_in_bookings() throws ProdigaGeneralExpectedException
    {
        //make 3 bookings with booking category 1
        for(int i=0;i<3;i++)
        {
            DataHelper.createBooking(cat1, admin, bookingRepository);
        }

        Assertions.assertEquals(3, controller.getUsedInBookings(cat1), "Amount that booking category is used in bookings is not properly reflected.");
        Assertions.assertEquals(0, controller.getUsedInBookings(cat2), "Amount that booking category is used in bookings is not properly reflected.");
    }
}
