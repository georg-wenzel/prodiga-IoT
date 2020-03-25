package uibk.ac.at.prodiga.tests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.DepartmentRepository;
import uibk.ac.at.prodiga.repositories.TeamRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.services.TeamService;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Test class for the Team Service
 */
@RunWith(SpringJUnit4ClassRunner.class)
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
        User admin = userRepository.findFirstByUsername("admin");

        //Before tests, initialize 1 test team and user
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

    /**
     * Tests loading of team data
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void load_team_data()
    {
        Team team = teamService.getFirstByName("TEAM_TEST_01");
        Assert.assertNotNull("Could not load test team TEAM_TEST_01.", team);

        User admin = userRepository.findFirstByUsername("admin");

        Assert.assertEquals("Creation user of TEAM_TEST_01 does not match admin.", team.getObjectCreatedUser(), admin);
        Assert.assertTrue("Creation date not loaded properly from TEAM_TEST_01.",  (new Date()).getTime() -  team.getObjectCreatedDateTime().getTime() < 1000 * 60);
        Assert.assertNull("TEAM_TEST_01 changed date time should be null, but is not", team.getObjectChangedDateTime());
        Assert.assertNull("TEAM_TEST_01 changed user should be null, but is not", team.getObjectChangedUser());
        Assert.assertEquals("TEAM_TEST_01 department does not match DEPT_TEST_01", "DEPT_TEST_01", team.getDepartment().getName());
    }

    /**
     * Tests unauthorized loading of team data
     */
    @DirtiesContext
    @Test(expected = org.springframework.security.access.AccessDeniedException.class)
    @WithMockUser(username = "testuser", authorities = {"ADMIN", "TEAMLEADER", "EMPLOYEE"})
    public void load_team_unauthorized()
    {
        teamService.getFirstByName("TEAM_TEST_01");
        Assert.fail("Team loaded despite lacking authorization of DEPARTMENTLEADER");
    }

    /**
     * Tests loading of team collection
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void load_teams()
    {
        Collection<Team> teams = teamService.getAllTeams();
        Assert.assertNotNull("Could not load list of teams", teams);
        Assert.assertTrue("Could not find TEAM_TEST_01 within list of teams", teams.stream().anyMatch(x -> x.getName().equals("TEAM_TEST_01")));
    }

    /**
     * Tests unauthorized loading of team collection
     */
    @DirtiesContext
    @Test(expected = org.springframework.security.access.AccessDeniedException.class)
    @WithMockUser(username = "testuser", authorities = {"ADMIN", "TEAMLEADER", "EMPLOYEE"})
    public void load_teams_unauthorized()
    {
        teamService.getAllTeams();
        Assert.fail("Team collection loaded despite lacking authorization of DEPARTMENTLEADER");
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

        Assert.assertEquals("Created team is not equal to team loaded from database.", teamRepository.findFirstById(team.getId()), team);
        Assert.assertEquals("Team creator TEST_DEPT_LEADER_01 did not become creator user of the DB object.", "TEST_DEPT_LEADER_01", team.getObjectCreatedUser().getUsername());
    }

    /**
     * Tests adding a team where the name is too short
     */
    @DirtiesContext
    @Test(expected = ProdigaGeneralExpectedException.class)
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void save_team_with_invalid_name() throws ProdigaGeneralExpectedException
    {
        Team team = new Team();
        team.setName("");
        team.setDepartment(departmentRepository.findFirstByName("DEPT_TEST_01"));
        teamService.saveTeam(team);

        Assert.fail("Team was able to be created despite the fact the team name was too short.");
    }


    /**
     * Tests adding a team with lacking authorizations
     */
    @DirtiesContext
    @Test(expected = org.springframework.security.access.AccessDeniedException.class)
    @WithMockUser(username = "testuser", authorities = {"EMPLOYEE", "TEAMLEADER", "ADMIN"})
    public void save_team_unauthorized() throws ProdigaGeneralExpectedException
    {
        Team team = new Team();
        team.setName("TEAM_TEST_02");
        team.setDepartment(departmentRepository.findFirstByName("DEPT_TEST_01"));
        teamService.saveTeam(team);

        Assert.fail("Team was able to be created despite lacking authorizations.");
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
        Assert.assertEquals("Update User has not been properly set to TEST_DEPT_LEADER_01", "TEST_DEPT_LEADER_01", team.getObjectChangedUser().getUsername());
        Assert.assertTrue("Creation date not set properly for TEAM_TEST_01.",  (new Date()).getTime() -  team.getObjectChangedDateTime().getTime() < 1000 * 60);

        //Check if name is updated
        Assert.assertEquals("Name of TEAM_TEST_01 was not updated accordingly", "TEAM_TEST_03", team.getName());
    }

    /**
     * Tests changing a team with lacking authentication
     */
    @DirtiesContext
    @Test(expected = org.springframework.security.access.AccessDeniedException.class)
    @WithMockUser(username = "testuser", authorities = {"ADMIN", "TEAMLEADER", "EMPLOYEE"})
    public void update_team_unauthorized() throws ProdigaGeneralExpectedException
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");
        team.setName("TEAM_TEST_03");
        teamService.saveTeam(team);

        Assert.fail("Team was updated despite lacking authorization");
    }

    /**
     * Tests deleting a team with members
     */
    @DirtiesContext
    @Test(expected = ProdigaGeneralExpectedException.class)
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void delete_team_with_members() throws ProdigaGeneralExpectedException
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");
        teamService.deleteTeam(team);

        Assert.fail("Team was deleted despite still having members");
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
    @DirtiesContext
    @Test(expected = org.springframework.security.access.AccessDeniedException.class)
    @WithMockUser(username = "testuser", authorities = {"ADMIN", "TEAMLEADER", "EMPLOYEE"})
    public void delete_team_unauthorized() throws ProdigaGeneralExpectedException
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");
        teamService.deleteTeam(team);
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

        Assert.assertTrue("USER_TEST_01 was not made employee.", u1.getRoles().contains(UserRole.EMPLOYEE) && !u1.getRoles().contains(UserRole.TEAMLEADER));
        Assert.assertTrue("USER_TEST_02 was not made teamleader..", !u2.getRoles().contains(UserRole.EMPLOYEE) && u2.getRoles().contains(UserRole.TEAMLEADER));
    }

    /**
     * Tests setting the team leader with lacking authorization
     */
    @DirtiesContext
    @Test(expected = org.springframework.security.access.AccessDeniedException.class)
    @WithMockUser(username = "testuser", authorities = {"ADMIN", "TEAMLEADER", "EMPLOYEE"})
    public void set_team_leader_unauthorized() throws ProdigaGeneralExpectedException
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");
        User u2 = userRepository.findFirstByUsername("USER_TEST_02");
        teamService.setTeamLeader(team, u2);

        Assert.fail("Team was updated despite lacking authorization");
    }

    /**
     * Tests setting the team leader to an employee outside the department
     */
    @DirtiesContext
    @Test(expected = ProdigaGeneralExpectedException.class)
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void set_team_leader_outside() throws ProdigaGeneralExpectedException
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");
        User u2 = userRepository.findFirstByUsername("USER_TEST_03");
        teamService.setTeamLeader(team, u2);

        Assert.fail("Team was updated despite USER_TEST_03 not being from the right department.");
    }

    /**
     * Tests setting the team leader to an employee who is already teamleader/departmentleader
     */
    @DirtiesContext
    @Test(expected = ProdigaGeneralExpectedException.class)
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void set_team_leader_to_departmentleader() throws ProdigaGeneralExpectedException
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");
        User u2 = userRepository.findFirstByUsername("USER_TEST_02");
        Set<UserRole> u2Roles = u2.getRoles();
        u2Roles.add(UserRole.DEPARTMENTLEADER);
        u2.setRoles(u2Roles);
        u2 = userRepository.save(u2);

        teamService.setTeamLeader(team, u2);

        Assert.fail("Team was updated despite USER_TEST_02 being a department leader..");
    }

    /**
     * Tests setting the team leader to a nonexisting DB user
     */
    @DirtiesContext
    @Test(expected = RuntimeException.class)
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void set_team_leader_to_new_object() throws ProdigaGeneralExpectedException
    {
        Team team = teamRepository.findFirstByName("TEAM_TEST_01");
        User u2 = new User();

        teamService.setTeamLeader(team, u2);

        Assert.fail("Team was updated despite User not existing in the database.");
    }
}
