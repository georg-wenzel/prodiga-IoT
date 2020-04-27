package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.services.DepartmentService;
import uibk.ac.at.prodiga.services.RaspberryPiService;
import uibk.ac.at.prodiga.services.TeamService;
import uibk.ac.at.prodiga.services.UserService;

@Component
@Scope("application")
public class DashboardController
{

    private final DepartmentService departmentService;
    private final RaspberryPiService raspberryPiService;
    private final UserService userService;
    private final TeamService teamService;

    public DashboardController(DepartmentService departmentService, RaspberryPiService raspberryPiService, UserService userService, TeamService teamService)
    {
        this.departmentService = departmentService;
        this.raspberryPiService = raspberryPiService;
        this.userService = userService;
        this.teamService = teamService;
    }

    /**
     * Returns the number of all departments.
     *
     * @return number of all departments.
     */
    public int numDepartments(){
        return departmentService.getNumDepartments();
    }

    /**
     * Returns the number of all configured raspberry pis.
     * @return number of all configured raspberry pis.
     */
    public int numRaspberryPis(){
        return this.raspberryPiService.getNumConfiguredRaspberryPis();
    }

    /**
     * Returns the number of all users.
     *
     * @return number of total users.
     */
    public int numUsers(){
        return userService.getNumUsers();
    }

    /**
     * Returns the number of all teams.
     * @return number of teams.
     */
    public int numTeams(){
        return this.teamService.getNumTeams();
    }

}
