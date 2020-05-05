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

    User admin = null;
    User u1 = null;
    User u2 = null;
    Dice d = null;
    BookingCategory cat1 = null;
    BookingCategory cat2 = null;

    @BeforeEach
    public void initEach(@Autowired UserRepository userRepository){
        admin = DataHelper.createAdminUser("admin", userRepository);
        admin.setMayEditHistoricData(true);
        admin = userRepository.save(admin);
        u1 = DataHelper.createUserWithRoles("statistic_test_user1", Sets.newSet(UserRole.EMPLOYEE),userRepository);
        u2 = DataHelper.createUserWithRoles("statistic_test_user2", Sets.newSet(UserRole.EMPLOYEE),userRepository);
        d = DataHelper.createDice("testdice", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        cat1 = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        cat2 = DataHelper.createBookingCategory("test_category_02", admin, bookingCategoryRepository);
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "statistic_test_user1", authorities = {"EMPLOYEE"})
    public void statistic_user_backstepDay1_1booking(){
        //end 1 hour ago
        Date endingTime1 = new Date(new Date().getTime() - 60*1000*60);
        //start 3 hours ago
        Date startingTime1 = new Date(new Date().getTime() - 60*1000*180);
        Booking booking1 = DataHelper.createBooking(cat1,startingTime1,endingTime1,u1,d,bookingRepository);
        //end 25 days ago
        Date endingTime2 = new Date(new Date().getTime() - 60*1000*60*25);
        //start 50 days ago
        Date startingTime2 = new Date(new Date().getTime() - 60*1000*60*30);
        Booking booking2 = DataHelper.createBooking(cat2,startingTime2,endingTime2,u1,d,bookingRepository);

        HashMap<BookingCategory,Long> dailyStatistic = new HashMap<>();
        dailyStatistic.put(cat1, (endingTime1.getTime()-startingTime1.getTime()) / (1000*60*60));
        Assertions.assertEquals(dailyStatistic, productivityAnalysisService.getStatisicForCurrentUserByDay(1));

    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "statistic_test_user1", authorities = {"EMPLOYEE"})
    public void statistic_user_backstepDay1_2bookings(){
        //set activity end time to 1 hour before current time.
        Date endingTime1 = new Date(new Date().getTime() - 60*1000*60);
        //set activity start time to 3 hours ago
        Date startingTime1 = new Date(new Date().getTime() - 60*1000*180);
        Booking booking1 = DataHelper.createBooking(cat1,startingTime1,endingTime1,u1,d,bookingRepository);

        //set activity end time to 5 minutes before current time.
        Date endingTime2 = new Date(new Date().getTime() - 60*1000*5);
        //set activity start time to 30 minutes ago
        Date startingTime2 = new Date(new Date().getTime() - 60*1000*30);
        Booking booking2 = DataHelper.createBooking(cat2,startingTime2,endingTime2,u1,d,bookingRepository);
        HashMap<BookingCategory,Long> dailyStatistic = new HashMap<>();
        dailyStatistic.put(cat1, (endingTime1.getTime()-startingTime1.getTime()) / (1000*60*60));
        dailyStatistic.put(cat2, (endingTime2.getTime()-startingTime2.getTime()) / (1000*60*60));
        Assertions.assertEquals(dailyStatistic, productivityAnalysisService.getStatisicForCurrentUserByDay(1));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "statistic_test_user1", authorities = {"EMPLOYEE"})
    public void statistic_user_backstepDays(){
        //set activity end time to 25 hours before current time.
        Date endingTime1 = new Date(new Date().getTime() - 60*1000*60*25);
        //set activity start time to 30 hours ago
        Date startingTime1 = new Date(new Date().getTime() - 60*1000*60*30);
        Booking booking1 = DataHelper.createBooking(cat1,startingTime1,endingTime1,u1,d,bookingRepository);

        //set activity end time to 5 minutes before current time.
        Date endingTime2 = new Date(new Date().getTime() - 60*1000*5);
        //set activity start time to 30 minutes ago
        Date startingTime2 = new Date(new Date().getTime() - 60*1000*30);
        Booking booking2 = DataHelper.createBooking(cat2,startingTime2, endingTime2,u1,d,bookingRepository);

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
        //set activity end time to 3 days 22 hours days before current time.
        Date endingTime1 = new Date(new Date().getTime() - 60*1000*60*24*4 + 60*1000*180);
        //set activity start time to 4 days ago
        Date startingTime1 = new Date(new Date().getTime() - 60*1000*60*24*4);
        Booking booking1 = DataHelper.createBooking(cat1,startingTime1,endingTime1,u1,d,bookingRepository);

        HashMap<BookingCategory,Long> thisWeekStatistic = new HashMap<>();
        thisWeekStatistic.put(cat1, (endingTime1.getTime()-startingTime1.getTime()) / (1000*60*60));
        Assertions.assertEquals(thisWeekStatistic, productivityAnalysisService.getStatisicForCurrentUserByWeek(1));
    }

}