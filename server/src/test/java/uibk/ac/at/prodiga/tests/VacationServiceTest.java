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
import uibk.ac.at.prodiga.services.VacationService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

import java.time.*;
import java.util.Collection;
import java.util.Date;

/**
 * Test class for the Vacation Service
 *
 * IMPORTANT:
 * Due to the ever changing semantics of weekends, holidays and date of test execution, these tests are not written as deterministically and strict as other service tests.
 * They should provide a good guideline on if a fundamental function broke, but testing the web app to make sure intended behavior is still complete is a good idea.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class VacationServiceTest
{
    @Autowired
    UserRepository userRepository;

    @Autowired
    VacationRepository vacationRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    DiceRepository diceRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    BookingCategoryRepository bookingCategoryRepository;

    @Autowired
    RaspberryPiRepository raspberryPiRepository;

    @Autowired
    VacationService vacationService;

    /**
     * Tests loading of vacation data
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void load_vacation_data()
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Vacation v1 = DataHelper.createVacation(5,10, u1, vacationRepository);

        Date fromDate = Date.from(LocalDate.now().plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate = Date.from(LocalDate.now().plusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Vacation v1_service = vacationService.getVacationById(v1.getId());

        Assertions.assertNotNull(v1_service, "Could not load test vacation.");
        Assertions.assertEquals(v1.getUser(), v1_service.getUser(), "Service returned user does not match DB state.");
        Assertions.assertEquals(v1.getBeginDate(), v1_service.getBeginDate(), "Service returned begin date does not match DB state.");
        Assertions.assertEquals(v1.getEndDate(), v1_service.getEndDate(), "Service returned end date does not match DB state.");
        Assertions.assertEquals(fromDate, v1_service.getBeginDate(), "Begin Date stored in database does not match up with target date.");
        Assertions.assertEquals(toDate, v1_service.getEndDate(), "End Date stored in database does not match up with target date.");
        Assertions.assertEquals(u1, v1_service.getUser(), "User was not correctly stored in DB.");
    }

    /**
     * Tests unauthorized loading of vacation data
     */
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"TEAMLEADER", "DEPARTMENTLEADER", "ADMIN"})
    public void load_vacation_data_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            vacationService.getVacationById((long)1);
        }, "Vacation loaded despite lacking authorization of EMPLOYEE");
    }

    /**
     * Tests loading all vacations for currently logged in user.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void load_vacations()
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        User u2 = DataHelper.createUserWithRoles("vacation_test_user_02", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Vacation v1 = DataHelper.createVacation(5,10, u1, vacationRepository);
        Vacation v2 = DataHelper.createVacation(14,21, u1, vacationRepository);
        Vacation v3 = DataHelper.createVacation(12,18,u2,vacationRepository);

        Collection<Vacation> vacs = vacationService.getAllVacations();

        Assertions.assertTrue(vacs.contains(v1), "Vacation v1 not found in vacation set for user u1.");
        Assertions.assertTrue(vacs.contains(v2), "Vacation v2 not found in vacation set for user u1.");
        Assertions.assertFalse(vacs.contains(v3), "Vacation v3 found in vacation set for user u1, but should not be.");
    }

    /**
     * Tests unauthorized loading of all user vacations
     */
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"TEAMLEADER", "DEPARTMENTLEADER", "ADMIN"})
    public void load_vacations_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            vacationService.getAllVacations();
        }, "Vacations loaded despite lacking authorization of EMPLOYEE");
    }

    /**
     * Tests creating a vacation which will always be valid for a new user.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void create_vacation() throws ProdigaGeneralExpectedException
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);

        Date fromDate = Date.from(LocalDate.now().plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate = Date.from(LocalDate.now().plusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Vacation v1 = new Vacation();
        v1.setBeginDate(fromDate);
        v1.setEndDate(toDate);
        v1.setUser(u1);

        v1 = vacationService.saveVacation(v1);

        Assertions.assertEquals(fromDate, v1.getBeginDate(), "Begin date not correctly stored for vacation.");
        Assertions.assertEquals(toDate, v1.getEndDate(), "End date not correctly stored for vacation.");
        Assertions.assertEquals(u1, v1.getUser(), "User not correctly stored for vacation.");
        Assertions.assertEquals(u1, v1.getObjectCreatedUser(), "User u1 did not become creation user of the vacation");
        Assertions.assertTrue((new Date()).getTime() -  v1.getObjectCreatedDateTime().getTime() < 1000 * 60, "Creation date has not been properly set.");
    }

    /**
     * Tests creating a vacation which will always overstep 25 yearly days
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void create_vacation_too_many_days()
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);

        Vacation v2;

        //Need to make two different cases, based on what date it currently is
        //if in december month
        if(LocalDate.now().getMonth() == Month.DECEMBER)
        {
            //create test case in following year
            //20 true  days
            Vacation v1 = DataHelper.createTrueVacation(LocalDate.of(LocalDate.now().getYear()+1, 1, 10), 20, u1, vacationRepository);
            //6 true days
            v2 = DataHelper.returnTrueVacation(LocalDate.of(LocalDate.now().getYear()+1, 3, 10), 6, u1);
        }
        else
        {
            //create test case in current year
            //20 true days earlier in the year
            Vacation v1 = DataHelper.createTrueVacation(LocalDate.of(LocalDate.now().getYear(), 1, 1), 20, u1, vacationRepository);
            //6 true days now
            v2 = DataHelper.returnTrueVacation(LocalDate.now().plusDays(1), 6, u1);
        }

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v2);
        }, "Vacation saved despite passing 25 days in one year.");
    }


    /**
     * Tests validation on the date input
     */
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void create_vacation_invalid_dates()
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);

        //vacation that ends before starting
        Date fromDate = Date.from(LocalDate.now().plusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate = Date.from(LocalDate.now().plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Vacation v1 = new Vacation();
        v1.setBeginDate(fromDate);
        v1.setEndDate(toDate);
        v1.setUser(u1);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v1);
        }, "Vacation saved despite ending before starting.");

        //vacation that is in the past
        fromDate = Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant());
        toDate = Date.from(LocalDate.now().minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Vacation v2 = new Vacation();
        v2.setBeginDate(fromDate);
        v2.setEndDate(toDate);
        v2.setUser(u1);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v2);
        }, "Vacation saved despite being in the past.");

        //vacation that is too far in the future
        fromDate = Date.from(LocalDate.now().plusYears(2).plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        toDate = Date.from(LocalDate.now().plusYears(2).plusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Vacation v3 = new Vacation();
        v3.setBeginDate(fromDate);
        v3.setEndDate(toDate);
        v3.setUser(u1);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v3);
        }, "Vacation saved despite being too far in the future.");

        //find next saturday, create weekend vacation
        LocalDate beginDate = LocalDate.now();
        while(beginDate.getDayOfWeek() != DayOfWeek.SATURDAY)
            beginDate = beginDate.plusDays(1);

        fromDate = Date.from(beginDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        toDate = Date.from(beginDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Vacation v4 = new Vacation();
        v4.setBeginDate(fromDate);
        v4.setEndDate(toDate);
        v4.setUser(u1);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v4);
        }, "Vacation saved despite being only weekend days.");

        //vacation on new years holiday (xxxx-01-01)
        fromDate = Date.from(LocalDate.of(LocalDate.now().getYear() + 1,1,1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        toDate = Date.from(LocalDate.of(LocalDate.now().getYear() + 1,1,1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Vacation v5 = new Vacation();
        v5.setBeginDate(fromDate);
        v5.setEndDate(toDate);
        v5.setUser(u1);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v5);
        }, "Vacation saved despite being on new years holiday.");
    }

    /**
     * Tests creating a vacation which passes new year, and is longer than 25 days total
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void create_vacation_new_year() throws ProdigaGeneralExpectedException
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);

        //create 25 true vacation days earlier
        DataHelper.createTrueVacation(LocalDate.of(LocalDate.now().getYear(), 1,1), 25, u1, vacationRepository);

        //create vacation over new years that lasts 25 true vacation days, all of those are in the new year, because xxxx-12-31 is a holiday.
        Vacation v1 = DataHelper.returnTrueVacation(LocalDate.of(LocalDate.now().getYear(), 12, 31), 25, u1);


        Vacation db_v1 = vacationService.saveVacation(v1);

        //asserts that dates still match
        Assertions.assertEquals(v1.getBeginDate(), db_v1.getBeginDate(), "Beginning Date was not correctly stored in DB.");
        Assertions.assertEquals(v1.getEndDate(), db_v1.getEndDate(), "End date was not correctly stored in DB.");
    }

    /**
     * Tests creating a vacation which passes new year, but user already has too many vacation days in the following year
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void create_vacation_new_year_invalid()
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);

        //get somewhere in june of next year and make a 20 day vacation
        DataHelper.createTrueVacation(LocalDate.of(LocalDate.now().getYear() + 1, 6, 10), 20, u1, vacationRepository);

        //vacation starts at xxxx-12-31, so no vacation days in the current year, then 6 true days in the next year, which is too many
        Vacation v1 = DataHelper.returnTrueVacation(LocalDate.of(LocalDate.now().getYear(),12,31), 6, u1);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v1);
        }, "Vacation saved despite user not having enough vacation days in the following year.");
    }

    /**
     * Tests validation on the date input with respect to existing vacations
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void create_vacation_overlapping()
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        DataHelper.createVacation(5,10, u1, vacationRepository);

        //vacation that overlaps with beginning date of v1
        Date fromDate1 = Date.from(LocalDate.now().plusDays(4).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate1 = Date.from(LocalDate.now().plusDays(8).atStartOfDay(ZoneId.systemDefault()).toInstant());

        //vacation that overlaps with ending date of v1
        Date fromDate2 = Date.from(LocalDate.now().plusDays(9).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate2 = Date.from(LocalDate.now().plusDays(13).atStartOfDay(ZoneId.systemDefault()).toInstant());

        //vacation that completely covers v1
        Date fromDate3 = Date.from(LocalDate.now().plusDays(4).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate3 = Date.from(LocalDate.now().plusDays(12).atStartOfDay(ZoneId.systemDefault()).toInstant());

        //vacation that is completely covered by v1
        Date fromDate4 = Date.from(LocalDate.now().plusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate4 = Date.from(LocalDate.now().plusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant());


        //None of these should pass
        Vacation v1 = new Vacation();
        v1.setBeginDate(fromDate1);
        v1.setEndDate(toDate1);
        v1.setUser(u1);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v1);
        }, "Vacation that intersects with v1 was saved successfully.");


        Vacation v2 = new Vacation();
        v2.setBeginDate(fromDate2);
        v2.setEndDate(toDate2);
        v2.setUser(u1);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v2);
        }, "Vacation that intersects with v1 was saved successfully.");

        Vacation v3 = new Vacation();
        v3.setBeginDate(fromDate3);
        v3.setEndDate(toDate3);
        v3.setUser(u1);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v3);
        }, "Vacation that intersects with v1 was saved successfully.");

        Vacation v4 = new Vacation();
        v4.setBeginDate(fromDate4);
        v4.setEndDate(toDate4);
        v4.setUser(u1);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v4);
        }, "Vacation that intersects with v1 was saved successfully.");
    }

    /**
     * Tests validation on the date input with respect to existing vacations
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void create_vacation_on_booking_day()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d = DataHelper.createDice("testdice", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        DataHelper.createBooking(cat, Date.from(Instant.ofEpochMilli(new Date().getTime() + 1000 * 60 * 60 * 24 * 2)), Date.from(Instant.ofEpochMilli(new Date().getTime() + 1000 * 60 * 60 * 24 * 3)),  u1, bookingRepository);

        //vacation that overlaps with the booking
        Date fromDate1 = Date.from(LocalDate.now().plusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate1 = Date.from(LocalDate.now().plusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Vacation v1 = new Vacation();
        v1.setBeginDate(fromDate1);
        v1.setEndDate(toDate1);
        v1.setUser(u1);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v1);
        }, "Vacation that intersects with a booking was saved successfully.");
    }

    /**
     * Tests if vacations that block booking dates are properly returned.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void check_booking_covers_vacation_day()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);

        Vacation v1 = DataHelper.createVacation(-4, -1, u1, vacationRepository);
        Vacation v2 = DataHelper.createVacation( -7, -7, u1, vacationRepository);

        Booking b1 = new Booking();
        long currentEpoch = new Date().toInstant().toEpochMilli();

        //5 days ago, not covered by vacation
        b1.setActivityStartDate(Date.from(Instant.ofEpochMilli(currentEpoch - 1000 * 60 * 60 * 24 * 5)));
        b1.setActivityEndDate(Date.from(Instant.ofEpochMilli(currentEpoch - 1000 * 60 * 60 * 24 * 5 + 1000 * 60 * 30)));
        Assertions.assertNull(vacationService.vacationCoversBooking(b1));

        //4 days ago, covered by vacation
        b1.setActivityStartDate(Date.from(Instant.ofEpochMilli(currentEpoch - 1000 * 60 * 60 * 24 * 4)));
        b1.setActivityEndDate(Date.from(Instant.ofEpochMilli(currentEpoch - 1000 * 60 * 60 * 24 * 4 + 1000 * 60 * 30)));
        Assertions.assertEquals(v1, vacationService.vacationCoversBooking(b1));

        //1 day ago, covered by vacation
        b1.setActivityStartDate(Date.from(Instant.ofEpochMilli(currentEpoch - 1000 * 60 * 60 * 24)));
        b1.setActivityEndDate(Date.from(Instant.ofEpochMilli(currentEpoch - 1000 * 60 * 60 * 24 + 1000 * 60 * 30)));
        Assertions.assertEquals(v1, vacationService.vacationCoversBooking(b1));

        //now, not covered by vacation
        b1.setActivityStartDate(Date.from(Instant.ofEpochMilli(currentEpoch)));
        b1.setActivityEndDate(Date.from(Instant.ofEpochMilli(currentEpoch + 1000 * 60 * 30)));
        Assertions.assertNull(vacationService.vacationCoversBooking(b1));

        //starts 8 days ago, ends 7 days ago, covered by v2 by end date
        b1.setActivityStartDate(Date.from(Instant.ofEpochMilli(currentEpoch  - 1000 * 60 * 60 * 24 * 8)));
        b1.setActivityEndDate(Date.from(Instant.ofEpochMilli(currentEpoch  - 1000 * 60 * 60 * 24 * 7)));
        Assertions.assertEquals(v2, vacationService.vacationCoversBooking(b1));
    }

    /**
     * Tests if check booking method can be called without EMPLOYEE authorization.
     */
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"TEAMLEADER", "DEPARTMENTLEADER", "ADMIN"})
    public void check_booking_covers_vacation_day_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            vacationService.vacationCoversBooking(new Booking());
        }, "Vacations for user checked despite lacking authorization of EMPLOYEE");
    }


    /**
     * Tests creating a vacation for another user
     */
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void create_vacation_other_user()
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_02", Sets.newSet(UserRole.EMPLOYEE), userRepository);

        Date fromDate = Date.from(LocalDate.now().plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate = Date.from(LocalDate.now().plusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Vacation v1 = new Vacation();
        v1.setBeginDate(fromDate);
        v1.setEndDate(toDate);
        v1.setUser(u1);

        Assertions.assertThrows(RuntimeException.class, () -> {
            vacationService.saveVacation(v1);
        }, "Vacation saved successfully for another user..");
    }

    /**
     * Tests creating a vacation without employee authorization
     */
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"TEAMLEADER", "DEPARTMENTLEADER", "ADMIN"})
    public void create_vacation_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            vacationService.saveVacation(new Vacation());
        }, "Vacations created despite lacking authorization of EMPLOYEE");
    }

    /**
     * Tests changing a vacation which will always be valid.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void update_vacation() throws ProdigaGeneralExpectedException
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Vacation v1 = DataHelper.createVacation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(10), u1, vacationRepository);

        Date newStartDate = Date.from(LocalDate.now().plusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date newEndDate = Date.from(LocalDate.now().plusDays(14).atStartOfDay(ZoneId.systemDefault()).toInstant());

        v1.setBeginDate(newStartDate);
        v1.setEndDate(newEndDate);

        v1 = vacationService.saveVacation(v1);

        Assertions.assertEquals(newStartDate, v1.getBeginDate(), "Begin date not correctly updated for vacation.");
        Assertions.assertEquals(newEndDate, v1.getEndDate(), "End date not correctly updated for vacation.");
        Assertions.assertEquals(u1, v1.getUser(), "User not correctly stored for vacation.");
        Assertions.assertEquals(u1, v1.getObjectChangedUser(), "User u1 did not become update user of the vacation.");
        Assertions.assertTrue((new Date()).getTime() -  v1.getObjectChangedDateTime().getTime() < 1000 * 60, "Update date has not been properly set.");
    }

    /**
     * Tests changing a vacation from the past to one with a valid time (should not work)
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void update_vacation_from_past()
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Vacation v1 = DataHelper.createVacation(-6, -3, u1, vacationRepository);

        Date newStartDate = Date.from(LocalDate.now().plusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date newEndDate = Date.from(LocalDate.now().plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant());

        v1.setBeginDate(newStartDate);
        v1.setEndDate(newEndDate);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v1);
        }, "Vacation edited despite being from past date.");
    }

    /**
     * Tests changing the user of a vacation to another.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void update_vacation_to_other_user()
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        User u2 = DataHelper.createRandomUser(userRepository);
        Vacation v1 = DataHelper.createVacation(5, 10, u1, vacationRepository);

        v1.setUser(u2);

        Assertions.assertThrows(RuntimeException.class, () -> {
            vacationService.saveVacation(v1);
        }, "Vacation edited despite user being changed to another.");
    }

    /**
     * Tests changing the vacation of another user
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void update_vacation_from_other_user()
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        User u2 = DataHelper.createRandomUser(userRepository);
        Vacation v1 = DataHelper.createVacation(5, 10, u2, vacationRepository);

        v1.setUser(u1);

        Assertions.assertThrows(RuntimeException.class, () -> {
            vacationService.saveVacation(v1);
        }, "Vacation edited despite user being not the logged in user.");
    }

    /**
     * Tests deleting a vacation
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void delete_vacation() throws ProdigaGeneralExpectedException
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Vacation v1 = DataHelper.createVacation(5,10, u1, vacationRepository);

        vacationService.deleteVacation(v1, false);

        Assertions.assertNull(vacationService.getVacationById(v1.getId()), "Vacation was not properly deleted.");
    }

    /**
     * Tests deleting a vacation from another user
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void delete_vacation_other_user()
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        User u2 = DataHelper.createRandomUser(userRepository);
        Vacation v1 = DataHelper.createVacation(5,10, u2, vacationRepository);

        Assertions.assertThrows(RuntimeException.class, () -> {
            vacationService.deleteVacation(v1, false);
        }, "Vacation deleted despite being from another user.");
    }

    /**
     * Tests deleting a vacation from the past
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void delete_vacation_past()
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Vacation v1 = DataHelper.createVacation(-6,5, u1, vacationRepository);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.deleteVacation(v1, false);
        }, "Vacation deleted despite having started already.");
    }

    /**
     * Tests deleting a vacation without employee authorization
     */
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"TEAMLEADER", "DEPARTMENTLEADER", "ADMIN"})
    public void delete_vacation_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            vacationService.deleteVacation(new Vacation(), false);
        }, "Vacation deleted despite lacking authorization of EMPLOYEE");
    }

    /**
     * Tests getting the number of remaining vacation days
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void get_remaining_vacation_days()
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        //4 days in current year (27., 28., 29., 30.), 10 days in next year
        LocalDate decemberDate = LocalDate.of(LocalDate.now().getYear(), 12, 27);
        DataHelper.createTrueVacation(decemberDate, 14, u1, vacationRepository);;
        //3 days in next year
        DataHelper.createTrueVacation(LocalDate.of(LocalDate.now().getYear() + 1, 3, 5), 3, u1, vacationRepository);;
        //11 days in current year
        DataHelper.createTrueVacation(LocalDate.of(LocalDate.now().getYear(), 6, 10), 11, u1, vacationRepository);;

        //remaining vacation days
        //depending on whether or not there is weekend days in 27,28,29,30, the amount of vacation days differs
        int addThisYear = 0;
        if(decemberDate.getDayOfWeek() == DayOfWeek.WEDNESDAY || decemberDate.getDayOfWeek() == DayOfWeek.SUNDAY) addThisYear = 1;
        else if(decemberDate.getDayOfWeek() == DayOfWeek.THURSDAY || decemberDate.getDayOfWeek() == DayOfWeek.SATURDAY || decemberDate.getDayOfWeek() == DayOfWeek.FRIDAY) addThisYear = 2;

        Assertions.assertEquals(10 + addThisYear, vacationService.getUsersRemainingVacationDays(LocalDate.now().getYear()), "Vacation days for the current year were not calculated properly.");
        Assertions.assertEquals(12 - addThisYear,vacationService.getUsersRemainingVacationDays(LocalDate.now().getYear() + 1), "Vacation days for the next year were not calculated properly.");
    }

    /**
     * Tests getting remaining vacation days without employee authorization
     */
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"TEAMLEADER", "DEPARTMENTLEADER", "ADMIN"})
    public void get_remaining_vacation_days_unauthorized() throws ProdigaGeneralExpectedException
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            vacationService.getUsersRemainingVacationDays(2020);
        }, "Vacation days returned despite lacking authorization of EMPLOYEE");
    }
}
