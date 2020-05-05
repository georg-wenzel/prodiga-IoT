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
import uibk.ac.at.prodiga.services.ProductivityAnalysisService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class ProductivityAnalysisServiceTest {

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
    BookingCategoryRepository bookingCategoryRepository;

    @Autowired
    RaspberryPiRepository raspberryPiRepository;

    @Autowired
    BookingService bookingService;

    @Autowired
    ProdigaUserLoginManager prodigaUserLoginManager;

    @Autowired
    ProductivityAnalysisService productivityAnalysisService;

    @DirtiesContext
    @Test
    @WithMockUser(username = "statistic_test_user1", authorities = {"EMPLOYEE"})
    public void statistic_user_backstepDay1_1booking(){
        //Data setup
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("statistic_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d = DataHelper.createDice("testdice", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        BookingCategory cat2 = DataHelper.createBookingCategory("test_category_02", admin, bookingCategoryRepository);

        Booking b1 = new Booking();
        b1.setDice(d);
        b1.setBookingCategory(cat);
        //set activity end time to 1 hour before current time.
        Date endingTime = new Date(new Date().getTime() - 60*1000*60);
        b1.setActivityEndDate(endingTime);
        //set activity start time to 3 hours ago
        Date startingTime = new Date(new Date().getTime() - 60*1000*180);
        b1.setActivityStartDate(startingTime);
        try {
            b1 = bookingService.saveBooking(b1);
        } catch (ProdigaGeneralExpectedException e) {
            e.printStackTrace();
        }

        Booking b2 = new Booking();
        b2.setDice(d);
        b2.setBookingCategory(cat2);
        //set activity end time to 25 hours before current time.
        Date endingTime1 = new Date(new Date().getTime() - 60*1000*60*25);
        b2.setActivityEndDate(endingTime1);
        //set activity start time to 30 hours ago
        Date startingTime1 = new Date(new Date().getTime() - 60*1000*60*30);
        b2.setActivityStartDate(startingTime1);
        try {
            b2 = bookingService.saveBooking(b2);
        } catch (ProdigaGeneralExpectedException e) {
            e.printStackTrace();
        }
        HashMap<BookingCategory,Long> dailyStatistic = new HashMap<>();
        dailyStatistic.put(cat, (endingTime.getTime()-startingTime.getTime()) / (1000*60*60));
        Assertions.assertEquals(dailyStatistic, productivityAnalysisService.getStatisicForCurrentUserByDay(1));

    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "statistic_test_user1", authorities = {"EMPLOYEE"})
    public void statistic_user_backstepDay1_2bookings(){
        //Data setup
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("statistic_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d = DataHelper.createDice("testdice", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat1 = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        BookingCategory cat2 = DataHelper.createBookingCategory("test_category_02", admin, bookingCategoryRepository);

        Booking b1 = new Booking();
        b1.setDice(d);
        b1.setBookingCategory(cat1);
        //set activity end time to 1 hour before current time.
        Date endingTime1 = new Date(new Date().getTime() - 60*1000*60);
        b1.setActivityEndDate(endingTime1);
        //set activity start time to 3 hours ago
        Date startingTime1 = new Date(new Date().getTime() - 60*1000*180);
        b1.setActivityStartDate(startingTime1);
        try {
            b1 = bookingService.saveBooking(b1);
        } catch (ProdigaGeneralExpectedException e) {
            e.printStackTrace();
        }
        Booking b2 = new Booking();
        b2.setDice(d);
        b2.setBookingCategory(cat2);
        //set activity end time to 5 minutes before current time.
        Date endingTime2 = new Date(new Date().getTime() - 60*1000*5);
        b2.setActivityEndDate(endingTime2);
        //set activity start time to 30 minutes ago
        Date startingTime2 = new Date(new Date().getTime() - 60*1000*30);
        b2.setActivityStartDate(startingTime2);
        try {
            b2 = bookingService.saveBooking(b2);
        } catch (ProdigaGeneralExpectedException e) {
            e.printStackTrace();
        }
        HashMap<BookingCategory,Long> dailyStatistic = new HashMap<>();
        dailyStatistic.put(cat1, (endingTime1.getTime()-startingTime1.getTime()) / (1000*60*60));
        dailyStatistic.put(cat2, (endingTime2.getTime()-startingTime2.getTime()) / (1000*60*60));
        Assertions.assertEquals(dailyStatistic, productivityAnalysisService.getStatisicForCurrentUserByDay(1));

    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "statistic_test_user1", authorities = {"EMPLOYEE"})
    public void statistic_user_backstepDays(){
        //Data setup
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("statistic_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d = DataHelper.createDice("testdice", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat1 = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        BookingCategory cat2 = DataHelper.createBookingCategory("test_category_02", admin, bookingCategoryRepository);

        Booking b1 = new Booking();
        b1.setDice(d);
        b1.setBookingCategory(cat1);
        //set activity end time to 25 hours before current time.
        Date endingTime1 = new Date(new Date().getTime() - 60*1000*60*25);
        b1.setActivityEndDate(endingTime1);
        //set activity start time to 30 hours ago
        Date startingTime1 = new Date(new Date().getTime() - 60*1000*60*30);
        b1.setActivityStartDate(startingTime1);
        try {
            b1 = bookingService.saveBooking(b1);
        } catch (ProdigaGeneralExpectedException e) {
            e.printStackTrace();
        }

        Booking b2 = new Booking();
        b2.setDice(d);
        b2.setBookingCategory(cat2);
        //set activity end time to 5 minutes before current time.
        Date endingTime2 = new Date(new Date().getTime() - 60*1000*5);
        b2.setActivityEndDate(endingTime2);
        //set activity start time to 30 minutes ago
        Date startingTime2 = new Date(new Date().getTime() - 60*1000*30);
        b2.setActivityStartDate(startingTime2);
        try {
            b2 = bookingService.saveBooking(b2);
        } catch (ProdigaGeneralExpectedException e) {
            e.printStackTrace();
        }
        HashMap<BookingCategory,Long> backstep1Statistic = new HashMap<>();
        backstep1Statistic.put(cat2, (endingTime2.getTime()-startingTime2.getTime()) / (1000*60*60));
        HashMap<BookingCategory,Long> backstep2Statistic = new HashMap<>();
        backstep2Statistic.put(cat1, (endingTime1.getTime()-startingTime1.getTime()) / (1000*60*60));

        Assertions.assertEquals(backstep1Statistic,productivityAnalysisService.getStatisicForCurrentUserByDay(1));
        Assertions.assertEquals(backstep2Statistic, productivityAnalysisService.getStatisicForCurrentUserByDay(2));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "statistic_test_user1", authorities = {"EMPLOYEE"})
    public void statistic_user_backstepWeek(){
        //Data setup
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("statistic_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Dice d = DataHelper.createDice("testdice", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        BookingCategory cat1 = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);

        Booking b1 = new Booking();
        b1.setDice(d);
        b1.setBookingCategory(cat1);
        //set activity end time to 3 days 22 hours days before current time.
        Date endingTime1 = new Date(new Date().getTime() - 60*1000*60*24*4 + 60*1000*180);
        b1.setActivityEndDate(endingTime1);
        //set activity start time to 4 days ago
        Date startingTime1 = new Date(new Date().getTime() - 60*1000*60*24*4);
        b1.setActivityStartDate(startingTime1);
        try {
            b1 = bookingService.saveBooking(b1);
        } catch (ProdigaGeneralExpectedException e) {
            e.printStackTrace();
        }

        HashMap<BookingCategory,Long> thisWeekStatistic = new HashMap<>();
        thisWeekStatistic.put(cat1, (endingTime1.getTime()-startingTime1.getTime()) / (1000*60*60));
        Assertions.assertEquals(thisWeekStatistic, productivityAnalysisService.getStatisicForCurrentUserByWeek(1));
    }

}