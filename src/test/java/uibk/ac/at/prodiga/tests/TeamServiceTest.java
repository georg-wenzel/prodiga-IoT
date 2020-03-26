package uibk.ac.at.prodiga.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.InitializingBean;
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
import uibk.ac.at.prodiga.services.TeamService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Test class for the Team Service
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class TeamServiceTest implements InitializingBean
{
    @Autowired
    TeamService teamService;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    UserRepository userRepository;

    /**
     * Sets up the test environment - executed before each test and cleaned up after each test (@DirtiesContext)
     */
    @Override
    public void afterPropertiesSet()
    {
        //Grab admin user to set as creation user for test teams and users
        User admin = DataHelper.createAdminUser("admin", userRepository);

        //Before tests, initialize test teams and users
        if(departmentRepository.findFirstByName("DEPT_TEST_01") == null)
        {
            Department dept = new Department();
            dept.setName("DEPT_TEST_01");
            dept.setObjectCreatedUser(admin);
            dept.setObjectCreatedDateTime(new Date());
            dept = departmentRepository.save(dept);

            Department dept2 = new Department();
            dept2.setName("DEPT_TEST_02");
            dept2.setObjectCreatedUser(admin);
            dept2.setObjectCreatedDateTime(new Date());
            dept2 = departmentRepository.save(dept2);

            Team team = new Team();
            team.setName("TEAM_TEST_01");
            team.setObjectCreatedUser(admin);
            team.setObjectCreatedDateTime(new Date());
            team.setDepartment(dept);
            team = teamRepository.save(team);

            Team team2 = new Team();
            team2.setName("TEAM_TEST_02");
            team2.setObjectCreatedUser(admin);
            team2.setObjectCreatedDateTime(new Date());
            team2.setDepartment(dept2);
            team2 = teamRepository.save(team2);

            User test_leader = new User();
            test_leader.setCreateDate(new Date());
            test_leader.setCreateUser(admin);
            test_leader.setUsername("USER_TEST_01");
            test_leader.setAssignedDepartment(dept);
            test_leader.setAssignedTeam(team);
            test_leader.setRoles(Sets.newSet(UserRole.TEAMLEADER));
            userRepository.save(test_leader);

            User test_dept_leader = new User();
            test_dept_leader.setCreateDate(new Date());
            test_dept_leader.setCreateUser(admin);
            test_dept_leader.setAssignedDepartment(dept);
            test_dept_leader.setUsername("TEST_DEPT_LEADER_01");
            test_dept_leader.setRoles(Sets.newSet(UserRole.DEPARTMENTLEADER));
            userRepository.save(test_dept_leader);

            User test_employee = new User();
            test_employee.setCreateDate(new Date());
            test_employee.setCreateUser(admin);
            test_employee.setUsername("USER_TEST_02");
            test_employee.setRoles(Sets.newSet(UserRole.EMPLOYEE));
            test_employee.setAssignedTeam(team);
            userRepository.save(test_employee);

            User test_external_employee = new User();
            test_external_employee.setCreateDate(new Date());
            test_external_employee.setCreateUser(admin);
            test_external_employee.setUsername("USER_TEST_03");
            test_external_employee.setRoles(Sets.newSet(UserRole.EMPLOYEE));
            test_external_employee.setAssignedDepartment(dept2);
            userRepository.save(test_external_employee);

            //Create test admin to change teams with
            User test_admin = new User();
            test_admin.setCreateDate(new Date());
            test_admin.setCreateUser(admin);
            test_admin.setUsername("ADMIN_TEST_01");
            test_admin.setRoles(Sets.newSet(UserRole.ADMIN));
            userRepository.save(test_admin);
        }
    }

    /**
     * Tests loading of team data
     */
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void load_team_data()
    {
        Team team = teamService.getFirstByName("TEAM_TEST_01");
        Assertions.assertNotNull(team, "Could not load test team TEAM_TEST_01.");

        User admin = userRepository.findFirstByUsername("admin");

        Assertions.assertEquals(team.getObjectCreatedUser(), admin, "Creation user of TEAM_TEST_01 does not match admin.");
        Assertions.assertTrue((new Date()).getTime() -  team.getObjectCreatedDateTime().getTime() < 1000 * 60, "Creation date not loaded properly from TEAM_TEST_01.");
        Assertions.assertNull(team.getObjectChangedDateTime(), "TEAM_TEST_01 changed date time should be null, but is not");
        Assertions.assertNull(team.getObjectChangedUser(), "TEAM_TEST_01 changed user should be null, but is not");
        Assertions.assertEquals("DEPT_TEST_01", team.getDepartment().getName(), "TEAM_TEST_01 department does not match DEPT_TEST_01");
    }

    /**
     * Tests unauthorized loading of team data
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"ADMIN", "TEAMLEADER", "EMPLOYEE"})
    public void load_team_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            teamService.getFirstByName("TEAM_TEST_01");
        }, "Team loaded despite lacking authorization of DEPARTMENTLEADER");
    }

    /**
     * Tests loading of team collection
     */
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void load_teams()
    {
        Collection<Team> teams = teamService.getAllTeams();
        Assertions.assertNotNull(teams, "Could not load list of teams");
        Assertions.assertTrue(teams.stream().anyMatch(x -> x.getName().equals("TEAM_TEST_01")), "Could not find TEAM_TEST_01 within list of teams");
    }

    /**
     * Tests unauthorized loading of team collection
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"ADMIN", "TEAMLEADER", "EMPLOYEE"})
    public void load_teams_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            teamService.getAllTeams();
        }, "Team collection loaded despite lacking authorization of DEPARTMENTLEADER");
    }

    /**
     * Tests saving a team with sufficient authorization
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void save_team() throws ProdigaGeneralExpectedException
    {
        Team team = new Team();
        team.setName("TEAM_TEST_03");
        team.setDepartment(departmentRepository.findFirstByName("DEPT_TEST_01"));
        team = teamService.saveTeam(team);

        Assertions.assertEquals(teamRepository.findFirstById(team.getId()), team, "Created team is not equal to team loaded from database.");
        Assertions.assertEquals("TEST_DEPT_LEADER_01", team.getObjectCreatedUser().getUsername(), "Team creator TEST_DEPT_LEADER_01 did not become creator user of the DB object.");
    }

    /**
     * Tests adding a team where the name is too short
     */
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void save_team_with_invalid_name() throws ProdigaGeneralExpectedException
    {
        Team team = new Team();
        team.setName("");
        team.setDepartment(departmentRepository.findFirstByName("DEPT_TEST_01"));

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            teamService.saveTeam(team);
        }, "Team was able to be created despite the fact the team name was too short.");
    }


    /**
     * Tests adding a team with lacking authorizations
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"EMPLOYEE", "TEAMLEADER", "ADMIN"})
    public void save_team_unauthorized() throws ProdigaGeneralExpectedException
    {
        Team team = new Team();
        team.setName("TEAM_TEST_02");
        team.setDepartment(departmentRepository.findFirstByName("DEPT_TEST_01"));

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            teamService.saveTeam(team);
        }, "Team was able to be created despite lacking authorizations.");
    }

    /**
     * Tests changing a team
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void update_team() throws ProdigaGeneralExpectedException
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");
        team.setName("TEAM_TEST_03");
        team = teamService.saveTeam(team);

        //check if update user and time has been set
        Assertions.assertEquals("TEST_DEPT_LEADER_01", team.getObjectChangedUser().getUsername(), "Update User has not been properly set to TEST_DEPT_LEADER_01");
        Assertions.assertTrue((new Date()).getTime() -  team.getObjectChangedDateTime().getTime() < 1000 * 60, "Creation date not set properly for TEAM_TEST_01.");

        //Check if name is updated
        Assertions.assertEquals("TEAM_TEST_03", team.getName(), "Name of TEAM_TEST_01 was not updated accordingly");
    }

    /**
     * Tests changing a team with lacking authentication
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"ADMIN", "TEAMLEADER", "EMPLOYEE"})
    public void update_team_unauthorized()
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");
        team.setName("TEAM_TEST_03");

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            teamService.saveTeam(team);
        }, "Team was updated despite lacking authorization");
    }

    /**
     * Tests deleting a team with members
     */
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void delete_team_with_members()
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            teamService.deleteTeam(team);
        }, "Team was deleted despite still having members");
    }

    /**
     * Tests deleting an empty team
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void delete_team() throws ProdigaGeneralExpectedException
    {
        User u = userRepository.findFirstByUsername("USER_TEST_02");
        u.setAssignedTeam(null);
        userRepository.save(u);

        u = userRepository.findFirstByUsername("USER_TEST_01");
        u.setAssignedTeam(null);
        userRepository.save(u);

        Team team = teamRepository.findFirstByName("TEAM_TEST_01");
        teamService.deleteTeam(team);
    }

    /**
     * Tests deleting a team with lacking authorization
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"ADMIN", "TEAMLEADER", "EMPLOYEE"})
    public void delete_team_unauthorized()
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            teamService.deleteTeam(team);
        });
    }

    /**
     * Tests setting the team leader
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void set_team_leader() throws ProdigaGeneralExpectedException
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");
        User u2 = userRepository.findFirstByUsername("USER_TEST_02");
        teamService.setTeamLeader(team, u2);
        //reload users
        User u1 = userRepository.findFirstByUsername("USER_TEST_01");
        u2 = userRepository.findFirstByUsername("USER_TEST_02");

        Assertions.assertTrue(u1.getRoles().contains(UserRole.EMPLOYEE) && !u1.getRoles().contains(UserRole.TEAMLEADER), "USER_TEST_01 was not made employee.");
        Assertions.assertTrue(!u2.getRoles().contains(UserRole.EMPLOYEE) && u2.getRoles().contains(UserRole.TEAMLEADER), "USER_TEST_02 was not made teamleader..");
    }

    /**
     * Tests setting the team leader with lacking authorization
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"ADMIN", "TEAMLEADER", "EMPLOYEE"})
    public void set_team_leader_unauthorized()
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");
        User u2 = userRepository.findFirstByUsername("USER_TEST_02");

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            teamService.setTeamLeader(team, u2);
        }, "Team was updated despite lacking authorization");
    }

    /**
     * Tests setting the team leader to an employee outside the department
     */
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void set_team_leader_outside() throws ProdigaGeneralExpectedException
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");
        User u2 = userRepository.findFirstByUsername("USER_TEST_03");

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            teamService.setTeamLeader(team, u2);
        }, "Team was updated despite USER_TEST_03 not being from the right department.");
    }

    /**
     * Tests setting the team leader to an employee who is already teamleader/departmentleader
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void set_team_leader_to_departmentleader()
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");
        User u2 = userRepository.findFirstByUsername("USER_TEST_02");
        Set<UserRole> u2Roles = u2.getRoles();
        u2Roles.add(UserRole.DEPARTMENTLEADER);
        u2.setRoles(u2Roles);
        User u3 = userRepository.save(u2);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            teamService.setTeamLeader(team, u3);
        }, "Team was updated despite USER_TEST_02 being a department leader..");
    }

    /**
     * Tests setting the team leader to a nonexisting DB user
     */
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void set_team_leader_to_new_object()
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");
        User u2 = new User();

        Assertions.assertThrows(RuntimeException.class, () -> {
            teamService.setTeamLeader(team, u2);
        }, "Team was updated despite User not existing in the database.");
    }

    /**
     * Tests changing the team of another department
     */
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void change_foreign_team() throws ProdigaGeneralExpectedException
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_02");
        team.setName("Cool Team");

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            teamService.saveTeam(team);
        }, "Team was updated despite the department leader not being in the correct department.");
    }

    /**
     * Tests changing the team's department
     */
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void change_team_dept() throws ProdigaGeneralExpectedException
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");
        team.setDepartment(departmentRepository.findFirstByName("DEPT_TEST_02"));

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            teamService.saveTeam(team);
        }, "Team was updated despite department being changed.");
    }
}
