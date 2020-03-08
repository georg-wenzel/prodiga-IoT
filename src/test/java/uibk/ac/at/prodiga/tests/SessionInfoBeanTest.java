package uibk.ac.at.prodiga.tests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.services.UserService;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@WebAppConfiguration
public class SessionInfoBeanTest {

    @Autowired
    private ProdigaUserLoginManager prodigaUserLoginManager;

    @Autowired
    private UserService userService;

    @Test
    @WithMockUser(username = "user1", authorities = {"EMPLOYEE"})
    public void testLoggedIn() {
        Assert.assertTrue("sessionInfoBean.isLoggedIn does not return true for authenticated user", prodigaUserLoginManager.isLoggedIn());
        Assert.assertEquals("sessionInfoBean.getCurrentUserName does not return authenticated user's name", "user1", prodigaUserLoginManager.getCurrentUserName());
        Assert.assertEquals("sessionInfoBean.getCurrentUser does not return authenticated user", "user1", prodigaUserLoginManager.getCurrentUser().getUsername());
        Assert.assertEquals("sessionInfoBean.getCurrentUserRoles does not return authenticated user's roles", "EMPLOYEE", prodigaUserLoginManager.getCurrentUserRoles());
        Assert.assertTrue("sessionInfoBean.hasRole does not return true for a role the authenticated user has", prodigaUserLoginManager.hasRole("EMPLOYEE"));
        Assert.assertFalse("sessionInfoBean.hasRole does not return false for a role the authenticated user does not have", prodigaUserLoginManager.hasRole("ADMIN"));
    }

    @Test
    public void testNotLoggedIn() {
        Assert.assertFalse("sessionInfoBean.isLoggedIn does return true for not authenticated user", prodigaUserLoginManager.isLoggedIn());
        Assert.assertEquals("sessionInfoBean.getCurrentUserName does not return empty string when not logged in", "", prodigaUserLoginManager.getCurrentUserName());
        Assert.assertNull("sessionInfoBean.getCurrentUser does not return null when not logged in", prodigaUserLoginManager.getCurrentUser());
        Assert.assertEquals("sessionInfoBean.getCurrentUserRoles does not return empty string when not logged in", "", prodigaUserLoginManager.getCurrentUserRoles());
        for (UserRole role : UserRole.values()) {
            Assert.assertFalse("sessionInfoBean.hasRole does not return false for all possible roales", prodigaUserLoginManager.hasRole(role.name()));
        }
    }

}
