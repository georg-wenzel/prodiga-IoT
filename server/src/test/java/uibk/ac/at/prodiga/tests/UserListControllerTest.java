package uibk.ac.at.prodiga.tests;

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

    @BeforeEach
    public void init() {
        admin = DataHelper.createAdminUser("admin", userRepository);
        teamLeader = DataHelper.createUserWithRoles("team", Set.of(UserRole.TEAMLEADER), userRepository);
        deptLeader = DataHelper.createUserWithRoles("dept", Set.of(UserRole.DEPARTMENTLEADER), userRepository);

        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, admin, teamRepository);
        DataHelper.createUserWithRoles("test1", Set.of(UserRole.EMPLOYEE), admin, dept, team, userRepository);
        DataHelper.createUserWithRoles("test2", Set.of(UserRole.EMPLOYEE), admin, dept, team, userRepository);
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void UserListController_getUserAsAdmin_allUsersReturned() {
        System.out.println(userListController.getUsers());
    }

}
