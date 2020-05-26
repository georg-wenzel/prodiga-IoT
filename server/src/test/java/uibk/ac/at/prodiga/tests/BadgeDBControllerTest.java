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
import uibk.ac.at.prodiga.ui.controllers.BadgeDBController;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.*;

/**
 * Tests proper implementation of the BadgeDB Controller
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class BadgeDBControllerTest {

    @Autowired
    BadgeDBService badgeDBService;

    @Autowired
    ProdigaUserLoginManager prodigaUserLoginManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BadgeDBRepository badgeDBRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    User admin;
    User employee;
    BadgeDB badgeDB;
    BadgeDBController controller;

    @BeforeEach
    public void init_each()
    {
        admin = DataHelper.createAdminUser("admin", userRepository);
        employee = DataHelper.createUserWithRoles("badgedb_test_user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        badgeDB = DataHelper.createRandomBadge(employee, badgeDBRepository);
        controller = new BadgeDBController(badgeDBService, prodigaUserLoginManager);
    }

    /**
     * Tests if controller properly returns the badges for a given user.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "test_admin", authorities = {"ADMIN"})
    public void badges_by_user() {
        // Tests for current user
        User user = DataHelper.createUserWithRoles("test_admin", Sets.newSet(UserRole.ADMIN), userRepository);
        BadgeDB badgeDB1 = DataHelper.createRandomBadge(user, badgeDBRepository);
        Collection<BadgeDB> badgeDBS1 = controller.getBadgesByUser();
        Assertions.assertEquals(badgeDB1.getBadgeName(), badgeDBS1.iterator().next().getBadgeName(), "Different Badge in Repository");
        badgeDBRepository.delete(badgeDB1);
        Collection<BadgeDB> badgeDBS2 = controller.getBadgesByUser();
        Assertions.assertEquals(new ArrayList<>(),badgeDBS2, "User should not have any Badges");

        // Tests fur certain user
        Collection<BadgeDB> badgeDBS3 = controller.getBadgesByUser(employee);
        Assertions.assertEquals(badgeDB.getBadgeName(), badgeDBS3.iterator().next().getBadgeName(), "Different Badge in Repository");
        badgeDBRepository.delete(badgeDB);
        Collection<BadgeDB> badgeDBS4 = controller.getBadgesByUser(employee);
        Assertions.assertEquals(new ArrayList<>(), badgeDBS4, "User should not have any Badges");
    }

    /**
     * Tests if controller properly returns the number of badges for a given user.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "test_admin", authorities = {"ADMIN"})
    public void badges_by_user_num() {
        User user = DataHelper.createUserWithRoles("test_admin", Sets.newSet(UserRole.ADMIN), userRepository);
        BadgeDB badgeDB1 = DataHelper.createRandomBadge(user, badgeDBRepository);
        BadgeDB badgeDB2 = DataHelper.createRandomBadge(user, badgeDBRepository);
        int numBadges1 = controller.getBadgesByUserNum();
        Assertions.assertEquals(2,numBadges1, "User should have exactly 3 badges.");
        badgeDBRepository.delete(badgeDB1);
        badgeDBRepository.delete(badgeDB2);
        int numBadges2 = controller.getBadgesByUserNum();
        Assertions.assertEquals(0, numBadges2, "User should not have any Badges");

        BadgeDB badgeDB3 = DataHelper.createRandomBadge(employee, badgeDBRepository);
        BadgeDB badgeDB4 = DataHelper.createRandomBadge(employee, badgeDBRepository);
        int numBadges3 = controller.getBadgesByUserNum(employee);
        Assertions.assertEquals(3,numBadges3, "User should have exactly 3 badges.");
        badgeDBRepository.delete(badgeDB);
        badgeDBRepository.delete(badgeDB3);
        badgeDBRepository.delete(badgeDB4);
        int numBadges4 = controller.getBadgesByUserNum(employee);
        Assertions.assertEquals(0, numBadges4, "User should not have any Badges");
    }

    /**
     * Tests if controller properly returns the number of badges for a given department
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "test_departmentleader", authorities = {"DEPARTMENTLEADER"})
    public void badges_by_department() {
        User user = DataHelper.createUserWithRoles("test_departmentleader", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        Department test = DataHelper.createRandomDepartment(admin, departmentRepository);
        User testUser1 = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE), admin, test, null, userRepository);
        User testUser2 = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE), admin, test, null, userRepository);
        BadgeDB badgeDB1 = DataHelper.createRandomBadge(testUser1, badgeDBRepository);
        BadgeDB badgeDB2 = DataHelper.createRandomBadge(testUser2, badgeDBRepository);
        Collection<BadgeDB> badgeDBS = controller.getBadgesByDepartment(test);
        Assertions.assertEquals(badgeDB1.getBadgeName(), Objects.requireNonNull(badgeDBS.stream().findFirst().orElse(null)).getBadgeName(), "Badges should be equivalent");
        Assertions.assertEquals(badgeDB2.getBadgeName(), Objects.requireNonNull(badgeDBS.stream().skip(1).findFirst().orElse(null)).getBadgeName(), "Badges should be equivalent");

        user.setAssignedDepartment(test);
        userRepository.save(user);
        testUser1.setAssignedDepartment(test);
        userRepository.save(testUser1);
        testUser2.setAssignedDepartment(test);
        userRepository.save(testUser2);

        Collection<BadgeDB> badgeDBS2 = controller.getBadgesByDepartment();
        Assertions.assertEquals(badgeDB1.getBadgeName(), Objects.requireNonNull(badgeDBS2.stream().findFirst().orElse(null)).getBadgeName(), "Badges should be equivalent");
        Assertions.assertEquals(badgeDB2.getBadgeName(), Objects.requireNonNull(badgeDBS2.stream().skip(1).findFirst().orElse(null)).getBadgeName(), "Badges should be equivalent");
    }

    /**
     * Tests if controller properly returns the number of badges for a given department.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "test_departmentleader", authorities = {"DEPARTMENTLEADER"})
    public void badges_by_department_num() {
        User user = DataHelper.createUserWithRoles("test_departmentleader", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        Department test = DataHelper.createRandomDepartment(admin, departmentRepository);
        User testUser1 = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE), admin, test, null, userRepository);
        User testUser2 = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE), admin, test, null, userRepository);
        BadgeDB badgeDB1 = DataHelper.createRandomBadge(testUser1, badgeDBRepository);
        BadgeDB badgeDB2 = DataHelper.createRandomBadge(testUser2, badgeDBRepository);

        int numBadges1 = controller.getBadgesByDepartmentNum(test);
        Assertions.assertEquals(2,numBadges1, "Department should have exactly 3 badges.");
        badgeDBRepository.delete(badgeDB1);
        badgeDBRepository.delete(badgeDB2);
        int numBadges2 = controller.getBadgesByDepartmentNum(test);
        Assertions.assertEquals(0, numBadges2, "User should not have any Badges");

        user.setAssignedDepartment(test);
        userRepository.save(user);

        BadgeDB badgeDB3 = DataHelper.createRandomBadge(testUser1, badgeDBRepository);
        BadgeDB badgeDB4 = DataHelper.createRandomBadge(testUser2, badgeDBRepository);

        int numBadges3 = controller.getBadgesByDepartmentNum();
        Assertions.assertEquals(2,numBadges3, "Department should have exactly 3 badges.");
        badgeDBRepository.delete(badgeDB3);
        badgeDBRepository.delete(badgeDB4);
        int numBadges4 = controller.getBadgesByDepartmentNum();
        Assertions.assertEquals(0, numBadges4, "User should not have any Badges");
    }

    /**
     * Tests if controller properly returns the badges from last week.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "test_admin", authorities = {"ADMIN"})
    public void badges_last_week_user() {
        User user = DataHelper.createUserWithRoles("test_admin", Sets.newSet(UserRole.ADMIN), userRepository);
        BadgeDB badgeDB1 = DataHelper.createRandomBadgeLastWeek(user, badgeDBRepository);

        Collection<BadgeDB> badgeDBS = controller.getLastWeeksBadgesByUser();
        Assertions.assertEquals(badgeDB1.getBadgeName(), badgeDBS.iterator().next().getBadgeName(), "Different Badge in Repository");
        Collection<BadgeDB> badgeDBS2 = controller.getLastWeeksBadges();
        Assertions.assertEquals(badgeDB1.getBadgeName(), badgeDBS2.iterator().next().getBadgeName(), "Different Badge in Repository");
    }
}
