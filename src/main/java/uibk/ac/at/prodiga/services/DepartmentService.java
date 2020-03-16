package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.repositories.DepartmentRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;

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

    public DepartmentService(DepartmentRepository departmentRepository, ProdigaUserLoginManager userLoginManager, UserService userService)
    {
        this.departmentRepository = departmentRepository;
        this.userLoginManager = userLoginManager;
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Collection<Department> getAllDepartments()
    {
        return Lists.newArrayList(departmentRepository.findAll());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Department getFirstById(Long id)
    {
        return departmentRepository.findFirstById(id);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Department getFirstByName(String name)
    {
        return departmentRepository.findFirstByName(name);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Department saveDepartment(Department department) throws ProdigaGeneralExpectedException
    {
        //check fields
        if(department.getName().length() > 20 || department.getName().length() < 2)
        {
            throw new ProdigaGeneralExpectedException("Department name must be between 2 and 20 characters.", MessageType.ERROR);
        }

        //check that user is a a valid, unchanged database user
        if(!userService.loadUser(department.getDepartmentLeader().getUsername()).equals(department.getDepartmentLeader()))
        {
            throw new ProdigaGeneralExpectedException("Department leader is not a valid database user.", MessageType.ERROR);
        }

        //set appropriate fields
        if(department.isNew())
        {
            department.setObjectCreatedDateTime(Date.from(Instant.now()));
            department.setObjectCreatedUser(userLoginManager.getCurrentUser());
        }
        else
        {
            department.setObjectChangedDateTime(Date.from(Instant.now()));
            department.setObjectChangedUser(userLoginManager.getCurrentUser());
        }

        return departmentRepository.save(department);
    }
}
