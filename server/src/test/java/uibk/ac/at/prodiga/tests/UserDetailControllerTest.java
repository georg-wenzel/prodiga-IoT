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
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.DepartmentRepository;
import uibk.ac.at.prodiga.repositories.TeamRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.ui.controllers.UserDetailController;

import java.util.List;
import java.util.Set;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class UserDetailControllerTest {

    @Autowired
    UserDetailController userDetailController;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    TeamRepository teamRepository;

    User admin = null;
    User u1 = null;
    User teamLeader = null;
    User deptLeader = null;
    Department dept = null;
    Team t = null;

    @BeforeEach
    public void init() {
        admin = DataHelper.createAdminUser("admin", userRepository);
        teamLeader = DataHelper.createUserWithRoles("team", Sets.newSet(UserRole.TEAMLEADER), userRepository);
        deptLeader = DataHelper.createUserWithRoles("dept", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);

        dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        t = DataHelper.createRandomTeam(dept, admin, teamRepository);
        u1 = DataHelper.createUserWithRoles("test1", Sets.newSet(UserRole.EMPLOYEE), admin, dept, t, userRepository);
        DataHelper.createUserWithRoles("test2", Sets.newSet(UserRole.EMPLOYEE), admin, dept, t, userRepository);
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void userDetailController_reloadUser_correctUserSet() {
        userDetailController.doReloadUser("team");

        Assertions.assertEquals(teamLeader, userDetailController.getUser());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void userDetailController_reloadUser_newUserCreated() {
        userDetailController.doReloadUser(null);
        Assertions.assertTrue(userDetailController.getUser().isNew());
    }


    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void userDetailController_getAvailableTeamListNoDept_EmptyListReturned() {
        Assertions.assertEquals(0, userDetailController.getAvailableTeamList().size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void userDetailController_getAvailableTeamListNoDept_correctAmountReturned() {
        userDetailController.setUser(u1);
        Assertions.assertEquals(1, userDetailController.getAvailableTeamList().size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void userDetailController_saveUser_userSaved() throws Exception {
        userDetailController.setUser(u1);
        userDetailController.getUser().setEmail("test");
        userDetailController.doSaveUser();
        Assertions.assertEquals("test", userRepository.findFirstByUsername("test1").getEmail());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void userDetailController_deleteUser_userDelted() throws Exception {
        userDetailController.setUser(u1);
        userDetailController.doDeleteUser();
        Assertions.assertNull(userRepository.findFirstByUsername("test1"));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void userDetailController_getUserRoles_correctRoleReturned(){
        userDetailController.setUser(u1);
        Assertions.assertEquals(UserRole.EMPLOYEE, userDetailController.getUserRoles().get(0));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void userDetailController_getAllUserRoles_correctRoleReturned(){
        List<UserRole> roles = userDetailController.getAllUserRoles();

        Assertions.assertEquals(4, roles.size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void userDetailController_setIsAdmin_adminsSetCorrectly(){
        userDetailController.setUser(u1);
        userDetailController.setIsAdmin(true);

        Assertions.assertTrue(userDetailController.getUser().getRoles().contains(UserRole.ADMIN));

        userDetailController.setIsAdmin(false);

        Assertions.assertFalse(userDetailController.getUser().getRoles().contains(UserRole.ADMIN));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void userDetailController_getIsAdmin_adminsCorrectlyReturned(){
        Assertions.assertFalse(userDetailController.getIsAdmin());
        userDetailController.setUser(u1);
        Assertions.assertFalse(userDetailController.getIsAdmin());
        userDetailController.setUser(admin);
        Assertions.assertTrue(userDetailController.getIsAdmin());
    }

}
