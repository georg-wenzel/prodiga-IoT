package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Department;
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

        //check that department matches the logged in users department
        if(userLoginManager.getCurrentUser().getAssignedDepartment() != null && !userLoginManager.getCurrentUser().getAssignedDepartment().equals(team.getDepartment()))
        {
            throw new ProdigaGeneralExpectedException("A team can only be created or changed within the department of the logged in user.", MessageType.ERROR);
        }

        //set appropriate fields
        if(team.isNew())
        {
            team.setObjectCreatedDateTime(new Date());
            team.setObjectCreatedUser(userLoginManager.getCurrentUser());
        }
        else
        {
            team.setObjectChangedDateTime(new Date());
            team.setObjectChangedUser(userLoginManager.getCurrentUser());

            //Check that department has not changed
            if(!team.getDepartment().equals(teamRepository.findFirstById(team.getId()).getDepartment()))
            {
                throw new ProdigaGeneralExpectedException("A team's department cannot be changed.", MessageType.ERROR);
            }
        }
        return teamRepository.save(team);
    }

    //TODO: When team is deleted, set team to null in all past bookings.
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

        //delete team
        teamRepository.delete(team);
    }

    /**
     * Sets the team leader to a certain user
     * @param team The team to set the leader for
     * @param newLeader The user to make leader
     * @throws ProdigaGeneralExpectedException If team/user are not valid, or the user cannot be made leader of this team, an exception is thrown.
     */
    @PreAuthorize("hasAuthority('DEPARTMENTLEADER')")
    public void setTeamLeader(Team team, User newLeader) throws ProdigaGeneralExpectedException
    {
        //check that user is a valid, unchanged database user
        if(!userService.isUserUnchanged(newLeader))
            throw new RuntimeException("Team leader is not a valid unchanged database user.");

        //check that Department is a valid, unchanged database entry
        if(!this.isTeamUnchanged(team))
            throw new RuntimeException("Team is not a valid unchanged database entry.");

        //User has to be a simple employee within this team.
        if(!EmployeeManagementUtil.isSimpleEmployee(newLeader))
            throw new ProdigaGeneralExpectedException("This user cannot be promoted to team leader because he already has a department- or teamleader role.", MessageType.ERROR);

        if(newLeader.getAssignedTeam() == null || !newLeader.getAssignedTeam().equals(team))
            throw new ProdigaGeneralExpectedException("This user cannot be promoted to team leader for this team, because he is not assigned to this team..", MessageType.ERROR);

        //Check if this team already has a team leader
        User oldLeader = userRepository.findTeamLeaderOf(team);
        if(oldLeader != null)
        {
            //set old user to employee
            Set<UserRole> roles = oldLeader.getRoles();
            roles.remove(UserRole.TEAMLEADER);
            roles.add(UserRole.EMPLOYEE);
            oldLeader.setRoles(roles);
            userRepository.save(oldLeader);
        }
        //Set new leader role to teamleader
        Set<UserRole> roles = newLeader.getRoles();
        roles.remove(UserRole.EMPLOYEE);
        roles.add(UserRole.TEAMLEADER);
        newLeader.setRoles(roles);
        userRepository.save(newLeader);
    }


    /**
     * Returns true if the team is the same as the database state
     * @param team The team to check
     * @return True if the team is the same as in the database, false otherwise.
     */
    public boolean isTeamUnchanged(Team team)
    {
        return team.equals(teamRepository.findFirstById(team.getId()));
    }
}
