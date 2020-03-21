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
    private final UserService userService;
    private final ProdigaUserLoginManager userLoginManager;

    public DepartmentService(DepartmentRepository departmentRepository, UserService userService, ProdigaUserLoginManager userLoginManager)
    {
        this.departmentRepository = departmentRepository;
        this.userService = userService;
        this.userLoginManager = userLoginManager;
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

        User u = department.getDepartmentLeader();
        //check that user is a a valid, unchanged database user
        if(!userService.isUserUnchanged(u))
        {
            throw new ProdigaGeneralExpectedException("Department leader is not a valid unchanged database user.", MessageType.ERROR);
        }

        //set appropriate fields
        if(department.isNew())
        {
            department.setObjectCreatedDateTime(new Date());
            department.setObjectCreatedUser(userLoginManager.getCurrentUser());

            //User may not be an existing department or teamleader
            if(!EmployeeManagementUtil.isSimpleEmployee(u))
            {
                throw new ProdigaGeneralExpectedException("The user that is set to become department leader may not be a teamleader or department leader already.", MessageType.ERROR);
            }

            //set user to department leader
            Set<UserRole> roles = u.getRoles();
            roles.remove(UserRole.EMPLOYEE);
            roles.add(UserRole.DEPARTMENTLEADER);
            u.setRoles(roles);
            department.setDepartmentLeader(userService.saveUser(u));
        }
        else
        {
            department.setObjectChangedDateTime(new Date());
            department.setObjectChangedUser(userLoginManager.getCurrentUser());

            Department oldDept = departmentRepository.findFirstById(department.getId());
            User oldLeader = oldDept.getDepartmentLeader();

            if(!oldLeader.getUsername().equals(department.getDepartmentLeader().getUsername()))
            {
                User newLeader = department.getDepartmentLeader();
                //new leader may not be an existing department or teamleader
                if(!EmployeeManagementUtil.isSimpleEmployee(newLeader))
                {
                    throw new ProdigaGeneralExpectedException("The user that is set to become department leader may not be a teamleader or department leader already.", MessageType.ERROR);
                }

                //change permissions
                Set<UserRole> oldUserRoles = oldLeader.getRoles();
                oldUserRoles.remove(UserRole.DEPARTMENTLEADER);
                oldUserRoles.add(UserRole.EMPLOYEE);
                oldLeader.setRoles(oldUserRoles);

                Set<UserRole> newUserRoles = newLeader.getRoles();
                newUserRoles.remove(UserRole.EMPLOYEE);
                newUserRoles.add(UserRole.DEPARTMENTLEADER);
                newLeader.setRoles(newUserRoles);

                userService.saveUser(oldLeader);
                newLeader = userService.saveUser(newLeader);
                department.setDepartmentLeader(newLeader);
            }
        }

        return departmentRepository.save(department);
    }
}
