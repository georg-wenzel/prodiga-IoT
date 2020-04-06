package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.services.TeamService;
import uibk.ac.at.prodiga.services.UserService;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.SnackbarHelper;

import java.util.Collection;

@Component
@Scope("view")
public class TeamController {
    private final TeamService teamService;
    private Team team;
    private final UserService userService;

    public TeamController(TeamService teamService, UserService userService){
        this.teamService = teamService;
        this.userService = userService;
    }

    /**
     * Returns a collection of all teams
     * @return a collection of all teams
     */
    public Collection<Team> getAllTeams(){
        return teamService.getAllTeams();
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
        teamService.saveTeam(team);
        SnackbarHelper.getInstance().showSnackBar("Team " + team.getId() + " saved!", MessageType.INFO);
    }

    /**
     * Saves a team in the database. If an object with this ID already exists, overwrites the object's data at this ID
     * @param team The team to save
     */
    public void saveTeam(Team team){
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
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
