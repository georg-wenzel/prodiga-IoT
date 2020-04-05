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

    public Collection<Team> getAllTeams(){
        return teamService.getAllTeams();
    }

    public Team getFirstByName(String name){
        return teamService.getFirstByName(name);
    }

    public void saveTeam() throws Exception{
        teamService.saveTeam(team);
        SnackbarHelper.getInstance().showSnackBar("Team " + team.getId() + " saved!", MessageType.INFO);
    }

    public void saveTeam(Team team){
        SnackbarHelper.getInstance().showSnackBar("Team " + team.getId() + " saved!", MessageType.INFO);
    }

    public boolean isTeamUnchanged(Team team){
        return teamService.isTeamUnchanged(team);
    }

    public void setTeamLeader(Team team, User user) throws ProdigaGeneralExpectedException {
        this.teamService.setTeamLeader(team, user);
    }

    public User getTeamLeaderOf(Team team){
        return userService.getTeamLeaderOf(team);
    }

    public Long getTeamById(){
        if(this.team == null){
            return (long) -1;
        }
        return this.team.getId();
    }

    public void setTeamById(Long teamId){
        loadTeamById(teamId);
    }

    private void loadTeamById(Long teamId){
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
