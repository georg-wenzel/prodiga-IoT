package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.services.TeamService;
import uibk.ac.at.prodiga.services.UserService;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;
import uibk.ac.at.prodiga.utils.SnackbarHelper;

import java.io.Serializable;
import java.util.Collection;

@Component
@Scope("view")
public class TeamController implements Serializable {

    private static final long serialVersionUID = 5327384987692577315L;

    private final TeamService teamService;
    private final UserService userService;
    private final ProdigaUserLoginManager userLoginManager;
    private Team team;
    private User teamLeader;
    private Collection<Team> teams;

    public TeamController(TeamService teamService, ProdigaUserLoginManager userLoginManager, UserService userService){
        this.teamService = teamService;
        this.userService = userService;
        this.userLoginManager = userLoginManager;
    }

    /**
     * Returns a collection of all teams visible to the calling user
     * @return a collection of all teams visible to the calling user
     */
    public Collection<Team> getAllTeams()
    {
        if(teams == null)
        {
            if(userLoginManager.getCurrentUser().getRoles().contains(UserRole.ADMIN))
                teams = teamService.getAllTeams();

            else
                teams = teamService.findTeamsOfDepartment();
        }
        return teams;
    }

    public Department getUserDept()
    {
        return userLoginManager.getCurrentUser().getAssignedDepartment();
    }

    /**
     * Returns the first team with a matching name (unique identifier)
     * @param name The name of the team
     * @return The first (and only) team with a matching name, or null if none was found
     */
    public Team getFirstByName(String name){
        return teamService.getFirstByName(name);
    }

    /**
     * Saves currently selected team
     * @throws Exception when save fails
     */
    public void saveTeam() throws Exception{
        //manually set dept if not admin
        if(!userLoginManager.getCurrentUser().getRoles().contains(UserRole.ADMIN))
            this.team.setDepartment(userLoginManager.getCurrentUser().getAssignedDepartment());

        this.team = teamService.saveTeam(team);
        if(saveTeamLeader()) {
            setTeamLeader(team, teamLeader);
        }
        SnackbarHelper.getInstance().showSnackBar("Team " + team.getId() + " saved!", MessageType.INFO);
    }

    /**
     * Returns true if the team is the same as the database state
     * @param team The team to check
     * @return True if the team is the same as in the database, false otherwise.
     */
    public boolean isTeamUnchanged(Team team){
        return teamService.isTeamUnchanged(team);
    }

    /**
     * Sets the team leader to a certain user
     * @param team The team to set the leader for
     * @param user The user to make leader
     * @throws ProdigaGeneralExpectedException If team/user are not valid, or the user cannot be made leader of this team, an exception is thrown.
     */
    public void setTeamLeader(Team team, User user) throws ProdigaGeneralExpectedException {
        this.teamService.setTeamLeader(team, user);
    }

    /**
     * Returns the team leader of a department
     * @param team to get the leader from
     * @return Leader of team.
     */
    public User getTeamLeaderOf(Team team){
        return userService.getTeamLeaderOf(team);
    }

    /**
     * Gets team by id.
     * @return the team by id
     */
    public Long getTeamById(){
        if(this.team == null){
            return (long) -1;
        }
        return this.team.getId();
    }

    /**
     * Sets current team by teamId
     * @param teamId teamId to be set
     */
    public void setTeamById(Long teamId){
        loadTeamById(teamId);
    }

    /**
     * Sets currently active team by the id
     * @param teamId when teamId could not be found
     */
    public void loadTeamById(Long teamId){
        if(teamId == null){
            this.team = teamService.createTeam();
        } else {
            this.team = teamService.loadTeam(teamId);
        }
        if(team != null && !team.isNew()) {
            teamLeader = getTeamLeaderOf(team);
        }
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public User getTeamLeader() {
        return teamLeader;
    }

    public void setTeamLeader(User teamLeader) {
        this.teamLeader = teamLeader;
    }

    private boolean saveTeamLeader() {
        return teamLeader != null
            && !teamLeader.getRoles().contains(UserRole.TEAMLEADER);
    }

    public void doDeleteTeam() throws Exception {
        this.teamService.deleteTeam(team);
        SnackbarHelper.getInstance()
                .showSnackBar("Team \"" + team.getName() + "\" deleted!", MessageType.ERROR);
    }
}
