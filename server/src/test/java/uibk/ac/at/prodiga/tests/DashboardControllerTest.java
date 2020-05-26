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
import uibk.ac.at.prodiga.ui.controllers.DashboardController;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;
import java.util.*;

/**
 * Tests proper implementation of the Dashboard Controller
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class DashboardControllerTest {
    @Autowired
    BadgeDBService badgeDBService;

    @Autowired
    ProdigaUserLoginManager prodigaUserLoginManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BadgeDBRepository badgeDBRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    BookingCategoryRepository bookingCategoryRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    RaspberryPiRepository raspberryPiRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    RaspberryPiService raspberryPiService;

    @Autowired
    UserService userService;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    TeamService teamService;

    @Autowired
    BookingService bookingService;

    @Autowired
    ProdigaUserLoginManager userLoginManager;

    User admin;
    User employee;

    DashboardController controller;

    @BeforeEach
    public void init_each()
    {
        admin = DataHelper.createAdminUser("admin", userRepository);
        employee = DataHelper.createUserWithRoles("dashboard_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        controller = new DashboardController(departmentService, raspberryPiService, userService, teamService, bookingService, userLoginManager, badgeDBService);
    }

    /**
     * Tests if controller properly returns the number of departments.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "test_admin", authorities = {"EMPLOYEE"})
    public void num_departments() {
        Department testDepartment1 = DataHelper.createRandomDepartment(admin, departmentRepository);
        Department testDepartment2 = DataHelper.createRandomDepartment(admin, departmentRepository);
        Department testDepartment3 = DataHelper.createRandomDepartment(admin, departmentRepository);

        int numDepartments = controller.numDepartments();
        Assertions.assertEquals(3, numDepartments, "There should be exactly 3 departments.");
    }

    /**
     * Tests if controller properly returns the number of raspberry pis.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "test_admin", authorities = {"EMPLOYEE"})
    public void num_raspberry_pis() {
        Room testRoom1 = DataHelper.createRoom("testRoom1", admin, roomRepository);
        RaspberryPi testRaspberryPi1 = DataHelper.createRaspi("test1", admin, testRoom1, raspberryPiRepository, roomRepository);
        RaspberryPi testRaspberryPi2 = DataHelper.createRaspi("test2", admin, testRoom1, raspberryPiRepository, roomRepository);
        RaspberryPi testRaspberryPi3 = DataHelper.createRaspi("test3", admin, testRoom1, raspberryPiRepository, roomRepository);

        int numRaspis = controller.numRaspberryPis();
        Assertions.assertEquals(3, numRaspis, "There should be exactly 3 raspberry pis.");
    }

    /**
     * Tests if controller properly returns the number of users.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "test_admin", authorities = {"EMPLOYEE"})
    public void num_users() {
        User testUser1 = DataHelper.createRandomUser(userRepository);
        int numUsers = controller.numUsers();
        Assertions.assertEquals(3, numUsers, "There should be exactly 3 raspberry pis.");
    }

    /**
     * Tests if controller properly returns the number of teams.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "test_admin", authorities = {"EMPLOYEE"})
    public void num_teams() {
        Department testDepartment1 = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team testTeam1 = DataHelper.createRandomTeam(testDepartment1, admin, teamRepository);
        Team testTeam2 = DataHelper.createRandomTeam(testDepartment1, admin, teamRepository);
        Team testTeam3 = DataHelper.createRandomTeam(testDepartment1, admin, teamRepository);
        int numTeams = controller.numTeams();
        Assertions.assertEquals(3, numTeams, "There should be exactly 3 raspberry pis.");
    }

    /**
     * Tests if controller properly returns the number of last week's badges.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "test_admin", authorities = {"EMPLOYEE"})
    public void num_badges_last_week() {
        User user = DataHelper.createUserWithRoles("test_admin", Sets.newSet(UserRole.ADMIN), userRepository);
        BadgeDB testBadge1 = DataHelper.createRandomBadgeLastWeek(user, badgeDBRepository);
        BadgeDB testBadge2 = DataHelper.createRandomBadgeLastWeek(user, badgeDBRepository);
        BadgeDB testBadge3 = DataHelper.createRandomBadgeLastWeek(user, badgeDBRepository);
        int numBadges = controller.numBadgesLastWeek();
        Assertions.assertEquals(3, numBadges, "There should be exactly 3 badges.");
    }

    /**
     * Tests if controller properly returns the total number of badges
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "test_admin", authorities = {"EMPLOYEE"})
    public void num_badges_total() {
        User user = DataHelper.createUserWithRoles("test_admin", Sets.newSet(UserRole.ADMIN), userRepository);
        BadgeDB testBadge1 = DataHelper.createRandomBadgeLastWeek(user, badgeDBRepository);
        BadgeDB testBadge2 = DataHelper.createRandomBadgeLastWeek(user, badgeDBRepository);
        BadgeDB testBadge3 = DataHelper.createRandomBadge(user, badgeDBRepository);
        int numBadges = controller.numBadgesTotal();
        Assertions.assertEquals(3, numBadges, "There should be exactly 3 badges.");
    }

    /**
     * Tests if controller properly returns the total number of working hours.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "test_admin", authorities = {"EMPLOYEE"})
    public void working_hours_range() {
        User user = DataHelper.createUserWithRoles("test_admin", Sets.newSet(UserRole.ADMIN), userRepository);
        BookingCategory testBookingCategory = DataHelper.createBookingCategory("test", admin, bookingCategoryRepository);
        Booking testBooking1 = DataHelper.createBooking(testBookingCategory, new Date(System.currentTimeMillis() - 3600 * 1000), new Date(), admin, bookingRepository);
        Booking testBooking2 = DataHelper.createBooking(testBookingCategory, new Date(System.currentTimeMillis() - 3600 * 1000), new Date(), admin, bookingRepository);
        Booking testBooking3 = DataHelper.createBooking(testBookingCategory, new Date(System.currentTimeMillis() - 3600 * 1000), new Date(), admin, bookingRepository);

        Collection<Booking> bookings = new ArrayList<>();
        bookings.add(testBooking1);
        bookings.add(testBooking2);
        bookings.add(testBooking3);

        long workingHours = controller.workingHoursInRange(bookings);
        Assertions.assertEquals(3, workingHours, "There should be exactly 3 working hours");
    }

    /**
     * Tests if controller properly returns the total number of working hours for this week.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "test_admin", authorities = {"EMPLOYEE"})
    public void working_hours_range_this_week() {
        User user = DataHelper.createUserWithRoles("test_admin", Sets.newSet(UserRole.ADMIN), userRepository);
        BookingCategory testBookingCategory = DataHelper.createBookingCategory("test", admin, bookingCategoryRepository);
        Booking testBooking1 = DataHelper.createBooking(testBookingCategory, new Date(System.currentTimeMillis() - 3600 * 1000), new Date(), user, bookingRepository);
        Booking testBooking2 = DataHelper.createBooking(testBookingCategory, new Date(System.currentTimeMillis() - 3600 * 1000), new Date(), user, bookingRepository);
        Booking testBooking3 = DataHelper.createBooking(testBookingCategory, new Date(System.currentTimeMillis() - 3600 * 1000), new Date(), user, bookingRepository);

        long workingHours = controller.workingHoursThisWeek();
        Assertions.assertEquals(3, workingHours, "There should be exactly 3 working hours for this week");
    }

    /**
     * Tests if controller properly returns the total number of working hours for last week.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "test_admin", authorities = {"EMPLOYEE"})
    public void working_hours_range_last_week() {
        User user = DataHelper.createUserWithRoles("test_admin", Sets.newSet(UserRole.ADMIN), userRepository);
        BookingCategory testBookingCategory = DataHelper.createBookingCategory("test", admin, bookingCategoryRepository);
        Booking testBooking1 = DataHelper.createBooking(testBookingCategory, DataHelper.lastWeekBeginning(), DataHelper.lastWeekEnd(), user, bookingRepository);
        Booking testBooking2 = DataHelper.createBooking(testBookingCategory, DataHelper.lastWeekBeginning(), DataHelper.lastWeekEnd(), user, bookingRepository);
        Booking testBooking3 = DataHelper.createBooking(testBookingCategory, DataHelper.lastWeekBeginning(), DataHelper.lastWeekEnd(), user, bookingRepository);

        long workingHours = controller.workingHoursLastWeek();
        Assertions.assertEquals(503, workingHours, "There should be exactly 3 working hours for last week");
    }


}
