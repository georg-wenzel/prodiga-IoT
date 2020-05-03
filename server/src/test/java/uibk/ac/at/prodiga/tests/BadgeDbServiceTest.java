package uibk.ac.at.prodiga.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.repositories.*;
import uibk.ac.at.prodiga.services.*;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.utils.badge.Badge;
import uibk.ac.at.prodiga.utils.badge.Bugsimilian;

import javax.persistence.Table;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class BadgeDbServiceTest {

    @Autowired
    BadgeDBService badgeDBService;

    @Autowired
    BadgeDBRepository badgeDBRepository;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;


    /**
     * Test for loading batches.
     */
    @Test
    public void load_badge_data(){

        User admin = DataHelper.createAdminUser("admin", userRepository);

        BadgeDB badge = DataHelper.createRandomBadge(admin, badgeDBRepository);
        BadgeDB badgeDB = this.badgeDBService.getFirstByBadgeName(badge.getBadgeName());

        Assertions.assertNotNull(badgeDB, "Service returned object does not match DB state");
        Assertions.assertEquals(badgeDB.getBadgeName(), badge.getBadgeName(), "DB Badge has different name");
        Assertions.assertEquals(badgeDB.getUser(), badgeDB.getUser(), "DB Badge has different user");
        Assertions.assertEquals(badgeDB.getId(), badgeDB.getId(), "DB Badge has different id");
        Assertions.assertEquals(badgeDB.getFromDate(), badgeDB.getFromDate(), "DB Badge has different fromDate");
        Assertions.assertEquals(badgeDB.getToDate(), badgeDB.getToDate(), "DB Badge has different toDate");
    }

    /**
     * Testing the scheduler for creating badges
     * https://stackoverflow.com/questions/17327956/testing-scheduled-in-spring
     */
    @Test
    public void test_scheduler(){
        CronTrigger trigger = new CronTrigger("0 59 23 * * SUN");
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        Calendar nextWeek = Calendar.getInstance();
        nextWeek.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
        nextWeek.set(Calendar.HOUR_OF_DAY,0);
        nextWeek.set(Calendar.MINUTE,0);
        nextWeek.set(Calendar.SECOND,0);
        nextWeek.add(Calendar.DATE,7);

        final Date yesterday = today.getTime();

        Date nextWeekTest = trigger.nextExecutionTime(
                new TriggerContext() {

                    @Override
                    public Date lastScheduledExecutionTime() {
                        return yesterday;
                    }

                    @Override
                    public Date lastActualExecutionTime() {
                        return yesterday;
                    }

                    @Override
                    public Date lastCompletionTime() {
                        return yesterday;
                    }
                });

        Assertions.assertNotEquals(nextWeek, nextWeekTest);

    }

    /**
     * Test for getting badges by user.
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"EMPLOYEE", "TEAMLEADER", "DEPARTMENTLEADER"})
    public void badges_by_user_authorized(){
        User testUser = DataHelper.createRandomUser(userRepository);
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            Collection<BadgeDB> badgeDBS = badgeDBService.getAllBadgesByUser(testUser);
        }, "Users Badges were able to get despite lacking authorizations.");

    }

    /**
     * Test for creating badges.
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"ADMIN"})
    public void create_badges(){
        User testUser1 = DataHelper.createRandomUser(userRepository);
        User testUser2 = DataHelper.createRandomUser(userRepository);

        Badge badge1 = new Badge() {
            @Override
            public User calculateUser(Collection<BookingCategory> bookingCategories, BookingService bookingService) {
                return testUser1;
            }

            @Override
            public String getName() {
                return "testBadge1";
            }

            @Override
            public String getExplanation() {
                return null;
            }
        };

        Badge badge2 = new Badge() {
            @Override
            public User calculateUser(Collection<BookingCategory> bookingCategories, BookingService bookingService) {
                return testUser2;
            }

            @Override
            public String getName() {
                return "testBadge2";
            }

            @Override
            public String getExplanation() {
                return null;
            }
        };

        badgeDBService.registerBadges(badge1);
        badgeDBService.registerBadges(badge2);

        badgeDBService.createBadges();

        Collection<BadgeDB> badgeDBSTestUser1 = badgeDBService.getAllBadgesByUser(testUser1);
        Collection<BadgeDB> badgeDBSTestUser2 = badgeDBService.getAllBadgesByUser(testUser2);

        BadgeDB badgeDBTest1 = new BadgeDB();
        badgeDBTest1.setBadgeName("testBadge1");
        badgeDBTest1.setUser(testUser1);

        BadgeDB badgeDBTest2 = new BadgeDB();
        badgeDBTest2.setBadgeName("testBadge2");
        badgeDBTest2.setUser(testUser2);

        BadgeDB badgeDBTest1DB = badgeDBSTestUser1.stream().findFirst().orElse(null);
        BadgeDB badgeDBTest2DB = badgeDBSTestUser2.stream().findFirst().orElse(null);


        Assertions.assertNotNull(badgeDBService.getAllBadgesByUser(testUser1), "Could not load list of badges");
        Assertions.assertNotNull(badgeDBService.getAllBadgesByUser(testUser2), "Could not load list of badges");
        Assertions.assertEquals(badgeDBTest1.getBadgeName(), badgeDBTest1DB.getBadgeName(), "DB Badge does not have the same name");
        Assertions.assertEquals(badgeDBTest2.getBadgeName(), badgeDBTest2DB.getBadgeName(), "DB Badge does not have the same name");
        Assertions.assertEquals(badgeDBTest1.getUser(), badgeDBTest1DB.getUser(), "DB Badge does not have the same user");
        Assertions.assertEquals(badgeDBTest2.getUser(), badgeDBTest2DB.getUser(), "DB Badge does not have the same user");


    }
}
