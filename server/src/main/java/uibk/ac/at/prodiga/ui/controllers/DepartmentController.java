package uibk.ac.at.prodiga.ui.controllers;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.services.DepartmentService;
import uibk.ac.at.prodiga.services.TeamService;
import uibk.ac.at.prodiga.services.UserService;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.SnackbarHelper;
import uibk.ac.at.prodiga.utils.MessageType;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Component
@Scope("view")
public class DepartmentController implements Serializable {

    private static final long serialVersionUID = 8625687687692577315L;

    private final DepartmentService departmentService;
    private Department department;
    private Collection<Department> departments;
    private final UserService userService;
    private User departmentLeader;
    private TeamService teamService;

    public DepartmentController(DepartmentService departmentService, UserService userService, TeamService teamService) {
        this.departmentService = departmentService;
        this.userService = userService;
        this.teamService = teamService;
    }

     /**
     * Returns a collection of all departments
     * @return A collection of all departments
     */
    public Collection<Department> getAllDepartments() {
        if(departments == null) departments = departmentService.getAllDepartments();
        return departments;
    }


    /**
     * Returns the first department with a matching name (unique identifier)
     * @param name The name of the department
     * @return The first (and only) department with a matching name, or null if none was found
     */
    public Department getFirstByName(String name) {
        return departmentService.getFirstByName(name);
    }


    /**
     * Saves currently selected department
     * @throws Exception when save fails
     */
    public void doSaveDepartment() throws Exception {
        department = departmentService.saveDepartment(department);
        departmentService.setDepartmentLeader(department, departmentLeader);

        SnackbarHelper.getInstance().showSnackBar("Department " + department.getName() + " saved!", MessageType.INFO);
    }

    /**
     * Saves a department in the database. If an object with this ID already exists, overwrites the object's data at this ID
     * @param department The department to save
     * @return The new state of the object in the database.
     * @throws ProdigaGeneralExpectedException Is thrown when name is not between 2 and 20 characters, department leader is not a valid database user or department leader user is already a teamleader or departmentleader elsewhere.
     */
    public void doSaveDepartment(Department department) {
        SnackbarHelper.getInstance().showSnackBar("Department " + department.getName() + " saved!", MessageType.INFO);
    }


    /**
     * Returns true if the department is the same as the database state
     * @param department The department to check
     * @return True if the department is the same as in the database, false otherwise.
     */
    public boolean isDepartmentUnchanged(Department department) {
        return departmentService.isDepartmentUnchanged(department);
    }

    /**
     * Sets the department leader to a certain user
     * @param department The department to set the leader for
     * @param user The user to make leader
     * @throws ProdigaGeneralExpectedException If department/user are not valid, or the user cannot be made leader of this department, an exception is thrown.
     */
    public void setDepartmentLeader(Department department, User user) throws ProdigaGeneralExpectedException {
        this.departmentService.setDepartmentLeader(department, user);
    }

    /**
     * Returns the department leader of a department
     * @param department to get the leader from
     * @return Leader of department.
     */
    public User getDepartmentLeaderOf(Department department) {
        return userService.getDepartmentLeaderOf(department);
    }

    /**
     * Gets department by id.
     *
     * @return the department by id
     */
    public Long getDepartmentById() {
        if(this.department == null){
            return null;
        }
        return this.department.getId();
    }

    /**
     * Sets current department by departmentId
     * @throws Exception when department could not be found
     */
    public void setDepartmentById(Long departmentId) throws Exception{
        loadDepartmentById(departmentId);
    }

    /**
     * Sets currently active department by the id
     * @param departmentId when deparmentId could not be found
     */
    public void loadDepartmentById(Long departmentId) {
        if(departmentId != null){
            this.department = departmentService.loadDepartment(departmentId);
        } else {
            this.department = departmentService.createDepartment();
        }
        if(department != null && !department.isNew()) {
            departmentLeader = getDepartmentLeaderOf(department);
        }
    }

    /**
     * Gets department.
     *
     * @return the department
     */
    public Department getDepartment() {
        return this.department;
    }

    /**
     * Sets deparment.
     *
     * @param department
     */
    public void setDepartment(Department department) {
        this.department = department;
        loadDepartmentById(department.getId());
    }

    /**
     * Deletes the department.
     *
     */
    public void doDeleteDepartment() throws Exception {
        this.departmentService.deleteDepartment(department);
        departments = null;
        SnackbarHelper.getInstance()
                .showSnackBar("Department \"" + department.getName() + "\" deleted!", MessageType.ERROR);
    }

    /**
     * Gets the current selected department leader
     * @return The department leader as a user object
     */
    public User getDepartmentLeader() {
        return departmentLeader;
    }

    /**
     * Sets the current department leader
     * @param departmentLeader The new department leader
     */
    public void setDepartmentLeader(User departmentLeader) {
        this.departmentLeader = departmentLeader;
    }

    public Collection<Team> showTeamsofDepartment(Department department){
        return this.teamService.findTeamsOfDepartment(department);
    }

    /**
     * Removes the given user from the Department
     * @param user The user to delete
     * @throws ProdigaGeneralExpectedException When deleting is not possible
     */
    public void deleteUserFromDepartment(User user) throws ProdigaGeneralExpectedException {
        if(user == null) {
            return;
        }
        userService.assignTeam(user, null);
        userService.assignDepartment(user, null);
    }

    /**
     * Returns whether the given user may be deleted from the given department
     * @param user The user to check
     * @param d The team to check
     * @return Whether the user can be deleted
     */
    public boolean mayBeDeleteFromDepartment(User user, Department d) {
        if(d == null || user == null) {
            return false;
        }

        Team userTeam = user.getAssignedTeam();
        if(userTeam != null) {
            User teamLeader = userService.getTeamLeaderOf(userTeam);
            if(teamLeader != null &&  teamLeader.getUsername().equals(user.getUsername())) {
                return false;
            }
        }

        User deptLeader = getDepartmentLeaderOf(d);
        if(deptLeader == null) {
            return true;
        }

        return !deptLeader.getUsername().equals(user.getUsername());
    }
}