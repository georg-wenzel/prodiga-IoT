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
import uibk.ac.at.prodiga.services.*;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.ui.controllers.BookingController;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.Date;

/**
 * Tests proper implementation of the Booking Controller
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class BookingControllerTest
{
    @Autowired
    RaspberryPiRepository raspberryPiRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DiceRepository diceRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    BookingCategoryRepository bookingCategoryRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    ProdigaUserLoginManager prodigaUserLoginManager;

    @Autowired
    BookingService bookingService;

    @Autowired
    UserService userService;

    @Autowired
    BookingCategoryService bookingCategoryService;

    @Autowired
    DiceService diceService;

    @Autowired
    VacationService vacationService;

    @Autowired
    LogInformationService logInformationService;

    User admin;
    User employee;
    Dice employeeDice;
    BookingCategory testCategory;
    BookingController controller;

    @BeforeEach
    public void init_each()
    {
        admin = DataHelper.createAdminUser("admin", userRepository);
        employee = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        employeeDice = DataHelper.createDice("testdice1", null, admin, employee, diceRepository, raspberryPiRepository, roomRepository);
        testCategory = DataHelper.createBookingCategory("testcat1", admin, bookingCategoryRepository);
        controller = new BookingController(prodigaUserLoginManager, bookingService, bookingCategoryService,diceService);
    }

    /**
     * Tests if controller properly returns whether or not the last booking is longer than 2 days ago
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void booking_longer_than_2_days()
    {
        DataHelper.createBooking(testCategory, new Date(new Date().getTime() - 1000*60*60*24*4), new Date(new Date().getTime() - 1000*60*60*24*4 + 1000*60*30), employee, employeeDice, bookingRepository);
        Assertions.assertTrue(controller.getLastBookingLongerThan2DaysAgo(), "Last booking was not shown as being longer than 2 days ago, but was.");
        DataHelper.createBooking(testCategory, new Date(new Date().getTime() - 1000*60*60), new Date(new Date().getTime() - 1000*60*30), employee, employeeDice, bookingRepository);
        Assertions.assertFalse(controller.getLastBookingLongerThan2DaysAgo(), "Last booking was shown to be longer than 2 days ago, but was not.");
    }

    /**
     * Tests if controller properly returns whether or not a booking is editable
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void booking_is_editable()
    {
        Booking b1 = DataHelper.createBooking(testCategory, new Date(new Date().getTime() - 1000*60*60*24*16), new Date(new Date().getTime() - 1000*60*60*24*16 + 1000*60*30), employee, employeeDice, bookingRepository);
        Assertions.assertFalse(controller.isBookingEditable(b1), "Booking that was longer than 2 weeks ago was shown to be editable.");

        employee.setMayEditHistoricData(true);
        employee = userRepository.save(employee);
        //get new controller, to make sure flag is updated within
        BookingController controller = new BookingController(new ProdigaUserLoginManager(userService), bookingService, bookingCategoryService,diceService);
        Assertions.assertTrue(controller.isBookingEditable(b1), "Booking that was longer than 2 weeks ago was shown to not be editable despite user having permissions.");

        employee.setMayEditHistoricData(false);
        employee = userRepository.save(employee);
        b1.setActivityStartDate(new Date(new Date().getTime() - 1000*60*60));
        b1.setActivityEndDate(new Date(new Date().getTime() - 1000*60*30));
        //get new controller, to make sure flag is updated within
        controller = new BookingController(new ProdigaUserLoginManager(userService), bookingService, bookingCategoryService,diceService);
        Assertions.assertTrue(controller.isBookingEditable(b1), "Booking that was not longer than 2 weeks ago was not shown to be editable.");
    }

    /**
     * Tests if hours and minutes are correctly split from a bookings duration
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void hour_minute_split()
    {
        Booking b1 = DataHelper.createBooking(testCategory, new Date(new Date().getTime() - 1000*60*60*24*16), new Date(new Date().getTime() - 1000*60*60*24*16 + 1000*60*348), employee, employeeDice, bookingRepository);
        Assertions.assertEquals(5, controller.getFullHours(b1), "Hours of 348 minute long booking should be 5.");
        Assertions.assertEquals(48, controller.getRemainingMinutes(b1), "Minutes of 348 minute long booking should be 48.");
    }


    /**
     * Tests deleting a booking (only base case, permissions and edge cases have to be covered by the service and view
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void delete_booking() throws ProdigaGeneralExpectedException
    {
        Booking b1 = DataHelper.createBooking(testCategory, new Date(new Date().getTime() - 1000*60*60*24), new Date(new Date().getTime() - 1000*60*60*24 + 1000*60*30), employee, employeeDice, bookingRepository);
        Assertions.assertEquals(b1, bookingRepository.findFirstById(b1.getId()), "Could not properly set up test: Booking was not properly created.");
        controller.deleteBooking(b1);
        Assertions.assertNull(bookingRepository.findFirstById(b1.getId()), "Booking was not properly deleted.");
    }

    /**
     * Tests saving a booking, and whether or not fields are set automatically.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void save_booking() throws ProdigaGeneralExpectedException
    {
        //assign user to a department and team
        Department d = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team t = DataHelper.createRandomTeam(d, admin, teamRepository);
        employee.setAssignedDepartment(d);
        employee.setAssignedTeam(t);
        userRepository.save(employee);

        //refresh necessary services
        ProdigaUserLoginManager newManager = new ProdigaUserLoginManager(userService);
        controller = new BookingController(newManager, new BookingService(bookingRepository, newManager, diceRepository, vacationService, logInformationService), bookingCategoryService, diceService);

        Booking b1 = new Booking();
        b1.setActivityStartDate(new Date(new Date().getTime() - 1000*60*60*24));
        b1.setActivityEndDate(new Date(new Date().getTime() - 1000*60*60*24 + 1000*60*30));
        b1.setBookingCategory(testCategory);

        controller.setBooking(b1);
        controller.doSaveBooking();

        Booking db_booking = bookingRepository.findFirstById(controller.getBooking().getId());

        Assertions.assertEquals(b1.getActivityEndDate(), db_booking.getActivityEndDate(), "End date of activity does not match.");
        Assertions.assertEquals(b1.getActivityStartDate(), db_booking.getActivityStartDate(), "Start date of activity does not match.");
        Assertions.assertEquals(b1.getBookingCategory(), db_booking.getBookingCategory(), "Category of activity does not match.");
        Assertions.assertEquals(t, db_booking.getTeam(),"Team was not properly set automatically.");
        Assertions.assertEquals(d, db_booking.getDept(), "Department was not properly set automatically.");
        Assertions.assertEquals(employeeDice, db_booking.getDice(), "Dice was not properly set automatically.");

    }
}
