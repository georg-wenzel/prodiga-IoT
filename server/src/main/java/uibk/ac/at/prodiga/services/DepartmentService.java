package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.DepartmentRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.utils.EmployeeManagementUtil;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Service for accessing and manipulating departments.
 */
@Component
@Scope("application")
public class DepartmentService
{
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ProdigaUserLoginManager userLoginManager;
    private final LogInformationService logInformationService;
    private final TeamService teamService;

    public DepartmentService(DepartmentRepository departmentRepository, UserService userService, UserRepository userRepository, ProdigaUserLoginManager userLoginManager, LogInformationService logInformationService, TeamService teamService)
    {
        this.departmentRepository = departmentRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.userLoginManager = userLoginManager;
        this.logInformationService = logInformationService;
        this.teamService = teamService;
    }

    /**
     * Returns a collection of all departments
     * @return A collection of all departments
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Collection<Department> getAllDepartments()
    {
        return Lists.newArrayList(departmentRepository.findAll());
    }

    /**
     * Returns the first department with a matching name (unique identifier)
     * @param name The name of the department
     * @return The first (and only) department with a matching name, or null if none was found
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Department getFirstByName(String name)
    {
        return departmentRepository.findFirstByName(name);
    }

    /**
     * Saves a department in the database. If an object with this ID already exists, overwrites the object's data at this ID
     * @param department The department to save
     * @return The new state of the object in the database.
     * @throws ProdigaGeneralExpectedException Is thrown when name is not between 2 and 20 characters, department leader is not a valid database user or department leader user is already a teamleader or departmentleader elsewhere.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Department saveDepartment(Department department) throws ProdigaGeneralExpectedException
    {
        //check fields
        if(department.getName().length() > 20 || department.getName().length() < 2)
        {
            throw new ProdigaGeneralExpectedException("Department name must be between 2 and 20 characters.", MessageType.ERROR);
        }

        //set appropriate fields
        if(department.isNew())
        {
            department.setObjectCreatedDateTime(new Date());
            department.setObjectCreatedUser(userLoginManager.getCurrentUser());
        }
        else
        {
            department.setObjectChangedDateTime(new Date());
            department.setObjectChangedUser(userLoginManager.getCurrentUser());
        }
        return departmentRepository.save(department);
    }

    /**
     * Sets the department leader to a certain user
     * @param department The department to set the leader for
     * @param newLeader The user to make leader
     * @throws ProdigaGeneralExpectedException If department/user are not valid, or the user cannot be made leader of this department, an exception is thrown.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public void setDepartmentLeader(Department department, User newLeader) throws ProdigaGeneralExpectedException
    {
        //check that user is a valid, unchanged database user
        if(!userService.isUserUnchanged(newLeader))
            throw new RuntimeException("Department leader is not a valid unchanged database user.");

        //check that Department is a valid, unchanged database entry
        if(!isDepartmentUnchanged(department))
            throw new RuntimeException("Department is not a valid unchanged database entry.");

        //User has to be a simple employee within this department.
        if(!EmployeeManagementUtil.isSimpleEmployee(newLeader))
            throw new ProdigaGeneralExpectedException("This user cannot be promoted to department leader because he already has a department- or teamleader role.", MessageType.ERROR);

        if(newLeader.getAssignedDepartment() == null || !newLeader.getAssignedDepartment().equals(department))
            throw new ProdigaGeneralExpectedException("This user cannot be promoted to department leader for this department, because he is not assigned to this department..", MessageType.ERROR);

        //Check if this department already has a department leader
        User oldLeader = userService.getDepartmentLeaderOf(department);
        if(oldLeader != null)
        {
            //set old user to employee
            Set<UserRole> roles = oldLeader.getRoles();
            roles.remove(UserRole.DEPARTMENTLEADER);
            oldLeader.setRoles(roles);
            userRepository.save(oldLeader);
        }
        //Set new leader role to departmentleader
        Set<UserRole> roles = newLeader.getRoles();
        roles.add(UserRole.DEPARTMENTLEADER);
        newLeader.setRoles(roles);
        userRepository.save(newLeader);
    }


    /**
     * Returns true if the department is the same as the database state
     * @param department The department to check
     * @return True if the department is the same as in the database, false otherwise.
     */
    public boolean isDepartmentUnchanged(Department department)
    {
        return department.equals(departmentRepository.findFirstById(department.getId()));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Department createDepartment()
    {
        return new Department();
    }

    /**
     * Loads a single department identified by its departmentId.
     *
     * @param departmentId the departmentId to search for
     * @return the department with the given ID
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public Department loadDepartment(Long departmentId) {
        return departmentRepository.findFirstById(departmentId);
    }

    /**
     * Deletes the department.
     *
     * @param department the department to delete
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteDepartment(Department department) throws Exception {
        checkForDepartmentDeletionOrDeactivation(department);
        departmentRepository.delete(department);
        logInformationService.log("Department " + department.getName() + " was deleted!");
    }

    public void checkForDepartmentDeletionOrDeactivation(Department department) throws ProdigaGeneralExpectedException {
        if(userService.getDepartmentLeaderOf(department) != null){
            throw new ProdigaGeneralExpectedException("You can't delete/deactivate a department with an aktive leader!", MessageType.WARNING);
        }
        if(userService.getUsersByDepartment(department) != null && !userService.getUsersByDepartment(department).isEmpty()){
            throw new ProdigaGeneralExpectedException("You can't delete/deactivate a department with aktive members!", MessageType.WARNING);
        }
        if(teamService.findTeamsOfDepartment(department) !=null && !teamService.findTeamsOfDepartment(department).isEmpty()){

            throw new ProdigaGeneralExpectedException("You can't delete/deactivate a department with aktive teams!", MessageType.WARNING);
        }
    }

}
