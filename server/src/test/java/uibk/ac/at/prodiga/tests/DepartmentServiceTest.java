package uibk.ac.at.prodiga.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.DepartmentRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.services.DepartmentService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Test class for the Department Service
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class DepartmentServiceTest
{
    @Autowired
    DepartmentService departmentService;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    UserRepository userRepository;

    /**
     * Tests loading of department data
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void load_department_data()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);

        Department dept_db = departmentService.getFirstByName(dept.getName());
        Assertions.assertNotNull(dept_db, "Could not load test department.");
        Assertions.assertEquals(dept_db, dept, "Service returned object does not match DB state.");
        Assertions.assertEquals(admin, dept_db.getObjectCreatedUser(), "Creation user of test department does not match admin.");
        Assertions.assertEquals(dept.getObjectCreatedDateTime(), dept_db.getObjectCreatedDateTime(), "Creation date not loaded properly from test department.");
        Assertions.assertNull(dept_db.getObjectChangedDateTime(), "Test department changed date time should be null, but is not");
        Assertions.assertNull(dept_db.getObjectChangedUser(), "Test department changed user should be null, but is not");
    }

    /**
     * Tests unauthorized loading of department data
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "EMPLOYEE"})
    public void load_department_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            departmentService.getFirstByName("anyname");
        }, "Department loaded despite lacking authorization of ADMIN");
    }

    /**
     * Tests loading of department collection
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void load_departments()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);

        Collection<Department> depts = departmentService.getAllDepartments();
        Assertions.assertNotNull(depts, "Could not load list of departments");
        Assertions.assertTrue(depts.contains(dept), "Could not find test department within list of departments");
    }

    /**
     * Tests unauthorized loading of department collection
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "EMPLOYEE"})
    public void load_departments_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            Collection<Department> dept = departmentService.getAllDepartments();
        }, "Department collection loaded despite lacking authorization of ADMIN");
    }

    /**
     * Tests saving a department with sufficient authorization
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void save_department() throws ProdigaGeneralExpectedException
    {
        DataHelper.createAdminUser("admin", userRepository);

        Department dept = new Department();
        dept.setName("TEST_DEPARTMENT");
        dept = departmentService.saveDepartment(dept);

        Assertions.assertEquals(dept, departmentRepository.findFirstById(dept.getId()), "Created department is not equal to department loaded from database.");
        Assertions.assertEquals("admin", dept.getObjectCreatedUser().getUsername(), "Department creator admin did not become creator user of the DB object.");
        Assertions.assertTrue(new Date().getTime() -  dept.getObjectCreatedDateTime().getTime() < 1000 * 60, "DB object creation time was not properly set.");;
    }

    /**
     * Tests adding a department where the name is too short
     */
    @Test
    @WithMockUser(username = "ADMIN_TEST_01", authorities = {"ADMIN"})
    public void save_department_with_invalid_name() throws ProdigaGeneralExpectedException
    {
        Department dept = new Department();
        dept.setName("");

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            departmentService.saveDepartment(dept);
        }, "Department was able to be created despite the fact the department name was too short.");
    }


    /**
     * Tests adding a department with lacking authorizations
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"EMPLOYEE", "TEAMLEADER", "DEPARTMENTLEADER"})
    public void save_department_unauthorized()
    {
        Department dept = new Department();
        dept.setName("TEST_DEPARTMENT");

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            departmentService.saveDepartment(dept);
        }, "Department was able to be created despite lacking authorizations.");
    }

    /**
     * Tests changing a department
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void update_department() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);

        dept.setName("ANOTHERNAME");
        dept = departmentService.saveDepartment(dept);

        //check if update user and time has been set
        Assertions.assertEquals(dept.getObjectChangedUser().getUsername(), "admin", "Update User has not been properly set to admin.");
        Assertions.assertTrue((new Date()).getTime() -  dept.getObjectChangedDateTime().getTime() < 1000 * 60, "Update date not set properly for department.");

        //Check if name is updated
        Assertions.assertEquals("ANOTHERNAME", dept.getName(), "Name of test department was not updated accordingly");
    }

    /**
     * Tests getting the number of deparments
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "notadmin", authorities = {"EMPLOYEE"})
    public void get_num_departments() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);

        int numDepts = departmentService.getNumDepartments();

        for(int i=0;i<5;i++) {
            DataHelper.createRandomDepartment(admin, departmentRepository);
        }

        Assertions.assertEquals(numDepts + 5, departmentService.getNumDepartments(), "Number of departments was not updated properly.");
    }

    /**
     * Tests changing a department with lacking authentication
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "EMPLOYEE"})
    public void update_department_unauthorized()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        dept.setName("anothername");

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            departmentService.saveDepartment(dept);
        }, "Department was updated despite lacking authorization");
    }

    /**
     * Tests setting the department leader
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void set_department_leader() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        User user1 = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE, UserRole.DEPARTMENTLEADER), admin, dept, null, userRepository);
        User user2 = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE), admin, dept, null, userRepository);

        departmentService.setDepartmentLeader(dept, user2);
        //reload users

        user1 = userRepository.findFirstByUsername(user1.getUsername());
        user2 = userRepository.findFirstByUsername(user2.getUsername());

        Assertions.assertTrue(user1.getRoles().contains(UserRole.EMPLOYEE) && !user1.getRoles().contains(UserRole.DEPARTMENTLEADER), "user1 was not made employee.");
        Assertions.assertTrue(user2.getRoles().contains(UserRole.EMPLOYEE) && user2.getRoles().contains(UserRole.DEPARTMENTLEADER), "user2 was not made department leader.");
    }

    /**
     * Tests setting the department leader with lacking authorization
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "EMPLOYEE"})
    public void set_department_leader_unauthorized()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        User user = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE), admin, dept, null, userRepository);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            departmentService.setDepartmentLeader(dept, user);
        }, "Department was updated despite lacking authorization");
    }

    /**
     * Tests setting the department leader to an employee outside the department
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void set_department_leader_outside()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        Department dept2 = DataHelper.createRandomDepartment(admin, departmentRepository);
        User user = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE), admin, dept2, null, userRepository);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            departmentService.setDepartmentLeader(dept, user);
        }, "Department was updated despite test user not being from the right department.");
    }

    /**
     * Tests setting the department leader to an employee who is already teamleader/departmentleader
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void set_department_leader_to_teamleader()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);
        User user = DataHelper.createUserWithRoles(Sets.newSet(UserRole.EMPLOYEE, UserRole.TEAMLEADER), admin, dept, null, userRepository);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            departmentService.setDepartmentLeader(dept, user);
        }, "Department was updated despite user being a team leader.");
    }

    /**
     * Tests setting the department leader to a nonexisting DB user
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void set_department_leader_to_new_object()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        Department dept = DataHelper.createRandomDepartment(admin, departmentRepository);

        Assertions.assertThrows(RuntimeException.class, () -> {
            departmentService.setDepartmentLeader(dept, new User());
        }, "Department was updated despite User not existing in the database.");
    }
}
