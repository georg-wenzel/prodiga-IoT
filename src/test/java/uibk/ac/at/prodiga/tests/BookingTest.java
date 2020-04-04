package uibk.ac.at.prodiga.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.repositories.*;
import uibk.ac.at.prodiga.services.BookingService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

import java.util.Collection;
import java.util.Date;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class BookingTest
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
    TeamRepository teamRepository;

    @Autowired
    BookingTypeRepository bookingTypeRepository;

    @Autowired
    RaspberryPiRepository raspberryPiRepository;

    @Autowired
    BookingService bookingService;

    /**
     * Tests loading of booking by id.
     */
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void load_booking_by_id()
    {
        //Data setup
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d = DataHelper.createDice("testdice", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingType bt = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);
        Booking b = DataHelper.createBooking(bt, u1, d, bookingRepository);

        Booking booking_service = bookingService.loadBooking(b.getId());

        Assertions.assertEquals(b.getActivityStartDate(), booking_service.getActivityStartDate(), "Activity start date was not properly stored in DB.");
        Assertions.assertEquals(b.getActivityEndDate(), booking_service.getActivityEndDate(), "Activity end date was not stored properly in DB.");
        Assertions.assertEquals(d, booking_service.getDice(), "Dice was not properly stored in DB");
        Assertions.assertNull(booking_service.getObjectChangedDateTime(), "Booking changed date time should be null, but is not");
        Assertions.assertNull(booking_service.getObjectChangedUser(), "Booking changed user should be null, but is not");
        Assertions.assertEquals(u1, booking_service.getObjectCreatedUser(), "Creation user of booking type does not match booking_test_user1.");
        Assertions.assertEquals(b.getObjectCreatedDateTime(), booking_service.getObjectCreatedDateTime(), "Creation date not loaded properly for booking.");
    }

    /**
     * Tests loading of booking by dice with lacking authorization
     */
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"ADMIN", "DEPARTMENTLEADER", "TEAMLEADER"})
    public void load_booking_by_id_unauthorized()
    {
        //Data setup
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user_1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d = DataHelper.createDice("testdice", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingType bt = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);
        Booking b = DataHelper.createBooking(bt, u1, d, bookingRepository);


        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingService.loadBooking(b.getId());
        }, "Booking loaded despite lacking authorization of EMPLOYEE");
    }

    /**
     * Tests loading of booking by id when the logged in user does not match the user of the booking
     */
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void load_booking_by_id_from_other_user()
    {
        //Data setup
        User admin = DataHelper.createAdminUser("admin", userRepository);
        DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        User u2 = DataHelper.createUserWithRoles("booking_test_user2", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d = DataHelper.createDice("testdice", null, admin, u2, diceRepository, raspberryPiRepository, roomRepository);
        BookingType bt = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);
        Booking b = DataHelper.createBooking(bt, u2, d, bookingRepository);

        Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.loadBooking(b.getId());
        }, "Booking loaded from different user successfully.");
    }

    /**
     * Tests loading bookings by dice
     */
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
        BookingType bt1 = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);
        BookingType bt2 = DataHelper.createBookingType(5, true, admin, bookingTypeRepository);

        //Create two bookings for booking_test_user1 and one booking for booking_test_user2
        Booking b1 = DataHelper.createBooking(bt1, u1, d1, bookingRepository);
        Booking b2 = DataHelper.createBooking(bt2, u1, d1, bookingRepository);
        Booking b3 = DataHelper.createBooking(bt1, u2, d2, bookingRepository);

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
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "ADMIN"})
    public void load_booking_by_dice_unauthorized()
    {
        //Data setup
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingType bt1 = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);
        DataHelper.createBooking(bt1, u1, d1, bookingRepository);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingService.getAllBookingsByDice(d1);
        }, "Booking loaded despite lacking authorization of EMPLOYEE.");
    }

    /**
     * Tests loading bookings by department
     */
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
        BookingType bt1 = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);

        //Create two bookings for user in department and one booking for user outside of department
        Booking b1 = DataHelper.createBooking(bt1, u1, d1, bookingRepository);
        Booking b2 = DataHelper.createBooking(bt1, u1, d1, bookingRepository);
        Booking b3 = DataHelper.createBooking(bt1, u2, d2, bookingRepository);

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
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE", "TEAMLEADER", "ADMIN"})
    public void load_booking_by_dept_unauthorized()
    {
        //Data setup
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.DEPARTMENTLEADER),admin, dept, null, userRepository);
        User u2 = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE), admin, dept, null, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u2, diceRepository, raspberryPiRepository, roomRepository);
        BookingType bt1 = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);

        //Create two bookings for user in department and one booking for user outside of department
        Booking b1 = DataHelper.createBooking(bt1, u1, d1, bookingRepository);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingService.getAllBookingsByDepartment(dept);
        }, "Bookings of department loaded despite lacking authorization.");
    }

    /**
     * Tests loading bookings by team
     */
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
        BookingType bt1 = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);

        //Create two bookings for user in team and one booking for user outside of team
        Booking b1 = DataHelper.createBooking(bt1, u1, d1, bookingRepository);
        Booking b2 = DataHelper.createBooking(bt1, u1, d1, bookingRepository);
        Booking b3 = DataHelper.createBooking(bt1, u2, d2, bookingRepository);

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
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE", "DEPARTMENTLEADER", "ADMIN"})
    public void load_booking_by_team_unauthorized()
    {
        //Data setup
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team1 = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.TEAMLEADER),admin, dept, team1, userRepository);
        User u2 = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE), admin, dept, team1, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u2, diceRepository, raspberryPiRepository, roomRepository);
        BookingType bt1 = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);
        Booking b1 = DataHelper.createBooking(bt1, u1, d1, bookingRepository);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingService.getAllBookingsByTeam(team1);
        }, "Bookings of team loaded despite lacking authorization.");
    }

    /**
     * Tests adding a new booking
     */
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void save_booking() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingType bt1 = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);

        Booking b1 = new Booking();
        b1.setDice(d1);
        b1.setType(bt1);
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
        Assertions.assertEquals(bt1, b1.getType(), "Booking type was not returned properly.");
        Assertions.assertEquals(d1, b1.getDice(), "Dice was not returned properly.");
    }

    /**
     * Tests adding a new booking where start date is before end date
     */
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void save_booking_time_inverted() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingType bt1 = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);

        Booking b1 = new Booking();
        b1.setDice(d1);
        b1.setType(bt1);
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
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void save_booking_too_long() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingType bt1 = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);

        Booking b1 = new Booking();
        b1.setDice(d1);
        b1.setType(bt1);
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
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void save_booking_other_user() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        User u2 = DataHelper.createUserWithRoles("booking_test_user2", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d2 = DataHelper.createDice("testdice2", null, admin, u2, diceRepository, raspberryPiRepository, roomRepository);
        BookingType bt1 = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);

        Booking b2 = new Booking();
        b2.setDice(d2);
        b2.setType(bt1);
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
     * Tests adding data from longer ago than the previous week
     */
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void save_booking_2_weeks_ago() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingType bt1 = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);

        Booking b1 = new Booking();
        b1.setDice(d1);
        b1.setType(bt1);
        //set activity end time to 15 days before current time.
        Date endingTime = new Date(new Date().getTime() - 1000*60*60*24*15);
        b1.setActivityEndDate(endingTime);
        //set activity start time to 15.2 days ago
        Date startingTime = new Date(new Date().getTime() - (long)(1000*60*60*24*15.2));
        b1.setActivityStartDate(startingTime);

        Assertions.assertThrows(RuntimeException.class, () -> {
            bookingService.saveBooking(b1);
        }, "Booking was saved despite being before the previous week..");
    }

    /**
     * Tests adding data from longer ago than the previous week, with permissions
     */
    @Test
    @WithMockUser(username = "booking_test_user1", authorities = {"EMPLOYEE"})
    public void save_booking_2_weeks_ago_with_permissions() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        User u1 = DataHelper.createUserWithRoles("booking_test_user1", Sets.newSet(UserRole.EMPLOYEE),admin, dept, team, userRepository);
        u1.setMayEditHistoricData(true);
        u1 = userRepository.save(u1);
        Dice d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingType bt1 = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);

        Booking b1 = new Booking();
        b1.setDice(d1);
        b1.setType(bt1);
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
}
