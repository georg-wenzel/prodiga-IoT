package uibk.ac.at.prodiga.tests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.services.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@WebAppConfiguration
public class UserServiceTest {

    @Autowired
    UserService userService;

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testDatainitialization() {
        Assert.assertEquals("Insufficient amount of users initialized for test data source", 3, userService.getAllUsers().size());
        for (User user : userService.getAllUsers()) {
            if ("admin".equals(user.getUsername())) {
                Assert.assertTrue("User \"admin\" does not have role ADMIN", user.getRoles().contains(UserRole.ADMIN));
                Assert.assertNotNull("User \"admin\" does not have a createUser defined", user.getCreateUser());
                Assert.assertNotNull("User \"admin\" does not have a createDate defined", user.getCreateDate());
                Assert.assertNull("User \"admin\" has a updateUser defined", user.getUpdateUser());
                Assert.assertNull("User \"admin\" has a updateDate defined", user.getUpdateDate());
            } else if ("user1".equals(user.getUsername())) {
                Assert.assertTrue("User \"user1\" does not have role MANAGER", user.getRoles().contains(UserRole.MANAGER));
                Assert.assertNotNull("User \"user1\" does not have a createUser defined", user.getCreateUser());
                Assert.assertNotNull("User \"user1\" does not have a createDate defined", user.getCreateDate());
                Assert.assertNull("User \"user1\" has a updateUser defined", user.getUpdateUser());
                Assert.assertNull("User \"user1\" has a updateDate defined", user.getUpdateDate());
            } else if ("user2".equals(user.getUsername())) {
                Assert.assertTrue("User \"user2\" does not have role EMPLOYEE", user.getRoles().contains(UserRole.EMPLOYEE));
                Assert.assertNotNull("User \"user2\" does not have a createUser defined", user.getCreateUser());
                Assert.assertNotNull("User \"user2\" does not have a createDate defined", user.getCreateDate());
                Assert.assertNull("User \"user2\" has a updateUser defined", user.getUpdateUser());
                Assert.assertNull("User \"user2\" has a updateDate defined", user.getUpdateDate());
            } else {
                Assert.fail("Unknown user \"" + user.getUsername() + "\" loaded from test data source via UserService.getAllUsers");
            }
        }
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testDeleteUser() throws Exception {
        User adminUser = userService.loadUser("admin");
        Assert.assertNotNull("Admin user could not be loaded from test data source", adminUser);
        User toBeDeletedUser = userService.loadUser("user1");
        Assert.assertNotNull("User1 could not be loaded from test data source", toBeDeletedUser);

        userService.deleteUser(toBeDeletedUser);

        Assert.assertEquals("No user has been deleted after calling UserService.deleteUser", 2, userService.getAllUsers().size());
        User deletedUser = userService.loadUser("user1");
        Assert.assertNull("Deleted User1 could still be loaded from test data source via UserService.loadUser", deletedUser);

        for (User remainingUser : userService.getAllUsers()) {
            Assert.assertNotEquals("Deleted User1 could still be loaded from test data source via UserService.getAllUsers", toBeDeletedUser.getUsername(), remainingUser.getUsername());
        }
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testUpdateUser() {
        User adminUser = userService.loadUser("admin");
        Assert.assertNotNull("Admin user could not be loaded from test data source", adminUser);
        User toBeSavedUser = userService.loadUser("user1");
        Assert.assertNotNull("User1 could not be loaded from test data source", toBeSavedUser);

        Assert.assertNull("User \"user1\" has a updateUser defined", toBeSavedUser.getUpdateUser());
        Assert.assertNull("User \"user1\" has a updateDate defined", toBeSavedUser.getUpdateDate());

        toBeSavedUser.setEmail("changed-email@whatever.wherever");
        userService.saveUser(toBeSavedUser);

        User freshlyLoadedUser = userService.loadUser("user1");
        Assert.assertNotNull("User1 could not be loaded from test data source after being saved", freshlyLoadedUser);
        Assert.assertNotNull("User \"user1\" does not have a updateUser defined after being saved", freshlyLoadedUser.getUpdateUser());
        Assert.assertEquals("User \"user1\" has wrong updateUser set", adminUser, freshlyLoadedUser.getUpdateUser());
        Assert.assertNotNull("User \"user1\" does not have a updateDate defined after being saved", freshlyLoadedUser.getUpdateDate());
        Assert.assertEquals("User \"user1\" does not have a the correct email attribute stored being saved", "changed-email@whatever.wherever", freshlyLoadedUser.getEmail());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testCreateUser() {
        User adminUser = userService.loadUser("admin");
        Assert.assertNotNull("Admin user could not be loaded from test data source", adminUser);

        User toBeCreatedUser = new User();
        toBeCreatedUser.setUsername("newuser");
        toBeCreatedUser.setPassword("passwd");
        toBeCreatedUser.setEnabled(true);
        toBeCreatedUser.setFirstName("New");
        toBeCreatedUser.setLastName("User");
        toBeCreatedUser.setEmail("new-email@whatever.wherever");
        toBeCreatedUser.setPhone("+12 345 67890");
        toBeCreatedUser.setRoles(Sets.newSet(UserRole.EMPLOYEE, UserRole.MANAGER));
        userService.saveUser(toBeCreatedUser);

        User freshlyCreatedUser = userService.loadUser("newuser");
        Assert.assertNotNull("New user could not be loaded from test data source after being saved", freshlyCreatedUser);
        Assert.assertEquals("User \"newuser\" does not have a the correct username attribute stored being saved", "newuser", freshlyCreatedUser.getUsername());
        Assert.assertEquals("User \"newuser\" does not have a the correct password attribute stored being saved", "passwd", freshlyCreatedUser.getPassword());
        Assert.assertEquals("User \"newuser\" does not have a the correct firstName attribute stored being saved", "New", freshlyCreatedUser.getFirstName());
        Assert.assertEquals("User \"newuser\" does not have a the correct lastName attribute stored being saved", "User", freshlyCreatedUser.getLastName());
        Assert.assertEquals("User \"newuser\" does not have a the correct email attribute stored being saved", "new-email@whatever.wherever", freshlyCreatedUser.getEmail());
        Assert.assertEquals("User \"newuser\" does not have a the correct phone attribute stored being saved", "+12 345 67890", freshlyCreatedUser.getPhone());
        Assert.assertTrue("User \"newuser\" does not have role MANAGER", freshlyCreatedUser.getRoles().contains(UserRole.MANAGER));
        Assert.assertTrue("User \"newuser\" does not have role EMPLOYEE", freshlyCreatedUser.getRoles().contains(UserRole.EMPLOYEE));
        Assert.assertNotNull("User \"newuser\" does not have a createUser defined after being saved", freshlyCreatedUser.getCreateUser());
        Assert.assertEquals("User \"newuser\" has wrong createUser set", adminUser, freshlyCreatedUser.getCreateUser());
        Assert.assertNotNull("User \"newuser\" does not have a createDate defined after being saved", freshlyCreatedUser.getCreateDate());
    }

    @Test(expected = org.springframework.orm.jpa.JpaSystemException.class)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testExceptionForEmptyUsername() {
        User adminUser = userService.loadUser("admin");
        Assert.assertNotNull("Admin user could not be loaded from test data source", adminUser);

        User toBeCreatedUser = new User();
        userService.saveUser(toBeCreatedUser);
    }

    @Test(expected = org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class)
    public void testUnauthenticateddLoadUsers() {
        for (User user : userService.getAllUsers()) {
            Assert.fail("Call to userService.getAllUsers should not work without proper authorization");
        }
    }

    @Test(expected = org.springframework.security.access.AccessDeniedException.class)
    @WithMockUser(username = "user", authorities = {"EMPLOYEE"})
    public void testUnauthorizedLoadUsers() {
        for (User user : userService.getAllUsers()) {
            Assert.fail("Call to userService.getAllUsers should not work without proper authorization");
        }
    }

    @Test(expected = org.springframework.security.access.AccessDeniedException.class)
    @WithMockUser(username = "user1", authorities = {"EMPLOYEE"})
    public void testUnauthorizedLoadUser() {
        User user = userService.loadUser("admin");
        Assert.fail("Call to userService.loadUser should not work without proper authorization for other users than the authenticated one");
    }

    @WithMockUser(username = "user1", authorities = {"EMPLOYEE"})
    public void testAuthorizedLoadUser() {
        User user = userService.loadUser("user1");
        Assert.assertEquals("Call to userService.loadUser returned wrong user", "user1", user.getUsername());
    }

    @Test(expected = org.springframework.security.access.AccessDeniedException.class)
    @WithMockUser(username = "user1", authorities = {"EMPLOYEE"})
    public void testUnauthorizedSaveUser() {
        User user = userService.loadUser("user1");
        Assert.assertEquals("Call to userService.loadUser returned wrong user", "user1", user.getUsername());
        userService.saveUser(user);
    }

    @Test(expected = org.springframework.security.access.AccessDeniedException.class)
    @WithMockUser(username = "user1", authorities = {"EMPLOYEE"})
    public void testUnauthorizedDeleteUser() throws Exception {
        User user = userService.loadUser("user1");
        Assert.assertEquals("Call to userService.loadUser returned wrong user", "user1", user.getUsername());
        userService.deleteUser(user);
    }

}
