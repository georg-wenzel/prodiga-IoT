package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.TeamRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.utils.EmployeeManagementUtil;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Service for accessing and manipulating teams.
 */
//TODO: Nur der eigene Departmentleader darf Teams im Department verändern, Teams dürfen nicht in andere Departments zugewiesen werden, abgleichen User die Teamleader werden, müssen das gleiche Department haben wie das Team. (& entsprechende Tests)
@Component
@Scope("application")
public class TeamService
{
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ProdigaUserLoginManager userLoginManager;

    public TeamService(TeamRepository teamRepository, ProdigaUserLoginManager userLoginManager, UserService userService, UserRepository userRepository)
    {
        this.teamRepository = teamRepository;
        this.userLoginManager = userLoginManager;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * Returns a collection of all teams
     * @return A collection of all teams.
     */
    @PreAuthorize("hasAuthority('DEPARTMENTLEADER')")
    public Collection<Team> getAllTeams()
    {
        return Lists.newArrayList(teamRepository.findAll());
    }

    /**
     * Gets the FIRST team with the specified team name.
     * This may NOT return a unique result, as teams can have the same name across departments.
     * @param name The name of the team
     * @return The first team in the database which has this name, or null if none exists
     */
    @PreAuthorize("hasAuthority('DEPARTMENTLEADER')")
    public Team getFirstByName(String name)
    {
        return teamRepository.findFirstByName(name);
    }

    /**
     * Gets the first team with the specified id. (Unique identifier)
     * @param id The id of the team
     * @return The team with this Id, or null if none exists
     */
    @PreAuthorize("hasAuthority('DEPARTMENTLEADER')")
    public Team getFirstById(long id)
    {
        return teamRepository.findFirstById(id);
    }

    /**
     * Saves the current team in the database. If team with this ID already exists, overwrites data of existing team in the database.
     * @param team The team to save
     * @return The new state of the team after saving in the DB
     */
    @PreAuthorize("hasAuthority('DEPARTMENTLEADER')")
    public Team saveTeam(Team team) throws ProdigaGeneralExpectedException
    {
        //check fields
        if(team.getName().length() > 20 || team.getName().length() < 2)
        {
            throw new ProdigaGeneralExpectedException("Team name must be between 2 and 20 characters.", MessageType.ERROR);
        }

        //check that team leader is a valid, unchanged database user
        User u = team.getTeamLeader();
        if(!userService.isUserUnchanged(u))
        {
            throw new ProdigaGeneralExpectedException("Team leader is not a valid unchanged database user.", MessageType.ERROR);
        }

        //set appropriate fields
        if(team.isNew())
        {
            team.setObjectCreatedDateTime(new Date());
            team.setObjectCreatedUser(userLoginManager.getCurrentUser());

            //User may not be an existing department or teamleader
            if(!EmployeeManagementUtil.isSimpleEmployee(team.getTeamLeader()))
            {
                throw new ProdigaGeneralExpectedException("The user that is set to become teamleader may not be a teamleader or department leader already.", MessageType.ERROR);
            }

            //set user to team- or departmentleader
            Set<UserRole> roles = u.getRoles();
            roles.remove(UserRole.EMPLOYEE);
            roles.add(UserRole.TEAMLEADER);
            u.setRoles(roles);

            team.setTeamLeader(userRepository.save(u));
        }
        else
        {
            team.setObjectChangedDateTime(new Date());
            team.setObjectChangedUser(userLoginManager.getCurrentUser());

            Team oldTeam = teamRepository.findFirstById(team.getId());
            User oldLeader = oldTeam.getTeamLeader();

            if (!oldLeader.getUsername().equals(team.getTeamLeader().getUsername()))
            {
                User newLeader = team.getTeamLeader();
                //new leader may not be an existing department or teamleader
                if(!EmployeeManagementUtil.isSimpleEmployee(newLeader))
                {
                    throw new ProdigaGeneralExpectedException("The user that is set to become teamleader may not be a teamleader or department leader already.", MessageType.ERROR);
                }

                //change permissions
                Set<UserRole> oldUserRoles = oldLeader.getRoles();
                oldUserRoles.remove(UserRole.TEAMLEADER);
                oldUserRoles.add(UserRole.EMPLOYEE);
                oldLeader.setRoles(oldUserRoles);

                Set<UserRole> newUserRoles = newLeader.getRoles();
                newUserRoles.remove(UserRole.EMPLOYEE);
                newUserRoles.add(UserRole.TEAMLEADER);
                newLeader.setRoles(newUserRoles);

                userRepository.save(oldLeader);
                newLeader = userRepository.save(newLeader);
                team.setTeamLeader(newLeader);
            }
        }
        return teamRepository.save(team);
    }

    /**
     * Deletes the team with this ID from the database.
     * @param team The team to delete
     */
    @PreAuthorize("hasAuthority('DEPARTMENTLEADER')")
    public void deleteTeam(Team team) throws ProdigaGeneralExpectedException
    {
        //check if this team has no users
        if(!userRepository.findAllByAssignedTeam(team).isEmpty())
        {
            throw new ProdigaGeneralExpectedException("Team cannot be deleted because it has remaining users.", MessageType.ERROR);
        }

        //check if team can be found
        Team dbTeam = teamRepository.findFirstById(team.getId());
        if(dbTeam == null)
        {
            throw new ProdigaGeneralExpectedException("Could not find team with this ID in DB", MessageType.ERROR);
        }

        //make teamleader an employee
        User leader = dbTeam.getTeamLeader();
        Set<UserRole> leaderRoles = leader.getRoles();
        leaderRoles.remove(UserRole.TEAMLEADER);
        leaderRoles.add(UserRole.EMPLOYEE);
        leader.setRoles(leaderRoles);
        userRepository.save(leader);

        //delete team
        teamRepository.delete(team);
    }
}
