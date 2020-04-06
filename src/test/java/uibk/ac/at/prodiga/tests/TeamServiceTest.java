package uibk.ac.at.prodiga.tests;

import io.jsonwebtoken.lang.Assert;
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
import uibk.ac.at.prodiga.services.TeamService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

import java.util.Collection;
import java.util.Date;

/**
 * Test class for the Team Service
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class TeamServiceTest
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
     * Tests loading of team data
     */
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void load_team_data()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User dept_leader = DataHelper.createUserWithRoles("dept_leader", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, dept_leader, teamRepository);

        Team team_db = teamService.getFirstById(team.getId());

        Assertions.assertNotNull(team_db, "Could not load test team.");

        Assertions.assertEquals(team_db, team, "Service returned object does not match DB state.");
        Assertions.assertEquals(dept_leader, team_db.getObjectCreatedUser(), "Creation user of test team does not match dept_leader.");
        Assertions.assertEquals(team.getObjectCreatedDateTime(), team_db.getObjectCreatedDateTime(), "Creation date not loaded properly from test team.");
        Assertions.assertNull(team_db.getObjectChangedDateTime(), "Test team changed date time should be null, but is not");
        Assertions.assertNull(team_db.getObjectChangedUser(), "Test team changed user should be null, but is not");
        Assertions.assertEquals(team_db.getDepartment(), team.getDepartment(), "Department not properly loaded from test team.");
        Assertions.assertEquals(team.getDepartment(), dept, "Wrong department returned for test team");
    }

    /**
     * Tests unauthorized loading of team data
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"ADMIN", "TEAMLEADER", "EMPLOYEE"})
    public void load_team_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            teamService.getFirstByName("somename");
        }, "Team loaded despite lacking authorization of DEPARTMENTLEADER");
    }

    /**
     * Tests loading of team collection
     */
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void load_teams()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User dept_leader = DataHelper.createUserWithRoles("dept_leader", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, dept_leader, teamRepository);

        Collection<Team> teams = teamService.getAllTeams();
        Assertions.assertNotNull(teams, "Could not load list of teams");
        Assertions.assertTrue(teams.contains(team), "Could not find test team within list of teams.");
    }

    /**
     * Tests unauthorized loading of team collection
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"TEAMLEADER", "EMPLOYEE"})
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
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User dept_leader = DataHelper.createUserWithRoles("TEST_DEPT_LEADER_01", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);

        Team team = new Team();
        team.setName("sometestname");
        team.setDepartment(dept);
        team = teamService.saveTeam(team);

        Assertions.assertEquals(teamRepository.findFirstById(team.getId()), team, "Created team is not equal to team loaded from database.");
        Assertions.assertEquals(dept_leader, team.getObjectCreatedUser(), "Team creator TEST_DEPT_LEADER_01 did not become creator user of the DB object.");
    }

    /**
     * Tests adding a team where the name is too short
     */
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void save_team_with_invalid_name() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        DataHelper.createUserWithRoles("TEST_DEPT_LEADER_01", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);

        Team team = new Team();
        team.setName("");
        team.setDepartment(dept);

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
        User admin = DataHelper.createAdminUser("admin", userRepository);
        DataHelper.createUserWithRoles("TEST_DEPT_LEADER_01", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);

        Team team = new Team();
        team.setName("");
        team.setDepartment(dept);

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
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User dept_leader = DataHelper.createUserWithRoles("TEST_DEPT_LEADER_01", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, dept_leader, teamRepository);

        team.setName("anothername");
        team = teamService.saveTeam(team);

        //check if update user and time has been set
        Assertions.assertEquals(dept_leader, team.getObjectChangedUser(), "Update User has not been properly set.");
        Assertions.assertTrue((new Date()).getTime() -  team.getObjectChangedDateTime().getTime() < 1000 * 60, "Update date has not been properly set.");

        //Check if name is updated
        Assertions.assertEquals("anothername", team.getName(), "Name of test team was not updated accordingly");
    }

    /**
     * Tests changing a team with lacking authentication
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"ADMIN", "TEAMLEADER", "EMPLOYEE"})
    public void update_team_unauthorized()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User dept_leader = DataHelper.createUserWithRoles("TEST_DEPT_LEADER_01", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, dept_leader, teamRepository);

        team.setName("anothername");

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
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User dept_leader = DataHelper.createUserWithRoles("TEST_DEPT_LEADER_01", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, dept_leader, teamRepository);
        DataHelper.createUserWithRoles(Sets.newSet(UserRole.DEPARTMENTLEADER), admin, dept, team, userRepository);

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
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User dept_leader = DataHelper.createUserWithRoles("TEST_DEPT_LEADER_01", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, dept_leader, teamRepository);

        teamService.deleteTeam(team);

        Assert.isNull(teamRepository.findFirstById(team.getId()), "Team was not deleted properly.");
    }

    /**
     * Tests deleting a team with lacking authorization
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"ADMIN", "TEAMLEADER", "EMPLOYEE"})
    public void delete_team_unauthorized()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User dept_leader = DataHelper.createUserWithRoles("TEST_DEPT_LEADER_01", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, dept_leader, teamRepository);

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
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User dept_leader = DataHelper.createUserWithRoles("TEST_DEPT_LEADER_01", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, dept_leader, teamRepository);
        User team_leader = DataHelper.createUserWithRoles(Sets.newSet(UserRole.TEAMLEADER), admin, dept, team, userRepository);
        User team_member = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE), admin, dept, team, userRepository);

        teamService.setTeamLeader(team, team_member);

        //reload users
        team_leader = userRepository.findFirstByUsername(team_leader.getUsername());
        team_member =  userRepository.findFirstByUsername(team_member.getUsername());

        Assertions.assertTrue(team_leader.getRoles().contains(UserRole.EMPLOYEE) && !team_leader.getRoles().contains(UserRole.TEAMLEADER), "team_leader was not made employee.");
        Assertions.assertTrue(!team_member.getRoles().contains(UserRole.EMPLOYEE) && team_member.getRoles().contains(UserRole.TEAMLEADER), "team_member was not made teamleader.");
    }

    /**
     * Tests setting the team leader with lacking authorization
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"ADMIN", "TEAMLEADER", "EMPLOYEE"})
    public void set_team_leader_unauthorized()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User dept_leader = DataHelper.createUserWithRoles("TEST_DEPT_LEADER_01", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, dept_leader, teamRepository);
        DataHelper.createUserWithRoles(Sets.newSet(UserRole.TEAMLEADER), admin, dept, team, userRepository);
        User team_member = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE), admin, dept, team, userRepository);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            teamService.setTeamLeader(team, team_member);
        }, "Team was updated despite lacking authorization");
    }

    /**
     * Tests setting the team leader to an employee outside the department
     */
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void set_team_leader_outside() throws ProdigaGeneralExpectedException
    {
        User dept_leader = DataHelper.createUserWithRoles("TEST_DEPT_LEADER_01", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Department dept2 = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, dept_leader, teamRepository);
        DataHelper.createUserWithRoles(Sets.newSet(UserRole.TEAMLEADER), admin, dept, team, userRepository);
        User team_member = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE), admin, dept2, null, userRepository);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            teamService.setTeamLeader(team, team_member);
        }, "Team was updated despite team_member not being from the right department.");
    }

    /**
     * Tests setting the team leader to an employee who is already teamleader/departmentleader
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void set_team_leader_to_departmentleader()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        User dept_leader = DataHelper.createUserWithRoles("TEST_DEPT_LEADER_01", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        dept_leader.setAssignedDepartment(dept);
        dept_leader = userRepository.save(dept_leader);
        Team team = DataHelper.createRandomTeam(dept, dept_leader, teamRepository);

        //needed for lambda expression...
        User dept_leader2 = userRepository.findFirstByUsername("TEST_DEPT_LEADER_01");

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            teamService.setTeamLeader(team, dept_leader2);
        }, "Team was updated despite dept_leader being a department leader..");
    }

    /**
     * Tests setting the team leader to a nonexisting DB user
     */
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void set_team_leader_to_new_object()
    {
        User dept_leader = DataHelper.createUserWithRoles("TEST_DEPT_LEADER_01", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, dept_leader, teamRepository);

        Assertions.assertThrows(RuntimeException.class, () -> {
            teamService.setTeamLeader(team, new User());
        }, "Team was updated despite User not existing in the database.");
    }

    /**
     * Tests changing the team of another department
     */
    @Test
    @WithMockUser(username = "TEST_DEPT_LEADER_01", authorities = {"DEPARTMENTLEADER"})
    public void change_foreign_team() throws ProdigaGeneralExpectedException
    {
        User dept_leader = DataHelper.createUserWithRoles("TEST_DEPT_LEADER_01", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        dept_leader.setAssignedDepartment(dept);
        dept_leader = userRepository.save(dept_leader);
        Department dept2 = DataHelper.createRandomDepartment(admin, departmentRepository);

        Team team = DataHelper.createRandomTeam(dept2, dept_leader, teamRepository);

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
        User dept_leader = DataHelper.createUserWithRoles("TEST_DEPT_LEADER_01", Sets.newSet(UserRole.DEPARTMENTLEADER), userRepository);
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Department dept2 = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team team = DataHelper.createRandomTeam(dept, dept_leader, teamRepository);

        team.setDepartment(dept2);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            teamService.saveTeam(team);
        }, "Team was updated despite department being changed.");
    }
}
