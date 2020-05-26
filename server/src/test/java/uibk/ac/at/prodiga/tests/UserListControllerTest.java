package uibk.ac.at.prodiga.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uibk.ac.at.prodiga.ui.controllers.UserListController;

import java.util.Set;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class UserListControllerTest {

    @Autowired
    UserListController userListController;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    TeamRepository teamRepository;

    User admin = null;
    User teamLeader = null;
    User deptLeader = null;
    Department dept = null;
    Team t = null;

    @BeforeEach
    public void init() {
        admin = DataHelper.createAdminUser("admin", userRepository);
        teamLeader = DataHelper.createUserWithRoles("team", Set.of(UserRole.TEAMLEADER), userRepository);
        deptLeader = DataHelper.createUserWithRoles("dept", Set.of(UserRole.DEPARTMENTLEADER), userRepository);

        dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        t = DataHelper.createRandomTeam(dept, admin, teamRepository);
        DataHelper.createUserWithRoles("test1", Set.of(UserRole.EMPLOYEE), admin, dept, t, userRepository);
        DataHelper.createUserWithRoles("test2", Set.of(UserRole.EMPLOYEE), admin, dept, t, userRepository);
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void UserListController_getUserAsAdmin_allUsersReturned() {
        Assertions.assertEquals(5, userListController.getUsers().size());
        Assertions.assertEquals(5, userListController.getUserTeams().size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "team", authorities = {"TEAMLEADER"})
    public void UserListController_getUserAsTeamleader_allUsersReturned() {
        Assertions.assertEquals(3, userListController.getUsers().size());
        Assertions.assertEquals(3, userListController.getUserTeams().size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "dept", authorities = {"DEPARTMENTLEADER"})
    public void UserListController_getUserAsDepartmentLeader_allUsersReturned() {
        Assertions.assertEquals(3, userListController.getUsers().size());
        Assertions.assertEquals(3, userListController.getUserTeams().size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void UserListController_getAllUsersInDepartment_returnsCorrectAmount() {
        Assertions.assertEquals(0, userListController.getAllUsersInDepartment(null).size());
        Assertions.assertEquals(0, userListController.getAllUsersInDepartment(new Department()).size());
        Assertions.assertEquals(2, userListController.getAllUsersInDepartment(dept).size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void UserListController_getAllUsersInTeam_returnsCorrectAmount() {
        Assertions.assertEquals(0, userListController.getAllUsersInTeam(null).size());
        Assertions.assertEquals(0, userListController.getAllUsersInTeam(new Team()).size());
        Assertions.assertEquals(2, userListController.getAllUsersInTeam(t).size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"admin"})
    public void UserListController_getDepartmentTeamsAdmin_returnsEmpty() {
        Assertions.assertNull(userListController.getDepartmentTeams());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "dept", authorities = {"DEPARTMENTLEADER"})
    public void UserListController_getDepartmentTeamsDepartmentLeader_returnsCorrectAmount() {
        Assertions.assertEquals(0, userListController.getDepartmentTeams().size());
    }


}
