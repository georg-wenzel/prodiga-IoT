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

import java.time.LocalDate;
import java.time.ZoneId;
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
    User u3 = null;
    Dice d1 = null;
    Dice d2 = null;
    Dice d3 = null;
    Department dep = null;
    Team team1 = null;
    Team team2 = null;
    BookingCategory cat1 = null;
    BookingCategory cat2 = null;

    @BeforeEach
    public void initEach(@Autowired UserRepository userRepository){
        admin = DataHelper.createAdminUser("admin", userRepository);
        admin.setMayEditHistoricData(true);
        admin = userRepository.save(admin);
        dep = DataHelper.createRandomDepartment(admin,departmentRepository);
        team1 = DataHelper.createRandomTeam(dep,admin,teamRepository);
        team2 = DataHelper.createRandomTeam(dep,admin,teamRepository);
        u1 = DataHelper.createUserWithRoles("statistic_test_user1", Sets.newSet(UserRole.EMPLOYEE, UserRole.TEAMLEADER),admin,dep,team1,userRepository);
        u2 = DataHelper.createUserWithRoles("statistic_test_user2", Sets.newSet(UserRole.EMPLOYEE, UserRole.DEPARTMENTLEADER),admin,dep,team1,userRepository);
        u3 = DataHelper.createUserWithRoles("statistic_test_user3", Sets.newSet(UserRole.EMPLOYEE),admin,dep,team2,userRepository);
        d1 = DataHelper.createDice("testdice1", null, admin, u1, diceRepository, raspberryPiRepository, roomRepository);
        d2 = DataHelper.createDice("testdice2", null, admin, u2, diceRepository, raspberryPiRepository, roomRepository);
        d3 = DataHelper.createDice("testdice3", null, admin, u3, diceRepository, raspberryPiRepository, roomRepository);
        cat1 = DataHelper.createBookingCategory("test_category_01", admin, bookingCategoryRepository);
        cat2 = DataHelper.createBookingCategory("test_category_02", admin, bookingCategoryRepository);
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "statistic_test_user1", authorities = {"EMPLOYEE"})
    public void statistic_user_backstepDay1_1booking(){
        Date endTime1 = Date.from(LocalDate.now().minusDays(1).atTime(11,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Date startTime1 = Date.from(LocalDate.now().minusDays(1).atTime(10,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Booking booking1 = DataHelper.createBooking(cat1,startTime1,endTime1,u1,d1,bookingRepository);

        Date endTime2 = Date.from(LocalDate.now().minusDays(2).atTime(11,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Date startTime2 = Date.from(LocalDate.now().minusDays(2).atTime(10,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Booking booking2 = DataHelper.createBooking(cat2,startTime2,endTime2,u1,d1,bookingRepository);

        HashMap<BookingCategory,Long> dailyStatistic = new HashMap<>();
        dailyStatistic.put(cat1, (endTime1.getTime()-startTime1.getTime()) / (1000*60*60));
        Assertions.assertEquals(dailyStatistic, productivityAnalysisService.getStatisicForCurrentUserByDay(1));

    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "statistic_test_user1", authorities = {"EMPLOYEE"})
    public void statistic_user_backstepDay1_2bookings()
    {
        Date endTime1 = Date.from(LocalDate.now().minusDays(1).atTime(11,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Date startTime1 = Date.from(LocalDate.now().minusDays(1).atTime(10,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Booking booking1 = DataHelper.createBooking(cat1,startTime1,endTime1,u1,d1,bookingRepository);

        Date endTime2 = Date.from(LocalDate.now().minusDays(1).atTime(10,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Date startTime2 = Date.from(LocalDate.now().minusDays(1).atTime(9,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Booking booking2 = DataHelper.createBooking(cat2,startTime2,endTime2,u1,d1,bookingRepository);

        HashMap<BookingCategory,Long> dailyStatistic = new HashMap<>();
        dailyStatistic.put(cat1, (endTime1.getTime()-startTime1.getTime()) / (1000*60*60));
        dailyStatistic.put(cat2, (endTime2.getTime()-startTime2.getTime()) / (1000*60*60));
        Assertions.assertEquals(dailyStatistic, productivityAnalysisService.getStatisicForCurrentUserByDay(1));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "statistic_test_user1", authorities = {"EMPLOYEE"})
    public void statistic_user_backstepDays(){
        Date endTime1 = Date.from(LocalDate.now().minusDays(1).atTime(11,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Date startTime1 = Date.from(LocalDate.now().minusDays(1).atTime(10,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Booking booking1 = DataHelper.createBooking(cat1,startTime1,endTime1,u1,d1,bookingRepository);

        Date endTime2 = Date.from(LocalDate.now().minusDays(2).atTime(11,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Date startTime2 = Date.from(LocalDate.now().minusDays(2).atTime(10,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Booking booking2 = DataHelper.createBooking(cat2,startTime2,endTime2,u1,d1,bookingRepository);

        Date endTime3 = Date.from(LocalDate.now().minusDays(3).atTime(11,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Date startTime3 = Date.from(LocalDate.now().minusDays(3).atTime(10,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Booking booking3 = DataHelper.createBooking(cat2,startTime3,endTime3,u1,d1,bookingRepository);

        HashMap<BookingCategory,Long> backstep1Statistic = new HashMap<>();
        backstep1Statistic.put(cat1, (endTime2.getTime()-startTime2.getTime()) / (1000*60*60));
        HashMap<BookingCategory,Long> backstep2Statistic = new HashMap<>();
        backstep2Statistic.put(cat2, (endTime1.getTime()-startTime1.getTime()) / (1000*60*60));

        Assertions.assertEquals(backstep1Statistic,productivityAnalysisService.getStatisicForCurrentUserByDay(1));
        Assertions.assertEquals(backstep2Statistic, productivityAnalysisService.getStatisicForCurrentUserByDay(2));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "statistic_test_user1", authorities = {"EMPLOYEE"})
    public void statistic_user_backstepWeek(){
        Date endTime1 = Date.from(LocalDate.now().minusDays(7).atTime(11,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Date startTime1 = Date.from(LocalDate.now().minusDays(7).atTime(10,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Booking booking1 = DataHelper.createBooking(cat1,startTime1,endTime1,u1,d1,bookingRepository);

        Date endTime2 = Date.from(LocalDate.now().minusDays(14).atTime(11,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Date startTime2 = Date.from(LocalDate.now().minusDays(14).atTime(10,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Booking booking2 = DataHelper.createBooking(cat1,endTime2,endTime2,u1,d1,bookingRepository);

        HashMap<BookingCategory,Long> thisWeekStatistic = new HashMap<>();
        thisWeekStatistic.put(cat1, (endTime1.getTime()-startTime1.getTime()) / (1000*60*60));
        Assertions.assertEquals(thisWeekStatistic, productivityAnalysisService.getStatisicForCurrentUserByWeek(1));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "statistic_test_user1", authorities = {"EMPLOYEE"})
    public void statistic_user_backstepMonth(){
        Date endTime1, startTime1, endTime2, startTime2;

        //make sure month matches 1 month ago
        if(LocalDate.now().minusMonths(1).minusDays(7).getMonth() == LocalDate.now().minusMonths(1).getMonth()) {
            endTime1 = Date.from(LocalDate.now().minusMonths(1).minusDays(7).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime1 = Date.from(LocalDate.now().minusMonths(1).minusDays(7).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        else
        {
            endTime1 = Date.from(LocalDate.now().minusMonths(1).plusDays(7).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime1 = Date.from(LocalDate.now().minusMonths(1).plusDays(7).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        Booking booking1 = DataHelper.createBooking(cat1,startTime1,endTime1,u1,d1,bookingRepository);

        //make sure month matches 1 month ago
        if(LocalDate.now().minusMonths(1).minusDays(10).getMonth() == LocalDate.now().minusMonths(1).getMonth()) {
            endTime2 = Date.from(LocalDate.now().minusMonths(1).minusDays(10).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime2 = Date.from(LocalDate.now().minusMonths(1).minusDays(10).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        else
        {
            endTime2 = Date.from(LocalDate.now().plusDays(10).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime2 = Date.from(LocalDate.now().plusDays(10).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        Booking booking2 = DataHelper.createBooking(cat2,startTime2,endTime2,u1,d1,bookingRepository);

        Date endTime3 = Date.from(LocalDate.now().minusMonths(2).atTime(11,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Date startTime3 = Date.from(LocalDate.now().minusMonths(2).atTime(5,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Booking booking3 = DataHelper.createBooking(cat1, startTime3, endTime3, u1, d1, bookingRepository);

        HashMap<BookingCategory,Long> prevMonthStatistics = new HashMap<>();
        prevMonthStatistics.put(cat1, (endTime1.getTime()-startTime1.getTime()) / (1000*60*60));
        prevMonthStatistics.put(cat2, (endTime2.getTime()-startTime2.getTime()) /(1000*60*60));

        HashMap<BookingCategory,Long> twoMonthStatistic = new HashMap<>();
        twoMonthStatistic.put(cat1, (endTime3.getTime()-startTime3.getTime()) / (1000*60*60));

        Assertions.assertEquals(prevMonthStatistics, productivityAnalysisService.getStatisicForCurrentUserByMonth(1));
        Assertions.assertEquals(twoMonthStatistic, productivityAnalysisService.getStatisicForCurrentUserByMonth(2));
    }


    @DirtiesContext
    @Test
    @WithMockUser(username = "statistic_test_user1", authorities = {"TEAMLEADER"})
    public void statistic_team_backstepWeek()
    {
        Date endTime1 = Date.from(LocalDate.now().minusDays(7).atTime(11,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Date startTime1 = Date.from(LocalDate.now().minusDays(7).atTime(10,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Booking booking1 = DataHelper.createBooking(cat1,startTime1,endTime1,u1,d1,bookingRepository);

        Date endTime2 = Date.from(LocalDate.now().minusDays(7).atTime(11,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Date startTime2 = Date.from(LocalDate.now().minusDays(7).atTime(10,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Booking booking2 = DataHelper.createBooking(cat2,startTime2,endTime2,u2,d2,bookingRepository);

        Date endTime3 = Date.from(LocalDate.now().minusDays(7).atTime(11,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Date startTime3 = Date.from(LocalDate.now().minusDays(7).atTime(10,0,0).atZone(ZoneId.systemDefault()).toInstant());
        Booking booking3 = DataHelper.createBooking(cat2,startTime3,endTime3,u3,d3,bookingRepository);

        HashMap<BookingCategory,Long> teamWeekStatistic = new HashMap<>();
        teamWeekStatistic.put(cat1, (endTime1.getTime()-startTime1.getTime()) / (1000*60*60));
        teamWeekStatistic.put(cat2, (endTime2.getTime()-startTime2.getTime()) /(1000*60*60));
        Assertions.assertEquals(teamWeekStatistic, productivityAnalysisService.getStatisicForTeamByWeek(1));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "statistic_test_user1", authorities = {"TEAMLEADER"})
    public void statistic_team_backstepMonth(){

        Date endTime1, endTime2, endTime3, startTime1, startTime2, startTime3;

        //make sure month matches 1 month ago
        if(LocalDate.now().minusMonths(1).minusDays(7).getMonth() == LocalDate.now().minusMonths(1).getMonth()) {
            endTime1 = Date.from(LocalDate.now().minusMonths(1).minusDays(7).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime1 = Date.from(LocalDate.now().minusMonths(1).minusDays(7).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        else
        {
            endTime1 = Date.from(LocalDate.now().minusMonths(1).plusDays(7).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime1 = Date.from(LocalDate.now().minusMonths(1).plusDays(7).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        Booking booking1 = DataHelper.createBooking(cat1,startTime1,endTime1,u1,d1,bookingRepository);

        //make sure month matches 1 month ago
        if(LocalDate.now().minusMonths(1).minusDays(10).getMonth() == LocalDate.now().minusMonths(1).getMonth()) {
            endTime2 = Date.from(LocalDate.now().minusMonths(1).minusDays(10).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime2 = Date.from(LocalDate.now().minusMonths(1).minusDays(10).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        else
        {
            endTime2 = Date.from(LocalDate.now().plusDays(10).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime2 = Date.from(LocalDate.now().plusDays(10).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        Booking booking2 = DataHelper.createBooking(cat2,startTime2,endTime2,u2,d2,bookingRepository);

        //make sure month matches 1 month ago
        if(LocalDate.now().minusMonths(1).minusDays(5).getMonth() == LocalDate.now().minusMonths(1).getMonth()) {
            endTime3 = Date.from(LocalDate.now().minusMonths(1).minusDays(5).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime3 = Date.from(LocalDate.now().minusMonths(1).minusDays(5).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        else
        {
            endTime3 = Date.from(LocalDate.now().plusDays(5).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime3 = Date.from(LocalDate.now().plusDays(5).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        Booking booking3 = DataHelper.createBooking(cat1,startTime3,endTime3,u3,d3,bookingRepository);

        HashMap<BookingCategory,Long> thisMonthStatistic = new HashMap<>();
        thisMonthStatistic.put(cat1, (endTime1.getTime()-startTime1.getTime()) / (1000*60*60));
        Assertions.assertEquals(thisMonthStatistic, productivityAnalysisService.getStatisicForCurrentUserByMonth(1));
        thisMonthStatistic.put(cat2, (endTime2.getTime()-startTime2.getTime()) /(1000*60*60));
        Assertions.assertEquals(thisMonthStatistic, productivityAnalysisService.getStatisicForTeamByMonth(1));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "statistic_test_user2", authorities = {"DEPARTMENTLEADER"})
    public void statistic_department_backstepMonth()
    {
        Date endTime1, endTime2, endTime3, startTime1, startTime2, startTime3;

        //make sure month matches 1 month ago
        if(LocalDate.now().minusMonths(1).minusDays(7).getMonth() == LocalDate.now().minusMonths(1).getMonth()) {
            endTime1 = Date.from(LocalDate.now().minusMonths(1).minusDays(7).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime1 = Date.from(LocalDate.now().minusMonths(1).minusDays(7).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        else
        {
            endTime1 = Date.from(LocalDate.now().minusMonths(1).plusDays(7).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime1 = Date.from(LocalDate.now().minusMonths(1).plusDays(7).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        Booking booking1 = DataHelper.createBooking(cat1,startTime1,endTime1,u1,d1,bookingRepository);

        //make sure month matches 1 month ago
        if(LocalDate.now().minusMonths(1).minusDays(10).getMonth() == LocalDate.now().minusMonths(1).getMonth()) {
            endTime2 = Date.from(LocalDate.now().minusMonths(1).minusDays(10).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime2 = Date.from(LocalDate.now().minusMonths(1).minusDays(10).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        else
        {
            endTime2 = Date.from(LocalDate.now().plusDays(10).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime2 = Date.from(LocalDate.now().plusDays(10).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        Booking booking2 = DataHelper.createBooking(cat2,startTime2,endTime2,u2,d2,bookingRepository);

        //make sure month matches 1 month ago
        if(LocalDate.now().minusMonths(1).minusDays(5).getMonth() == LocalDate.now().minusMonths(1).getMonth()) {
            endTime3 = Date.from(LocalDate.now().minusMonths(1).minusDays(5).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime3 = Date.from(LocalDate.now().minusMonths(1).minusDays(5).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        else
        {
            endTime3 = Date.from(LocalDate.now().plusDays(5).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime3 = Date.from(LocalDate.now().plusDays(5).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        Booking booking3 = DataHelper.createBooking(cat2,startTime3,endTime3,u3,d3,bookingRepository);

        HashMap<BookingCategory,Long> backstep1MonthStatistic = new HashMap<>();
        backstep1MonthStatistic.put(cat1, (endTime1.getTime()-startTime1.getTime()) / (1000*60*60));
        backstep1MonthStatistic.put(cat2, ((endTime2.getTime()-startTime2.getTime()) + (endTime3.getTime()-startTime3.getTime())) /(1000*60*60));
        Assertions.assertEquals(backstep1MonthStatistic, productivityAnalysisService.getStatisicForDepartmenByMonth(1));

    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void statistic_departmentAsAdmin_backstepMonth(){
        Date endTime1, endTime2, endTime3, startTime1, startTime2, startTime3;

        //make sure month matches 1 month ago
        if(LocalDate.now().minusMonths(1).minusDays(7).getMonth() == LocalDate.now().minusMonths(1).getMonth()) {
            endTime1 = Date.from(LocalDate.now().minusMonths(1).minusDays(7).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime1 = Date.from(LocalDate.now().minusMonths(1).minusDays(7).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        else
        {
            endTime1 = Date.from(LocalDate.now().minusMonths(1).plusDays(7).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime1 = Date.from(LocalDate.now().minusMonths(1).plusDays(7).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        Booking booking1 = DataHelper.createBooking(cat1,startTime1,endTime1,u1,d1,bookingRepository);

        //make sure month matches 1 month ago
        if(LocalDate.now().minusMonths(1).minusDays(10).getMonth() == LocalDate.now().minusMonths(1).getMonth()) {
            endTime2 = Date.from(LocalDate.now().minusMonths(1).minusDays(10).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime2 = Date.from(LocalDate.now().minusMonths(1).minusDays(10).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        else
        {
            endTime2 = Date.from(LocalDate.now().plusDays(10).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime2 = Date.from(LocalDate.now().plusDays(10).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        Booking booking2 = DataHelper.createBooking(cat2,startTime2,endTime2,u2,d2,bookingRepository);

        //make sure month matches 1 month ago
        if(LocalDate.now().minusMonths(1).minusDays(5).getMonth() == LocalDate.now().minusMonths(1).getMonth()) {
            endTime3 = Date.from(LocalDate.now().minusMonths(1).minusDays(5).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime3 = Date.from(LocalDate.now().minusMonths(1).minusDays(5).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        else
        {
            endTime3 = Date.from(LocalDate.now().plusDays(5).atTime(11, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            startTime3 = Date.from(LocalDate.now().plusDays(5).atTime(10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        }
        Booking booking3 = DataHelper.createBooking(cat2,startTime3,endTime3,u3,d3,bookingRepository);

        HashMap<BookingCategory,Long> backstep1MonthStatistic = new HashMap<>();
        backstep1MonthStatistic.put(cat1, (endTime1.getTime()-startTime1.getTime()) / (1000*60*60));
        backstep1MonthStatistic.put(cat2, ((endTime2.getTime()-startTime2.getTime()) + (endTime3.getTime()-startTime3.getTime())) /(1000*60*60));

        Assertions.assertEquals(backstep1MonthStatistic, productivityAnalysisService.getStatisicForDepartmenByMonth(1));

    }
}