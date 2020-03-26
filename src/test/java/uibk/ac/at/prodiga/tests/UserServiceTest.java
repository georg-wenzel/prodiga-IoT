package uibk.ac.at.prodiga.tests;

import org.apache.tomcat.util.log.UserDataHelper;
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
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.DepartmentRepository;
import uibk.ac.at.prodiga.repositories.TeamRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.services.UserService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void userService_deletingUsers_UsersInDBChanges() throws Exception {
        int createdUser = 10;
        int deletedUser = 5;
        DataHelper.createAdminUser("admin", userRepository);

        int userInDB = userService.getAllUsers().size();

        for(int i = 0; i < createdUser; i++) {
            DataHelper.createUserWithRoles("user" + i, Sets.newSet(UserRole.EMPLOYEE), userRepository);
        }

        Assertions.assertEquals(userInDB + createdUser, userService.getAllUsers().size(),
                "Not all userers have been created");

        for(int i = 0; i < deletedUser; i++) {
            User u = userService.loadUser("user" + i);

            Assertions.assertNotNull(u, "Created user could not be found");

            userService.deleteUser(u);
        }

        Assertions.assertEquals((createdUser - deletedUser) + userInDB,
                userService.getAllUsers().size(), "Not all or more user got deleted");

        for(int i = 0; i < createdUser; i++) {
            User u = userService.loadUser("user" + i);

            if(i < deletedUser) {
                Assertions.assertNull(u, "Previously deleted user still exists in DB");
            } else {
                Assertions.assertNotNull(u, "User which should not be deleted, got deleted!");
            }
        }
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DirtiesContext
    public void userService_updateUser_FieldsChangeAccordingly() {
        DataHelper.createAdminUser("admin", userRepository);
        User u = DataHelper.createRandomUser(userRepository);

        u.setEmail("changed-email@whatever.wherever");
        userService.saveUser(u);

        u = userService.loadUser(u.getUsername());
        Assertions.assertNotNull(u, "Updated user could not be loaded from DB");
        Assertions.assertNotNull(u.getUpdateUser(), "Updated user not set after updating");
        Assertions.assertEquals("admin", u.getUpdateUser().getUsername(), "Updated username does not match logged in user");
        Assertions.assertNotNull(u.getUpdateDate(), "Updated Date not set after user update");
        Assertions.assertEquals("changed-email@whatever.wherever", u.getEmail(), "Email not correctly set in DB after update.");

    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DirtiesContext
    public void userService_createUser_UserExistsInDB() {
        User adminUser = DataHelper.createAdminUser("admin", userRepository);

        User toBeCreatedUser = new User();
        toBeCreatedUser.setUsername("newuser");
        toBeCreatedUser.setPassword("passwd");
        toBeCreatedUser.setEnabled(true);
        toBeCreatedUser.setFirstName("New");
        toBeCreatedUser.setLastName("User");
        toBeCreatedUser.setEmail("new-email@whatever.wherever");
        toBeCreatedUser.setPhone("+12 345 67890");
        toBeCreatedUser.setRoles(Sets.newSet(UserRole.EMPLOYEE, UserRole.TEAMLEADER));
        userService.saveUser(toBeCreatedUser);

        User freshlyCreatedUser = userService.loadUser("newuser");
        Assertions.assertNotNull(freshlyCreatedUser, "New user could not be loaded from test data source after being saved");
        Assertions.assertEquals("newuser", freshlyCreatedUser.getUsername(), "User \"newuser\" does not have a the correct username attribute stored being saved");
        Assertions.assertEquals("passwd", freshlyCreatedUser.getPassword(), "User \"newuser\" does not have a the correct password attribute stored being saved");
        Assertions.assertEquals("New", freshlyCreatedUser.getFirstName(), "User \"newuser\" does not have a the correct firstName attribute stored being saved");
        Assertions.assertEquals("User", freshlyCreatedUser.getLastName(), "User \"newuser\" does not have a the correct lastName attribute stored being saved");
        Assertions.assertEquals("new-email@whatever.wherever", freshlyCreatedUser.getEmail(), "User \"newuser\" does not have a the correct email attribute stored being saved");
        Assertions.assertEquals("+12 345 67890", freshlyCreatedUser.getPhone(), "User \"newuser\" does not have a the correct phone attribute stored being saved");
        Assertions.assertTrue(freshlyCreatedUser.getRoles().contains(UserRole.TEAMLEADER), "User \"newuser\" does not have role MANAGER");
        Assertions.assertTrue(freshlyCreatedUser.getRoles().contains(UserRole.EMPLOYEE), "User \"newuser\" does not have role EMPLOYEE");
        Assertions.assertNotNull(freshlyCreatedUser.getCreateUser(), "User \"newuser\" does not have a createUser defined after being saved");
        Assertions.assertEquals(adminUser, freshlyCreatedUser.getCreateUser(), "User \"newuser\" has wrong createUser set");
        Assertions.assertNotNull(freshlyCreatedUser.getCreateDate(), "User \"newuser\" does not have a createDate defined after being saved");
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DirtiesContext
    public void testExceptionForEmptyUsername() {
        DataHelper.createAdminUser("admin", userRepository);

        User toBeCreatedUser = new User();

        Assertions.assertThrows(org.springframework.orm.jpa.JpaSystemException.class, () -> {
            userService.saveUser(toBeCreatedUser);
        });
    }

    @Test
    public void testUnauthenticateddLoadUsers() {
        Assertions.assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> {
            userService.getAllUsers();
        });
    }

    @Test
    @WithMockUser(username = "user", authorities = {"EMPLOYEE"})
    public void testUnauthorizedLoadUsers() {

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            userService.getAllUsers();
        }, "Call to userService.getAllUsers should not work without proper authorization");
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"EMPLOYEE"})
    public void testUnauthorizedLoadUser() {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            User user = userService.loadUser("admin");
        }, "Call to userService.loadUser should not work without proper authorization for other users than the authenticated one");
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"EMPLOYEE"})
    public void testAuthorizedLoadUser() {
        DataHelper.createUserWithRoles("user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        User user = userService.loadUser("user1");
        Assertions.assertEquals("user1", user.getUsername(), "Call to userService.loadUser returned wrong user");
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"EMPLOYEE"})
    @DirtiesContext
    public void testUnauthorizedSaveUser() {
        User user = DataHelper.createUserWithRoles("user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            userService.saveUser(user);
        });
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"EMPLOYEE"})
    @DirtiesContext
    public void testUnauthorizedDeleteUser() throws Exception {
        User user = DataHelper.createUserWithRoles("user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            userService.deleteUser(user);
        });
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DirtiesContext
    public void testLoadDepartmentLeader() {
        User admin = DataHelper.createAdminUser("admin", userRepository);

        Department dept = new Department();
        dept.setName("DEPT_TEST_01");
        dept.setObjectCreatedDateTime(new Date());
        dept.setObjectCreatedUser(admin);
        dept = departmentRepository.save(dept);

        User test_leader = new User();
        test_leader.setUsername("USER_TEST_01");
        test_leader.setRoles(Sets.newSet(UserRole.DEPARTMENTLEADER));
        test_leader.setAssignedDepartment(dept);
        test_leader.setCreateDate(new Date());
        test_leader.setCreateUser(admin);
        test_leader = userRepository.save(test_leader);

        Assertions.assertEquals(userService.getDepartmentLeaderOf(dept), test_leader, "DEPT_TEST_01 department leader does not match USER_TEST_01.");
    }

    @Test
    @WithMockUser(username = "dept_leader", authorities = {"DEPARTMENTLEADER"})
    @DirtiesContext
    public void testLoadTeamLeader() {
        User admin = DataHelper.createAdminUser("admin", userRepository);

        Department dept = new Department();
        dept.setName("DEPT_TEST_01");
        dept.setObjectCreatedDateTime(new Date());
        dept.setObjectCreatedUser(admin);
        dept = departmentRepository.save(dept);

        Team team = new Team();
        team.setName("DEPT_TEST_01");
        team.setDepartment(dept);
        team.setObjectCreatedDateTime(new Date());
        team.setObjectCreatedUser(admin);
        team = teamRepository.save(team);

        User test_leader = new User();
        test_leader.setUsername("USER_TEST_01");
        test_leader.setRoles(Sets.newSet(UserRole.TEAMLEADER));
        test_leader.setAssignedDepartment(dept);
        test_leader.setAssignedTeam(team);
        test_leader.setCreateDate(new Date());
        test_leader.setCreateUser(admin);
        test_leader = userRepository.save(test_leader);

        Assertions.assertEquals(userService.getTeamLeaderOf(team), test_leader, "TEAM_TEST_01 team leader does not match USER_TEST_01.");
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"ADMIN", "TEAMLEADER", "EMPLOYEE"})
    public void testLoadTeamLeaderUnauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            userService.getTeamLeaderOf(new Team());
        }, "Team leader was loaded despite missing authorization.");
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "EMPLOYEE"})
    public void testLoadDepartmentLeaderUnauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            userService.getDepartmentLeaderOf(new Department());
        }, "Department leader was loaded despite missing authorization.");
    }
}
