package uibk.ac.at.prodiga.ui.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.primefaces.component.selectbooleancheckbox.SelectBooleanCheckbox;
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

import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.event.AjaxBehaviorEvent;

@Component
@Scope("view")
public class UserListController implements Serializable
{
    private static final long serialVersionUID = 5325687683192577315L;

    private String userToEdit;
    private boolean historicFlagToSet;
    private Long teamIdToSet;

    private Collection<User> users;

    private final UserService userService;
    private final TeamService teamService;
    private final ProdigaUserLoginManager userLoginManager;

    public UserListController(UserService userService, TeamService teamService, ProdigaUserLoginManager userLoginManager) {
        this.userService = userService;
        this.teamService = teamService;
        this.userLoginManager = userLoginManager;
    }

    /**
     * Returns a list of all users dependent on current authorization level of the calling user.
     *
     * @return a Collection of all users.
     */
    public Collection<User> getUsers()
    {
        if(users == null) {
            User u = userLoginManager.getCurrentUser();
            if (u.getRoles().contains(UserRole.ADMIN)) {
                users = userService.getAllUsers();
            } else if (u.getRoles().contains(UserRole.DEPARTMENTLEADER)) {
                users = userService.getUsersByDepartment();
            } else if (u.getRoles().contains(UserRole.TEAMLEADER)) {
                users = userService.getUsersByTeam();
            }
        }
        return users;
    }

    /**
     * Returns all user in the given department
     * @param d The department
     * @return A list with users or a empty list if department is null
     */
    public Collection<User> getAllUsersInDepartment(Department d){
        if(d == null || d.isNew()) {
            return new ArrayList<>();
        }
        return userService.getUsersByDepartment(d);
    }

    /**
     * Returns a list with all users in the given team
     * @param t The team
     * @return A list with users
     */
    public Collection<User> getAllUsersInTeam(Team t) {
        if(t == null || t.isNew()) {
            return new ArrayList<>();
        }
        return userService.getUsersByTeam(t);
    }

    /**
     * Returns all teams of the same department as the calling user
     * @return A collection of teams.
     */
    public Collection<Team> getDepartmentTeams()
    {
        User u = userLoginManager.getCurrentUser();
        if(u.getRoles().contains(UserRole.DEPARTMENTLEADER))
        {
            return teamService.findTeamsOfDepartment();
        }
        return null;
    }

    public void setUserToEdit(String userToEdit) {
        this.userToEdit = userToEdit;
    }

    public void setHistoricFlagToSet(boolean historicFlagToSet) {
        this.historicFlagToSet = historicFlagToSet;
    }

    public void setTeamIdToSet(Long teamIdToSet) {
        this.teamIdToSet = teamIdToSet;
    }

    public void editBoxChanged(AjaxBehaviorEvent e) throws ProdigaGeneralExpectedException
    {
        SelectBooleanCheckbox source = (SelectBooleanCheckbox) e.getSource();
        Object value = source.getValue();
        userService.setEditAllowed((String) e.getComponent().getAttributes().get("userToEdit"), (boolean)value);
    }
}
