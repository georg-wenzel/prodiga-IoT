package uibk.ac.at.prodiga.tests;

import org.junit.jupiter.api.Assertions;
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
import uibk.ac.at.prodiga.services.BookingService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class BookingServiceTest
{
    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    DiceRepository diceRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    VacationRepository vacationRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    BookingCategoryRepository bookingCategoryRepository;

    @Autowired
    RaspberryPiRepository raspberryPiRepository;

    @Autowired
    BookingService bookingService;

    /**
     * Tests loading of booking by id.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void load_booking_by_id()
    {
        //Data setup
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d = DataHelper.createDice("testdice", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        Booking b = DataHelper.createBooking(cat, u1, d, bookingRepository);

        Booking booking_service = bookingService.loadBooking(b.getId());

        Assertions.assertEquals(b.getActivityStartDate(), booking_service.getActivityStartDate(), "Activity start date was not properly stored in DB.");
        Assertions.assertEquals(b.getActivityEndDate(), booking_service.getActivityEndDate(), "Activity end date was not stored properly in DB.");
        Assertions.assertEquals(d, booking_service.getDice(), "Dice was not properly stored in DB.");
        Assertions.assertEquals(cat, booking_service.getBookingCategory(), "Category was not properly stored in DB.");
        Assertions.assertNull(booking_service.getObjectChangedDateTime(), "Booking changed date time should be null, but is not");
        Assertions.assertNull(booking_service.getObjectChangedUser(), "Booking changed user should be null, but is not");
        Assertions.assertEquals(u1, booking_service.getObjectCreatedUser(), "Creation user of booking type does not match booking_test_user1.");
        Assertions.assertEquals(b.getObjectCreatedDateTime(), booking_service.getObjectCreatedDateTime(), "Creation date not loaded properly for booking.");
    }

    /**
     * Tests loading of booking by dice with lacking authorization
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"ADMIN", "DEPARTMENTLEADER", "TEAMLEADER"})
    public void load_booking_by_id_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingService.loadBooking(0);
        }, "Booking loaded despite lacking authorization of EMPLOYEE");
    }

    /**
     * Tests loading of booking by id when the logged in user does not match the user of the booking
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void load_booking_by_id_from_other_user()
    {
        //Data setup
        User admin = DataHelper.createAdminUser("admin", userRepository);
        DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        User u2 = DataHelper.createUserWithRoles("booking_test_user2", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d = DataHelper.createDice("testdice", null, admin, u2, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        Booking b = DataHelper.createBooking(cat, u2, d, bookingRepository);

        Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.loadBooking(b.getId());
        }, "Booking loaded from different user successfully.");
    }

    /**
     * Tests loading bookings by dice
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void load_booking_by_dice()
    {
        //Data setup
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        User u2 = DataHelper.createUserWithRoles("booking_test_user2", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        Dice d2 = DataHelper.createDice("testdice2", null, admin, u2, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        BookingCategory cat2 = DataHelper.createBookingCategory("test_category_02", admin, bookingCategoryRepository);

        //Create two bookings for booking_test_user1 and one booking for booking_test_user2
        Booking b1 = DataHelper.createBooking(cat, u1, d1, bookingRepository);
        Booking b2 = DataHelper.createBooking(cat2, u1, d1, bookingRepository);
        Booking b3 = DataHelper.createBooking(cat, u2, d2, bookingRepository);

        //Load bookings of user 1
        Collection<Booking> bookings = bookingService.getAllBookingsByDice(d1);
        Assertions.assertTrue(bookings.contains(b1), "Booking 1 was not found in bookings collection.");
        Assertions.assertTrue(bookings.contains(b2), "Booking 2 was not found in bookings collection.");
        Assertions.assertFalse(bookings.contains(b3), "Booking 3 was found in bookings collection of wrong user.");

        //Try loading bookings of user 2 -> should give error
        Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.getAllBookingsByDice(d2);
        }, "Bookings loaded from different user successfully.");
    }

    /**
     * Tests loading bookings by dice with lacking authorization
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "ADMIN"})
    public void load_booking_by_dice_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingService.getAllBookingsByDice(new Dice());
        }, "Booking loaded despite lacking authorization of EMPLOYEE.");
    }

    /**
     * Tests loading bookings by department
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"DEPARTMENTLEADER"})
    public void load_booking_by_dept()
    {
        //Data setup
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Department dept2 = DataHelper.createRandomDepartment(admin, departmentRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.DEPARTMENTLEADER),admin, dept, null, userRepository);
        User u2 = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE), admin, dept, null, userRepository);
        User u3 = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE), admin, dept2, null, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u2, diceRepository, raspberryPiRepository, roomRepository);
        Dice d2 = DataHelper.createDice("testdice2", null, admin, u3, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);

        //Create two bookings for user in department and one booking for user outside of department
        Booking b1 = DataHelper.createBooking(cat, u1, d1, bookingRepository);
        Booking b2 = DataHelper.createBooking(cat, u1, d1, bookingRepository);
        Booking b3 = DataHelper.createBooking(cat, u2, d2, bookingRepository);

        //Load bookings of dept
        Collection<Booking> bookings = bookingService.getAllBookingsByDepartment(dept);
        Assertions.assertTrue(bookings.contains(b1), "Booking 1 was not found in bookings collection.");
        Assertions.assertTrue(bookings.contains(b2), "Booking 2 was not found in bookings collection.");
        Assertions.assertFalse(bookings.contains(b3), "Booking 3 was found in bookings collection of wrong department.");

        //Try loading bookings of dept2 -> should give error
        Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.getAllBookingsByDepartment(dept2);
        }, "Bookings loaded from different department successfully.");
    }

    /**
     * Tests loading bookings by department with lacking authorization
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE", "TEAMLEADER", "ADMIN"})
    public void load_booking_by_dept_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingService.getAllBookingsByDepartment(new Department());
        }, "Bookings of department loaded despite lacking authorization.");
    }

    /**
     * Tests loading bookings by team
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"TEAMLEADER"})
    public void load_booking_by_team()
    {
        //Data setup
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team1 = DataHelper.createRandomTeam(dept, admin, teamRepository);
        Team team2 = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.TEAMLEADER),admin, dept, team1, userRepository);
        User u2 = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE), admin, dept, team1, userRepository);
        User u3 = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE), admin, dept, team2, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u2, diceRepository, raspberryPiRepository, roomRepository);
        Dice d2 = DataHelper.createDice("testdice2", null, admin, u3, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);

        //Create two bookings for user in team and one booking for user outside of team
        Booking b1 = DataHelper.createBooking(cat, u1, d1, bookingRepository);
        Booking b2 = DataHelper.createBooking(cat, u1, d1, bookingRepository);
        Booking b3 = DataHelper.createBooking(cat, u2, d2, bookingRepository);

        //Load bookings of team
        Collection<Booking> bookings = bookingService.getAllBookingsByTeam(team1);
        Assertions.assertTrue(bookings.contains(b1), "Booking 1 was not found in bookings collection.");
        Assertions.assertTrue(bookings.contains(b2), "Booking 2 was not found in bookings collection.");
        Assertions.assertFalse(bookings.contains(b3), "Booking 3 was found in bookings collection of wrong team.");

        //Try loading bookings of dept2 -> should give error
        Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.getAllBookingsByTeam(team2);
        }, "Bookings loaded from different team successfully.");
    }


    /**
     * Tests loading bookings by team
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE", "DEPARTMENTLEADER", "ADMIN"})
    public void load_booking_by_team_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingService.getAllBookingsByTeam(new Team());
        }, "Bookings of team loaded despite lacking authorization.");
    }

    /**
     * Tests adding a new booking
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void save_booking() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);

        Booking b1 = new Booking();
        b1.setDice(d1);
        b1.setBookingCategory(cat);
        //set activity end time to 5 minutes before current time.
        Date endingTime = new Date(new Date().getTime() - 60*1000*5);
        b1.setActivityEndDate(endingTime);
        //set activity start time to 30 minutes ago
        Date startingTime = new Date(new Date().getTime() - 60*1000*30);
        b1.setActivityStartDate(startingTime);
        b1 = bookingService.saveBooking(b1);

        Assertions.assertEquals(startingTime, b1.getActivityStartDate(), "Activity start time was not returned properly.");
        Assertions.assertEquals(endingTime, b1.getActivityEndDate(), "Activity end time was not returned properly.");
        Assertions.assertEquals(team, b1.getTeam(), "Team was not returned properly.");
        Assertions.assertEquals(dept, b1.getDept(), "Department was not returned properly.");
        Assertions.assertEquals(cat, b1.getBookingCategory(), "Booking category was not returned properly.");
        Assertions.assertEquals(d1, b1.getDice(), "Dice was not returned properly.");
    }

    /**
     * Tests adding a new booking where start date is before end date
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void save_booking_time_inverted()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);

        Booking b1 = new Booking();
        b1.setDice(d1);
        b1.setBookingCategory(cat);
        //set activity end time to 30 minutes before current time.
        Date endingTime = new Date(new Date().getTime() - 60*1000*30);
        b1.setActivityEndDate(endingTime);
        //set activity start time to 5 minutes ago
        Date startingTime = new Date(new Date().getTime() - 60*1000*5);
        b1.setActivityStartDate(startingTime);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            bookingService.saveBooking(b1);
        }, "Booking was saved despite activity beginning before ending.");
    }

    /**
     * Tests adding a new booking where start date is before end date
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void save_booking_too_long()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);

        Booking b1 = new Booking();
        b1.setDice(d1);
        b1.setBookingCategory(cat);
        //set activity end time to 5 minutes before current time.
        Date endingTime = new Date(new Date().getTime() - 60*1000*5);
        b1.setActivityEndDate(endingTime);
        //set activity start time to 9 hours before current time
        Date startingTime = new Date(new Date().getTime() - 60*1000*60*9);
        b1.setActivityStartDate(startingTime);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            bookingService.saveBooking(b1);
        }, "Booking was saved despite activity being longer than 8 hours.");
    }

    /**
     * Tests adding a booking for another user
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void save_booking_other_user()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        User u2 = DataHelper.createUserWithRoles("booking_test_user2", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d2 = DataHelper.createDice("testdice2", null, admin, u2, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);

        Booking b2 = new Booking();
        b2.setDice(d2);
        b2.setBookingCategory(cat);
        //set activity end time to 5 minutes before current time.
        Date endingTime = new Date(new Date().getTime() - 60*1000*5);
        b2.setActivityEndDate(endingTime);
        //set activity start time to 30 minutes ago
        Date startingTime = new Date(new Date().getTime() - 60*1000*30);
        b2.setActivityStartDate(startingTime);

        Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.saveBooking(b2);
        }, "Booking was saved despite dice belonging to another user than logged in.");
    }

    /**
     * Tests adding a new booking which ends in the future
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void save_booking_future()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);

        Booking b1 = new Booking();
        b1.setDice(d1);
        b1.setBookingCategory(cat);
        //set activity end time to 5 minutes after current time.
        Date endingTime = new Date(new Date().getTime() + 60*1000*5);
        b1.setActivityEndDate(endingTime);
        Date startingTime = new Date(new Date().getTime() - 60*1000*60*5);
        b1.setActivityStartDate(startingTime);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            bookingService.saveBooking(b1);
        }, "Booking was saved despite activity ending in the future.");
    }

    /**
     * Tests adding data from longer ago than the previous week
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void save_booking_2_weeks_ago()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);

        Booking b1 = new Booking();
        b1.setDice(d1);
        b1.setBookingCategory(cat);
        //set activity end time to 15 days before current time.
        Date endingTime = new Date(new Date().getTime() - 1000*60*60*24*15);
        b1.setActivityEndDate(endingTime);
        //set activity start time to 15.2 days ago
        Date startingTime = new Date(new Date().getTime() - (long)(1000*60*60*24*15.2));
        b1.setActivityStartDate(startingTime);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            bookingService.saveBooking(b1);
        }, "Booking was saved despite being before the previous week..");
    }

    /**
     * Tests adding data from longer ago than the previous week, with permissions
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void save_booking_2_weeks_ago_with_permissions()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        u1.setMayEditHistoricData(true);
        u1 = userRepository.save(u1);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);

        Booking b1 = new Booking();
        b1.setDice(d1);
        b1.setBookingCategory(cat);
        //set activity end time to 15 days before current time.
        Date endingTime = new Date(new Date().getTime() - 1000*60*60*24*15);
        b1.setActivityEndDate(endingTime);
        //set activity start time to 15.2 days ago
        Date startingTime = new Date(new Date().getTime() - (long)(1000*60*60*24*15.2));
        b1.setActivityStartDate(startingTime);

        Assertions.assertDoesNotThrow(() -> {
            bookingService.saveBooking(b1);
        }, "Exception was thrown despite user having permissions to save historic data.");
    }

    /**
     * Tests if exceptions are properly thrown when trying to save bookings over vacations
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void save_booking_over_vacation() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);

        Vacation v1 = DataHelper.createVacation(-4, -1, u1, vacationRepository);
        Vacation v2 = DataHelper.createVacation( -7, -7, u1, vacationRepository);

        Booking b1 = new Booking();
        b1.setBookingCategory(cat);
        b1.setDice(d1);
        long currentEpoch = new Date().toInstant().toEpochMilli();

        //4 days ago, covered by vacation
        b1.setActivityStartDate(Date.from(Instant.ofEpochMilli(currentEpoch - 1000 * 60 * 60 * 24 * 4)));
        b1.setActivityEndDate(Date.from(Instant.ofEpochMilli(currentEpoch - 1000 * 60 * 60 * 24 * 4 + 1000 * 60 * 30)));
        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> bookingService.saveBooking(b1));

        //1 day ago, covered by vacation
        b1.setActivityStartDate(Date.from(Instant.ofEpochMilli(currentEpoch - 1000 * 60 * 60 * 24)));
        b1.setActivityEndDate(Date.from(Instant.ofEpochMilli(currentEpoch - 1000 * 60 * 60 * 24 + 1000 * 60 * 30)));
        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> bookingService.saveBooking(b1));

        //starts 8 days ago, ends 7 days ago, covered by v2 by end date
        b1.setActivityStartDate(Date.from(Instant.ofEpochMilli(currentEpoch  - 1000 * 60 * 60 * 24 * 8)));
        b1.setActivityEndDate(Date.from(Instant.ofEpochMilli(currentEpoch  - 1000 * 60 * 60 * 24 * 7)));
        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> bookingService.saveBooking(b1));

        //5 days ago, not covered by vacation
        b1.setActivityStartDate(Date.from(Instant.ofEpochMilli(currentEpoch - 1000 * 60 * 60 * 24 * 5)));
        b1.setActivityEndDate(Date.from(Instant.ofEpochMilli(currentEpoch - 1000 * 60 * 60 * 24 * 5 + 1000 * 60 * 30)));
        Assertions.assertDoesNotThrow(() -> bookingService.saveBooking(b1));
    }

    /**
     * Tests accessing the save method without being EMPLOYEE
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"TEAMLEADER", "DEPARTMENTLEADER", "ADMIN"})
    public void add_update_booking_unauthorized() throws ProdigaGeneralExpectedException
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingService.saveBooking(new Booking());
        }, "User was able to access deletion method despite lacking authorization.");
    }

    /**
     * Tests changing an existing booking
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void update_booking() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Department dept2 = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        Team team2 = DataHelper.createRandomTeam(dept2, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        BookingCategory cat2 = DataHelper.createBookingCategory("test_category_02", admin, bookingCategoryRepository);
        Booking b1 = DataHelper.createBooking(cat, admin, d1, bookingRepository);

        //Change users team and department -> should not change the team and department of the booking
        u1.setAssignedTeam(team2);
        u1.setAssignedDepartment(dept2);
        userRepository.save(u1);

        b1.setBookingCategory(cat2);
        //set activity end time to 5 minutes before current time.
        Date endingTime = new Date(new Date().getTime() - 60*1000*5);
        b1.setActivityEndDate(endingTime);
        //set activity start time to 30 minutes ago
        Date startingTime = new Date(new Date().getTime() - 60*1000*30);
        b1.setActivityStartDate(startingTime);

        b1 = bookingService.saveBooking(b1);

        Assertions.assertEquals(startingTime, b1.getActivityStartDate(), "Activity start time was not updated properly.");
        Assertions.assertEquals(endingTime, b1.getActivityEndDate(), "Activity end time was not updated properly.");
        Assertions.assertEquals(team, b1.getTeam(), "Team was updated, but should not have been.");
        Assertions.assertEquals(dept, b1.getDept(), "Department was updated, but should not have been.");
        Assertions.assertEquals(cat2, b1.getBookingCategory(), "Booking category was not updated properly.");
    }

    /**
     * Tests changing an existing booking and changing the dice, which is not allowed
     */
        @Test
        @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
        public void update_booking_with_dice() throws ProdigaGeneralExpectedException
        {
            User admin = DataHelper.createAdminUser("admin", userRepository);
            Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
            Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
            User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
            Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
            Dice d2 = DataHelper.createDice("testdice2", null, admin, null, diceRepository, raspberryPiRepository, roomRepository);
            BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
            Booking b1 = DataHelper.createBooking(cat, admin, d1, bookingRepository);

            b1.setDice(d2);

            Assertions.assertThrows(RuntimeException.class, () -> {
                bookingService.saveBooking(b1);
            }, "User was able to change the dice on the existing booking.");
    }

    /**
     * Tests changing an existing booking's time into the past, without permission
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void update_booking_to_past() throws ProdigaGeneralExpectedException {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE), admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        Booking b1 = DataHelper.createBooking(cat, admin, d1, bookingRepository);

        //set activity end time to 15 days before current time.
        Date endingTime = new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 15);
        b1.setActivityEndDate(endingTime);
        //set activity start time to 15.2 days ago
        Date startingTime = new Date(new Date().getTime() - (long) (1000 * 60 * 60 * 24 * 15.2));
        b1.setActivityStartDate(startingTime);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            bookingService.saveBooking(b1);
        }, "User was able to update booking into before last week without having sufficient authorization.");
    }

    /**
     * Tests changing an existing booking's time into the past, with permission
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void update_booking_to_past_with_permissions() throws ProdigaGeneralExpectedException {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE), admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        Booking b1 = DataHelper.createBooking(cat, admin, d1, bookingRepository);

        u1.setMayEditHistoricData(true);
        userRepository.save(u1);

        //set activity end time to 15 days before current time.
        Date endingTime = new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 15);
        b1.setActivityEndDate(endingTime);
        //set activity start time to 15.2 days ago
        Date startingTime = new Date(new Date().getTime() - (long) (1000 * 60 * 60 * 24 * 15.2));
        b1.setActivityStartDate(startingTime);

        Assertions.assertDoesNotThrow(() -> {
            bookingService.saveBooking(b1);
        }, "User was not able to update booking into before last week despite having sufficient authorization.");
    }

    /**
     * Tests changing an existing booking's time from the past
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void update_booking_from_past() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        BookingCategory cat2 = DataHelper.createBookingCategory("test_category_02", admin, bookingCategoryRepository);
        Booking b1 = DataHelper.createBooking(cat, new Date(new Date().getTime() - (long)(1000 * 60 * 60 * 24 * 15.2)), new Date(new Date().getTime() - (long)(1000 * 60 * 60 * 24 * 15)), admin, d1, bookingRepository);

        b1.setBookingCategory(cat2);
        //Changing the activity time FROM an earlier date TO an allowed date should not allow the user to save the booking, since the original booking was too long ago.
        //set activity end time to 3 days ago.
        Date endingTime = new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 3);
        b1.setActivityEndDate(endingTime);
        //set activity start time to 3.2 days ago
        Date startingTime = new Date(new Date().getTime() - (long) (1000 * 60 * 60 * 24 * 3.2));
        b1.setActivityStartDate(startingTime);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            bookingService.saveBooking(b1);
        }, "User was able to update booking from before last week without having sufficient authorization.");
    }

    /**
     * Tests changing an existing booking's time from the past, with authorizationn
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void update_booking_from_past_with_permissions() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        BookingCategory cat2 = DataHelper.createBookingCategory("test_category_02", admin, bookingCategoryRepository);
        Booking b1 = DataHelper.createBooking(cat, new Date(new Date().getTime() - (long)(1000 * 60 * 60 * 24 * 15.2)), new Date(new Date().getTime() - (long)(1000 * 60 * 60 * 24 * 15)), admin, d1, bookingRepository);


        u1.setMayEditHistoricData(true);
        userRepository.save(u1);

        b1.setBookingCategory(cat2);
        //set activity end time to 3 days ago.
        Date endingTime = new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 3);
        b1.setActivityEndDate(endingTime);
        //set activity start time to 3.2 days ago
        Date startingTime = new Date(new Date().getTime() - (long) (1000 * 60 * 60 * 24 * 3.2));
        b1.setActivityStartDate(startingTime);

        Assertions.assertDoesNotThrow(() -> {
            bookingService.saveBooking(b1);
        }, "User was not able to update booking from before last week despite having sufficient authorization.");
    }

    /**
     * Tests changing the booking of another user
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void update_booking_other_user() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        User u2 = DataHelper.createUserWithRoles("booking_test_user2", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d2 = DataHelper.createDice("testdice1", null, admin, u2, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        BookingCategory cat2 = DataHelper.createBookingCategory("test_category_02", admin, bookingCategoryRepository);
        Booking b1 = DataHelper.createBooking(cat, admin, d2, bookingRepository);


        b1.setBookingCategory(cat2);

        Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.saveBooking(b1);
        }, "User was able to update booking from another user.");
    }

    /**
     * Tests deleting an existing booking
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void delete_booking() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        Booking b1 = DataHelper.createBooking(cat, admin, d1, bookingRepository);

        Assertions.assertDoesNotThrow(() -> {
            bookingService.deleteBooking(b1, false);
        }, "User was not able to delete a valid booking.");
    }

    /**
     * Tests deleting an existing booking without being EMPLOYEE
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"TEAMLEADER", "DEPARTMENTLEADER", "ADMIN"})
    public void delete_booking_unauthorized() throws ProdigaGeneralExpectedException
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
                bookingService.deleteBooking(new Booking(), false);
        }, "User was able to access deletion method despite lacking authorization.");
    }

    /**
     * Tests deleting another users existing booking
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void delete_booking_other_user()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        User u2 = DataHelper.createUserWithRoles("booking_test_user2", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u2, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        Booking b1 = DataHelper.createBooking(cat, admin, d1, bookingRepository);

        Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.deleteBooking(b1, false);
        }, "User was able to delete another users valid booking.");
    }


    /**
     * Tests deleting an existing booking from earlier than last week
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void delete_booking_old()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        Booking b1 = DataHelper.createBooking(cat, new Date(new Date().getTime() - (long)(1000 * 60 * 60 * 24 * 15.2)), new Date(new Date().getTime() - (long)(1000 * 60 * 60 * 24 * 15)), admin, d1, bookingRepository);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            bookingService.deleteBooking(b1, false);
        }, "User was able to delete booking from before last week without having sufficient authorization.");
    }

    /**
     * Tests deleting an existing booking from earlier than last week with permissions
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void delete_booking_old_with_permissions()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        Booking b1 = DataHelper.createBooking(cat, new Date(new Date().getTime() - (long)(1000 * 60 * 60 * 24 * 15.2)), new Date(new Date().getTime() - (long)(1000 * 60 * 60 * 24 * 15)), admin, d1, bookingRepository);

        u1.setMayEditHistoricData(true);
        userRepository.save(u1);

        Assertions.assertDoesNotThrow(() -> {
            bookingService.deleteBooking(b1, false);
        }, "User was not able to delete booking from before last week despite having sufficient authorization.");
    }

    /**
     * Tests getting number of bookings by category
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void get_number_bookings_by_category()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user2", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);

        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        BookingCategory cat2 = DataHelper.createBookingCategory("test_category_02", admin, bookingCategoryRepository);

        for(int i=0;i<5;i++)
            DataHelper.createBooking(cat, admin, d1, bookingRepository);

        for(int i=0;i<10;i++)
            DataHelper.createBooking(cat2, admin, d1, bookingRepository);

        Assertions.assertEquals(5, bookingService.getNumberOfBookingsWithCategory(cat), "Number of bookings was not properly returned for category 1.");
        Assertions.assertEquals(10, bookingService.getNumberOfBookingsWithCategory(cat2), "Number of bookings was not properly returned for category 2.");
    }

    /**
     * Tests getting number of bookings by category without admin privileges
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"TEAMLEADER", "DEPARTMENTLEADER", "EMPLOYEE"})
    public void get_number_bookings_by_category_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingService.getNumberOfBookingsWithCategory(new BookingCategory());
        }, "User was able to access method to get number of bookings despite lacking authorization.");
    }

    /**
     * Tests getting number of bookings by category and team
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"TEAMLEADER"})
    public void get_number_bookings_by_category_and_team()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        Team team2 = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE, UserRole.TEAMLEADER),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        User u2 = DataHelper.createUserWithRoles("booking_test_user2", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d2 = DataHelper.createDice("testdice2", null, admin, u2, diceRepository, raspberryPiRepository, roomRepository);
        User u3 = DataHelper.createUserWithRoles("booking_test_user3", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team2, userRepository);
        Dice d3 = DataHelper.createDice("testdice3", null, admin, u3, diceRepository, raspberryPiRepository, roomRepository);


        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);

        for(int i=0;i<5;i++)
            DataHelper.createBooking(cat, admin, d1, bookingRepository);

        for(int i=0;i<10;i++)
            DataHelper.createBooking(cat, admin, d2, bookingRepository);

        for(int i=0;i<10;i++)
            DataHelper.createBooking(cat, admin, d3, bookingRepository);

        Assertions.assertEquals(15, bookingService.getNumberOfTeamBookingsWithCategory(cat), "Number of bookings was not properly returned for category.");
    }

    /**
     * Tests getting number of bookings by category and team without TEAMLEADER privileges
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN", "DEPARTMENTLEADER", "EMPLOYEE"})
    public void get_number_bookings_by_category_and_team_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingService.getNumberOfTeamBookingsWithCategory(new BookingCategory());
        }, "User was able to access method to get number of bookings despite lacking authorization.");
    }

    /**
     * Tests getting the last booking of the users dice
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "empl", authorities = {"EMPLOYEE"})
    public void get_last_booking_by_dice()
    {
        User admin = DataHelper.createAdminUser("adminuser", userRepository);

        User u = DataHelper.createUserWithRoles("empl", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Room r = DataHelper.createRoom("testroom", admin, roomRepository);
        RaspberryPi raspi = DataHelper.createRaspi("raspi", u, r, raspberryPiRepository, roomRepository);
        Dice d = DataHelper.createDice("anydice", raspi, admin, u, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("testcat1", u, bookingCategoryRepository);

        DataHelper.createBooking(cat, Date.from(Instant.now().plusSeconds(60)), Date.from(Instant.now().plusSeconds(70)), u, d, bookingRepository);
        DataHelper.createBooking(cat, Date.from(Instant.now().plusSeconds(70)), Date.from(Instant.now().plusSeconds(80)), u, d, bookingRepository);
        Booking last = DataHelper.createBooking(cat, Date.from(Instant.now().plusSeconds(80)), Date.from(Instant.now().plusSeconds(90)), u, d, bookingRepository);

        Assertions.assertEquals(last, bookingService.getLastBookingForDiceWithAuth(d));
    }

    /**
     * Tests getting the last booking of another users dice
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "empl", authorities = {"EMPLOYEE"})
    public void get_last_booking_by_dice_other_user()
    {
        User admin = DataHelper.createAdminUser("adminuser", userRepository);

        User u = DataHelper.createUserWithRoles("empl", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        User u2 = DataHelper.createUserWithRoles("emp2", Sets.newSet(UserRole.EMPLOYEE), userRepository);

        Room r = DataHelper.createRoom("testroom", admin, roomRepository);
        RaspberryPi raspi = DataHelper.createRaspi("raspi", u, r, raspberryPiRepository, roomRepository);

        Dice d = DataHelper.createDice("anydice", raspi, admin, u, diceRepository, raspberryPiRepository, roomRepository);
        Dice d2 = DataHelper.createDice("anydice2", raspi, admin, u2, diceRepository, raspberryPiRepository, roomRepository);

        Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.getLastBookingForDiceWithAuth(d2);
        }, "User was able to access method to get last booking from another dice.");
    }

    /**
     * Tests getting last booking without EMPLOYEE privileges
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN", "DEPARTMENTLEADER", "TEAMLEADER"})
    public void get_last_booking_by_dice_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingService.getLastBookingForDiceWithAuth(new Dice());
        }, "User was able to access last booking method despite lacking authorization of EMPLOYEE.");
    }

    /**
     * Tests the method to check all bookings in range by category and user
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"EMPLOYEE", "ADMIN"})
    public void category_range_for_user_and_cat()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);

        User u2 = DataHelper.createUserWithRoles("booking_test_user2", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d2 = DataHelper.createDice("testdice2", null, admin, u2, diceRepository, raspberryPiRepository, roomRepository);

        BookingCategory cat = DataHelper.createBookingCategory("testcat", admin, bookingCategoryRepository);
        BookingCategory cat2 = DataHelper.createBookingCategory("testcat2", admin, bookingCategoryRepository);

        Booking b1 = DataHelper.createBooking(cat, new Date(new Date().getTime() + 1000 * 60 * 60 * 24), new Date(new Date().getTime() + 1000 * 60 * 60 * 24 + 1000 * 60 * 60 * 30), u1, d1, bookingRepository);
        Booking b2 = DataHelper.createBooking(cat, new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 7), new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 7 + 1000 * 60 * 60 * 30), u1, d1, bookingRepository);
        Booking b3 = DataHelper.createBooking(cat2, new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 7), new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 7 + 1000 * 60 * 60 * 30), u1, d1, bookingRepository);
        Booking b4 = DataHelper.createBooking(cat, new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 7), new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 7 + 1000 * 60 * 60 * 30), u2, d2, bookingRepository);

        Collection<Booking> lastWeekCat = bookingService.getBookingInRangeByCategoryAndByUser(u1, cat, new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 10), new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 5));

        Assertions.assertFalse(lastWeekCat.contains(b1));
        Assertions.assertTrue(lastWeekCat.contains(b2));
        Assertions.assertFalse(lastWeekCat.contains(b3));
        Assertions.assertFalse(lastWeekCat.contains(b4));
    }

    /**
     * Tests the method to check all bookings in range by category and user
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"EMPLOYEE", "ADMIN"})
    public void category_range_for_all_users()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);

        User u2 = DataHelper.createUserWithRoles("booking_test_user2", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d2 = DataHelper.createDice("testdice2", null, admin, u2, diceRepository, raspberryPiRepository, roomRepository);

        BookingCategory cat = DataHelper.createBookingCategory("testcat", admin, bookingCategoryRepository);
        BookingCategory cat2 = DataHelper.createBookingCategory("testcat2", admin, bookingCategoryRepository);

        Booking b1 = DataHelper.createBooking(cat, new Date(new Date().getTime() + 1000 * 60 * 60 * 24), new Date(new Date().getTime() + 1000 * 60 * 60 * 24 + 1000 * 60 * 60 * 30), u1, d1, bookingRepository);
        Booking b2 = DataHelper.createBooking(cat, new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 7), new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 7 + 1000 * 60 * 60 * 30), u1, d1, bookingRepository);
        Booking b3 = DataHelper.createBooking(cat2, new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 7), new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 7 + 1000 * 60 * 60 * 30), u1, d1, bookingRepository);
        Booking b4 = DataHelper.createBooking(cat, new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 7), new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 7 + 1000 * 60 * 60 * 30), u2, d2, bookingRepository);

        Collection<Booking> lastWeekCat = bookingService.getBookingInRangeByCategory(cat, new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 10), new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 5));

        Assertions.assertFalse(lastWeekCat.contains(b1));
        Assertions.assertTrue(lastWeekCat.contains(b2));
        Assertions.assertFalse(lastWeekCat.contains(b3));
        Assertions.assertTrue(lastWeekCat.contains(b4));
    }

    /**
     * Tests checking if a users booking was longer than 2 days ago
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void booking_longer_than_2_days_ago()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("testcat", admin, bookingCategoryRepository);

        DataHelper.createBooking(cat, new Date(new Date().getTime() - 1000*60*60*24*4), new Date(new Date().getTime() - 1000*60*60*24*4 + 1000*60*30), u1, d1, bookingRepository);
        Assertions.assertTrue(bookingService.isBookingLongerThan2DaysAgo(u1), "Last booking was not shown as being longer than 2 days ago, but was.");
        DataHelper.createBooking(cat, new Date(new Date().getTime() - 1000*60*60), new Date(new Date().getTime() - 1000*60*30), u1, d1, bookingRepository);
        Assertions.assertFalse(bookingService.isBookingLongerThan2DaysAgo(u1), "Last booking was shown to be longer than 2 days ago, but was not.");
    }

    /**
     * Tests the method to check all bookings in a certain category last week
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"EMPLOYEE", "ADMIN"})
    public void category_range_for_last_week()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("testcat", admin, bookingCategoryRepository);
        BookingCategory cat2 = DataHelper.createBookingCategory("testcat2", admin, bookingCategoryRepository);

        LocalDate lastWeek = LocalDate.now().minusWeeks(1);

        Booking b1 = DataHelper.createBooking(cat, new Date(new Date().getTime() + 1000 * 60 * 60 * 24), new Date(new Date().getTime() + 1000 * 60 * 60 * 24 + 1000 * 60 * 60 * 30), u1, d1, bookingRepository);
        Booking b2 = DataHelper.createBooking(cat, Date.from(lastWeek.atStartOfDay(ZoneId.systemDefault()).toInstant().plusSeconds(60*60*5)), Date.from(lastWeek.atStartOfDay(ZoneId.systemDefault()).toInstant().plusSeconds(60*60*5)), u1, d1, bookingRepository);
        Booking b3 = DataHelper.createBooking(cat2, new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 6), new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 7 + 1000 * 60 * 60 * 30), u1, d1, bookingRepository);

        Collection<Booking> lastWeekCat = bookingService.getBookingInRangeByCategoryForLastWeek(cat);

        Assertions.assertFalse(lastWeekCat.contains(b1));
        Assertions.assertTrue(lastWeekCat.contains(b2));
        Assertions.assertFalse(lastWeekCat.contains(b3));
    }

    /**
     * Tests the methods for daily, weekly and monthly booking ranges.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void get_booking_ranges()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate lastWeek = LocalDate.now().minusWeeks(1);
        LocalDate lastMonth = LocalDate.now().minusMonths(1);

        //create bookings centered around these dates
        Booking daybooking = DataHelper.createBooking(cat, Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant().plusSeconds(60*60*5)), Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant().plusSeconds(60*60*7)), u1, d1, bookingRepository);
        Booking weekbooking = DataHelper.createBooking(cat, Date.from(lastWeek.atStartOfDay(ZoneId.systemDefault()).toInstant().plusSeconds(60*60*5)), Date.from(lastWeek.atStartOfDay(ZoneId.systemDefault()).toInstant().plusSeconds(60*60*5)), u1, d1, bookingRepository);
        Booking monthbooking = DataHelper.createBooking(cat, Date.from(lastMonth.atStartOfDay(ZoneId.systemDefault()).toInstant().plusSeconds(60*60*5)), Date.from(lastMonth.atStartOfDay(ZoneId.systemDefault()).toInstant().plusSeconds(60*60*7)), u1, d1, bookingRepository);

        //Create a second set two days/weeks/months ago
        Booking daybooking2 = DataHelper.createBooking(cat, Date.from(yesterday.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().plusSeconds(60*60*5)), Date.from(yesterday.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().plusSeconds(60*60*7)), u1, d1, bookingRepository);
        Booking weekbooking2 = DataHelper.createBooking(cat, Date.from(lastWeek.minusWeeks(1).atStartOfDay(ZoneId.systemDefault()).toInstant().plusSeconds(60*60*5)), Date.from(lastWeek.minusWeeks(1).atStartOfDay(ZoneId.systemDefault()).toInstant().plusSeconds(60*60*7)), u1, d1, bookingRepository);
        Booking monthbooking2 = DataHelper.createBooking(cat, Date.from(lastMonth.minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant().plusSeconds(60*60*5)), Date.from(lastMonth.minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant().plusSeconds(60*60*7)), u1, d1, bookingRepository);

        //get ranges
        Collection<Booking> daybookings = bookingService.getUsersBookingInRangeByDay(u1, 1);
        Collection<Booking> weekbookings = bookingService.getUserBookingInRangeByWeek(u1, 1);
        Collection<Booking> monthbookings = bookingService.getUserBookingInRangeByMonth(u1, 1);

        //asserts
        Assertions.assertTrue(daybookings.contains(daybooking), "Collection returned incorrect results.");
        Assertions.assertFalse(daybookings.contains(daybooking2), "Collection returned incorrect results.");

        Assertions.assertTrue(weekbookings.contains(weekbooking), "Collection returned incorrect results.");
        Assertions.assertFalse(weekbookings.contains(weekbooking2), "Collection returned incorrect results.");

        Assertions.assertTrue(monthbookings.contains(monthbooking), "Collection returned incorrect results.");
        Assertions.assertFalse(monthbookings.contains(monthbooking2), "Collection returned incorrect results.");

        //get second ranges
        daybookings = bookingService.getUsersBookingInRangeByDay(u1, 2);
        weekbookings = bookingService.getUserBookingInRangeByWeek(u1, 2);
        monthbookings = bookingService.getUserBookingInRangeByMonth(u1, 2);

        //asserts
        Assertions.assertFalse(daybookings.contains(daybooking), "Collection returned incorrect results.");
        Assertions.assertTrue(daybookings.contains(daybooking2), "Collection returned incorrect results.");

        Assertions.assertFalse(weekbookings.contains(weekbookings), "Collection returned incorrect results.");
        Assertions.assertTrue(weekbookings.contains(weekbooking2), "Collection returned incorrect results.");

        Assertions.assertFalse(monthbookings.contains(monthbookings), "Collection returned incorrect results.");
        Assertions.assertTrue(monthbookings.contains(monthbooking2), "Collection returned incorrect results.");
    }
}
