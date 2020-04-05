package uibk.ac.at.prodiga.ui.controllers;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.services.DepartmentService;
import uibk.ac.at.prodiga.services.UserService;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.SnackbarHelper;
import uibk.ac.at.prodiga.utils.MessageType;

import java.util.Collection;

@Component
@Scope("view")
public class DepartmentController {

    private final DepartmentService departmentService;
    private Department department;
    private final UserService userService;

    public DepartmentController(DepartmentService departmentService, UserService userService) {
        this.departmentService = departmentService;
        this.userService = userService;
    }

     /**
     * Returns a collection of all departments
     * @return A collection of all departments
     */
    public Collection<Department> getAllDepartments() {
        return departmentService.getAllDepartments();
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
        departmentService.saveDepartment(department);
        SnackbarHelper.getInstance().showSnackBar("Department " + department.getId() + " saved!", MessageType.INFO);
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
        SnackbarHelper.getInstance()
                .showSnackBar("Department \"" + department.getName() + "\" deleted!", MessageType.ERROR);
    }
}