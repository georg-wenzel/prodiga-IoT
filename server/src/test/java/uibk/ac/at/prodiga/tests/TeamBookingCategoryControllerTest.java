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
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.repositories.*;
import uibk.ac.at.prodiga.services.BookingCategoryService;
import uibk.ac.at.prodiga.services.BookingService;
import uibk.ac.at.prodiga.services.DiceService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.ui.controllers.BookingCategoryController;
import uibk.ac.at.prodiga.ui.controllers.TeamBookingCategoryController;
import uibk.ac.at.prodiga.utils.Constants;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class TeamBookingCategoryControllerTest
{
    @Autowired
    UserRepository userRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    TeamRepository teamRepository;

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

    @Autowired
    DiceService diceService;

    User admin;
    User teamleader;
    Team team;
    TeamBookingCategoryController controller;
    BookingCategory usedCat1, usedCat2;
    BookingCategory unusedCat1, unusedCat2;

    @BeforeEach
    public void init_each()
    {
        admin = DataHelper.createAdminUser("admin", userRepository);

        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        team = DataHelper.createRandomTeam(dept,admin,teamRepository);

        teamleader = DataHelper.createUserWithRoles("teamleader", Sets.newSet(UserRole.EMPLOYEE, UserRole.TEAMLEADER), admin, dept, team, userRepository);

        //create 2 booking categories
        usedCat1 = DataHelper.createBookingCategory("testcat1", admin, Sets.newSet(team), bookingCategoryRepository);
        usedCat2 = DataHelper.createBookingCategory("testcat2", admin, Sets.newSet(team), bookingCategoryRepository);
        unusedCat1 = DataHelper.createBookingCategory("testcat3", admin, bookingCategoryRepository);
        unusedCat2 = DataHelper.createBookingCategory("testcat4", admin, bookingCategoryRepository);

        //Create 4 bookings with usedCat1 from teamleader
        for(int i=0;i<4;i++)
        {
            DataHelper.createBooking(usedCat1, teamleader, bookingRepository);
        }
        //and 4 for admin
        for(int i=0;i<4;i++)
        {
            DataHelper.createBooking(usedCat1, admin, bookingRepository);
        }

        controller = new TeamBookingCategoryController(userLoginManager, bookingCategoryService, bookingService, diceService);
    }

    /**
     * tests if category is correctly classified as used by team or not
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "teamleader", authorities = {"EMPLOYEE", "TEAMLEADER"})
    public void correctly_classifies_category()
    {
        Assertions.assertTrue(controller.getCurrentlyUsedByTeam(usedCat1), "Category 1 incorrectly classified as not used by team.");
        Assertions.assertTrue(controller.getCurrentlyUsedByTeam(usedCat2), "Category 2 incorrectly classified as not used by team.");
        Assertions.assertFalse(controller.getCurrentlyUsedByTeam(unusedCat1), "Category 3 incorrectly classified as used by team.");
        Assertions.assertFalse(controller.getCurrentlyUsedByTeam(unusedCat2), "Category 4 incorrectly classified as used by team.");
    }

    /**
     * tests if the team is correctly inferred from the logged in user
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "teamleader", authorities = {"EMPLOYEE", "TEAMLEADER"})
    public void get_team_returns_correct()
    {
        Assertions.assertEquals(team, controller.getTeam());
    }

    /**
     * tests if global categories are correctly loaded
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "teamleader", authorities = {"EMPLOYEE", "TEAMLEADER"})
    public void get_global_categories()
    {
        Assertions.assertEquals(controller.getCategories(), bookingCategoryRepository.findAll());
    }


    /**
     * tests if category can correctly be removed from or added to team
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "teamleader", authorities = {"EMPLOYEE", "TEAMLEADER"})
    public void switch_category_classification() throws ProdigaGeneralExpectedException
    {
        if(usedCat1.getId().equals(Constants.DO_NOT_BOOK_BOOKING_CATEGORY_ID)) usedCat1 = DataHelper.createBookingCategory("testcat1_alt", admin, Sets.newSet(team), bookingCategoryRepository);

        //make sure values are loaded that are automatically loaded on view startup normally
        controller.getTeam();
        controller.getTeamHasCategories();

        controller.categoryChanged(usedCat1);
        controller.categoryChanged(unusedCat1);

        Assertions.assertFalse(controller.getCurrentlyUsedByTeam(usedCat1), "Category 1 incorrectly classified as used by team in controller.");
        Assertions.assertTrue(controller.getCurrentlyUsedByTeam(unusedCat1), "Category 3 incorrectly classified as not used by team in controller.");
    }

    /**
     * tests if booking count for team for category is properly returned
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "teamleader", authorities = {"EMPLOYEE", "TEAMLEADER"})
    public void get_booking_count_team() throws ProdigaGeneralExpectedException
    {
        Assertions.assertEquals(4, controller.getUsedInBookingsByTeam(usedCat1), "Booking amount was not properly returned for category 1.");
        Assertions.assertEquals(0, controller.getUsedInBookingsByTeam(usedCat2), "Booking amount was not properly returned for category 2.");
        Assertions.assertEquals(0, controller.getUsedInBookingsByTeam(unusedCat1), "Booking amount was not properly returned for category 3.");
        Assertions.assertEquals(0, controller.getUsedInBookingsByTeam(unusedCat2), "Booking amount was not properly returned for category 4.");
    }
}
