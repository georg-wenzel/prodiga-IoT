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

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;

/**
 * Test class for the Vacation Service
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
    BookingTypeRepository bookingTypeRepository;

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
        Vacation v3 = DataHelper.createVacation(12,15,u2,vacationRepository);

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
     * Tests creating a vacation which will always overstep 25 days
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void create_vacation_too_many_days() throws ProdigaGeneralExpectedException
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);

        Date fromDate, toDate;

        //Need to make two different cases, based on what date it currently is
        //if in december month
        if(LocalDate.now().getMonth() == Month.DECEMBER)
        {
            //create test case in following year
            //20 vacation days
            DataHelper.createVacation(100,120, u1, vacationRepository);
            //6 vacation days
            fromDate = Date.from(LocalDate.now().plusDays(40).atStartOfDay(ZoneId.systemDefault()).toInstant());
            toDate = Date.from(LocalDate.now().plusDays(46).atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        else
        {
            //create test case in current year
            //20 vacation days
            DataHelper.createVacation(0,20, u1, vacationRepository);
            //6 vacation days
            fromDate = Date.from(LocalDate.now().plusDays(21).atStartOfDay(ZoneId.systemDefault()).toInstant());
            toDate = Date.from(LocalDate.now().plusDays(27).atStartOfDay(ZoneId.systemDefault()).toInstant());
        }


        Vacation v1 = new Vacation();
        v1.setBeginDate(fromDate);
        v1.setEndDate(toDate);
        v1.setUser(u1);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v1);
        }, "Vacation saved despite passing 25 days in one year.");
    }


    /**
     * Tests validation on the date input
     */
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void create_vacation_invalid_dates() throws ProdigaGeneralExpectedException
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


        //vacation that is too short (same day)
        Vacation v2 = new Vacation();
        v2.setBeginDate(fromDate);
        v2.setEndDate(fromDate);
        v2.setUser(u1);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v2);
        }, "Vacation saved despite being too short.");

        //vacation that is in the past
        fromDate = Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant());
        toDate = Date.from(LocalDate.now().minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Vacation v3 = new Vacation();
        v3.setBeginDate(fromDate);
        v3.setEndDate(toDate);
        v3.setUser(u1);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v3);
        }, "Vacation saved despite being in the past.");

        //vacation that is too far in the future
        fromDate = Date.from(LocalDate.now().plusYears(2).plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        toDate = Date.from(LocalDate.now().plusYears(2).plusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Vacation v4 = new Vacation();
        v4.setBeginDate(fromDate);
        v4.setEndDate(toDate);
        v4.setUser(u1);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v4);
        }, "Vacation saved despite being too far in the future.");
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
        Vacation v1 = new Vacation();

        //get 27th december of this year
        LocalDate startDate = LocalDate.of(LocalDate.now().getYear(), 12, 27);

        //if in december month
        if(LocalDate.now().getMonth() == Month.DECEMBER)
        {
            //20 vacation days earlier
            DataHelper.createVacation(-30,-25, u1, vacationRepository);
            if(LocalDate.now().getDayOfMonth() > 26)
            {
                //start date is simply now, as it is already past the 26th, so there is less than 6 days in the current year
                Date fromDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date toDate = Date.from(LocalDate.now().plusDays(27).atStartOfDay(ZoneId.systemDefault()).toInstant());
                v1.setBeginDate(fromDate);
                v1.setEndDate(toDate);
                v1.setUser(u1);
            }
            else
            {
                //start date is 27th dec
                Date fromDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date toDate = Date.from(startDate.plusDays(27).atStartOfDay(ZoneId.systemDefault()).toInstant());
                v1.setBeginDate(fromDate);
                v1.setEndDate(toDate);
                v1.setUser(u1);
            }
        }
        else
        {
            //20 vacation days now
            DataHelper.createVacation(0,20, u1, vacationRepository);
            //start date is 27th dec
            Date fromDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date toDate = Date.from(startDate.plusDays(27).atStartOfDay(ZoneId.systemDefault()).toInstant());
            v1.setBeginDate(fromDate);
            v1.setEndDate(toDate);
            v1.setUser(u1);
        }

        //user has 5 vacation days in this year remaining
        //new vacation lasts for 27 days, and starts at the latest at the 27th, so it should pass.
        vacationService.saveVacation(v1);
    }

    /**
     * Tests creating a vacation which passes new year, but user already has too many vacation days in the following year
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void create_vacation_new_year_invalid() throws ProdigaGeneralExpectedException
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Vacation v1 = new Vacation();

        //get somewhere in june of next year and make a 20 day vacation
        LocalDate startDate = LocalDate.of(LocalDate.now().getYear() + 1, 6, 10);
        LocalDate endDate = startDate.plusDays(20);
        DataHelper.createVacation(startDate, endDate, u1, vacationRepository);

        //7 day vacation, with 6 in the new year, but user only has 5 left
        startDate = LocalDate.of(LocalDate.now().getYear(), 12, 31);
        endDate = startDate.plusDays(7);


        Date fromDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        v1.setBeginDate(fromDate);
        v1.setEndDate(toDate);
        v1.setUser(u1);

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
    public void create_vacation_overlapping() throws ProdigaGeneralExpectedException
    {
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        DataHelper.createVacation(5,10, u1, vacationRepository);

        //vacation that overlaps with beginning date of v1
        Date fromDate1 = Date.from(LocalDate.now().plusDays(4).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate1 = Date.from(LocalDate.now().plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant());

        //vacation that overlaps with ending date of v1
        Date fromDate2 = Date.from(LocalDate.now().plusDays(9).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate2 = Date.from(LocalDate.now().plusDays(11).atStartOfDay(ZoneId.systemDefault()).toInstant());

        //vacation that completely covers v1
        Date fromDate3 = Date.from(LocalDate.now().plusDays(4).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate3 = Date.from(LocalDate.now().plusDays(12).atStartOfDay(ZoneId.systemDefault()).toInstant());

        //vacation that is completely covered by v1
        Date fromDate4 = Date.from(LocalDate.now().plusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate4 = Date.from(LocalDate.now().plusDays(8).atStartOfDay(ZoneId.systemDefault()).toInstant());


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
     * Tests validation on the date input with respect to existing vaations
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void create_vacation_on_booking_day() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d = DataHelper.createDice("testdice", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingType bt = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);
        DataHelper.createBooking(bt, Date.from(Instant.ofEpochMilli(new Date().getTime() + 1000 * 60 * 60 * 24 * 2)), Date.from(Instant.ofEpochMilli(new Date().getTime() + 1000 * 60 * 60 * 24 * 3)),  u1, d, bookingRepository);

        //vacation that overlaps with beginning date of v1
        Date fromDate1 = Date.from(LocalDate.now().plusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate1 = Date.from(LocalDate.now().plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());

        //None of these should pass
        Vacation v1 = new Vacation();
        v1.setBeginDate(fromDate1);
        v1.setEndDate(toDate1);
        v1.setUser(u1);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            vacationService.saveVacation(v1);
        }, "Vacation that intersects with a booking was saved successfully.");
    }

    /**
     * Tests creating a vacation for another user
     */
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void create_vacation_other_user() throws ProdigaGeneralExpectedException
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
    public void create_vacation_unauthorized() throws ProdigaGeneralExpectedException
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            vacationService.saveVacation(new Vacation());
        }, "Vacations created despite lacking authorization of EMPLOYEE");
    }
}
