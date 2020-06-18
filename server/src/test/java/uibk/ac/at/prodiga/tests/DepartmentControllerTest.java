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
import uibk.ac.at.prodiga.services.DepartmentService;
import uibk.ac.at.prodiga.services.TeamService;
import uibk.ac.at.prodiga.services.UserService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.ui.controllers.DepartmentController;

import java.util.Collection;

/**
 * Tests proper implementation of the department controller
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class DepartmentControllerTest
{
    @Autowired
    UserRepository userRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    UserService userService;

    @Autowired
    TeamService teamService;

    User admin;
    DepartmentController controller;

    Department dept1, dept2;
    Team team1;

    @BeforeEach
    public void init_each()
    {
        admin = DataHelper.createAdminUser("admin", userRepository);
        dept1 = DataHelper.createRandomDepartment(admin, departmentRepository);
        dept2 = DataHelper.createRandomDepartment(admin, departmentRepository);
        team1 = DataHelper.createRandomTeam(dept2, admin, teamRepository);

        controller = new DepartmentController(departmentService, userService, teamService);
    }

    /**
     * Tests if controller properly fetches all departments
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void load_departments()
    {
        Assertions.assertEquals(controller.getAllDepartments(), departmentRepository.findAll());
    }

    /**
     * Tests if controller properly stores a new department
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void save_department() throws Exception
    {
        controller.setDepartment(new Department());
        controller.getDepartment().setName("testdepartment");
        controller.doSaveDepartment();

        //check that department was created properly
        Assertions.assertEquals(departmentRepository.findFirstById(controller.getDepartment().getId()), controller.getDepartment(), "Department was not properly stored in database");
        Assertions.assertEquals(departmentRepository.findFirstById(controller.getDepartment().getId()).getName(), controller.getDepartment().getName(), "Department name was not properly stored in database");
    }

    /**
     * Tests if controller properly updates departmentleader and name
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void edit_department() throws Exception
    {
        controller.setDepartment(dept1);
        User emp = DataHelper.createUserWithRoles("testemp", Sets.newSet(UserRole.EMPLOYEE), admin, dept1, null, userRepository);
        controller.setDepartmentLeader(emp);
        controller.getDepartment().setName("hullo");
        controller.doSaveDepartment();

        //check that department was updated properly
        Assertions.assertEquals(emp, userRepository.findDepartmentLeaderOf(dept1), "Department leader was not updated properly");
        Assertions.assertEquals("hullo", departmentRepository.findFirstById(dept1.getId()).getName());
    }

    /**
     * Tests if controller properly finds the departmentleader
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void find_dept_leader()
    {
        User emp = DataHelper.createUserWithRoles("testemp", Sets.newSet(UserRole.EMPLOYEE, UserRole.DEPARTMENTLEADER), admin, dept1, null, userRepository);

        //check that departmentleader is properly found
        Assertions.assertEquals(emp, controller.getDepartmentLeaderOf(dept1));
    }

    /**
     * Tests if controller properly deletes a department
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void delete_department() throws Exception
    {
        controller.setDepartment(dept1);
        controller.doDeleteDepartment();

        Assertions.assertNull(departmentRepository.findFirstById(dept1.getId()));
    }

    /**
     * Tests if controller properly deletes a user from the department
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void delete_user_from_department() throws Exception
    {
        User emp = DataHelper.createUserWithRoles("testemp", Sets.newSet(UserRole.EMPLOYEE), admin, dept1, null, userRepository);

        controller.setDepartment(dept1);
        controller.deleteUserFromDepartment(emp);

        Assertions.assertNull(userRepository.findFirstByUsername(emp.getUsername()).getAssignedDepartment(), "Department was not properly removed from test employee.");
    }

    /**
     * Tests if controller properly finds teams associated with department
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void find_dept_teams()
    {
        Collection<Team> deptTeams = controller.showTeamsofDepartment(dept2);

        Assertions.assertEquals(1, deptTeams.size(), "Not the correct amount of teams was found for dept2.");
        Assertions.assertTrue(deptTeams.contains(team1), "Team 1 was not found in teams of Dept 2");
    }

    /**
     * Tests if controller properly decides which users can and cannot be deleted from departments.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void test_deletion_eligibility_check()
    {
        User deptleader = DataHelper.createUserWithRoles("deptleader", Sets.newSet(UserRole.EMPLOYEE, UserRole.DEPARTMENTLEADER), admin, dept2, null, userRepository);
        User teamleader = DataHelper.createUserWithRoles("teamleader", Sets.newSet(UserRole.EMPLOYEE, UserRole.TEAMLEADER), admin, dept2, team1, userRepository);
        User employee = DataHelper.createUserWithRoles("testemp", Sets.newSet(UserRole.EMPLOYEE), admin, dept2, team1, userRepository);

        Assertions.assertFalse(controller.mayBeDeleteFromDepartment(null, dept2));
        Assertions.assertFalse(controller.mayBeDeleteFromDepartment(deptleader, dept2));
        Assertions.assertFalse(controller.mayBeDeleteFromDepartment(teamleader, dept2));
        Assertions.assertTrue(controller.mayBeDeleteFromDepartment(employee, dept2));
    }
}
