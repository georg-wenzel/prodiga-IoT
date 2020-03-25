package uibk.ac.at.prodiga.tests;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
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
     * Sets up the test environment
     */
    @BeforeAll
    public static void init(@Autowired DepartmentService departmentService,
                     @Autowired DepartmentRepository departmentRepository,
                     @Autowired UserRepository userRepository)
    {
        //Grab admin user to set as creation user for test departments and users
        User admin = DataHelper.createAdminUser("admin", userRepository);

        //Before tests, initialize test department and user
        User test_leader = new User();
        test_leader.setCreateDate(new Date());
        test_leader.setCreateUser(admin);
        test_leader.setUsername("USER_TEST_01");
        test_leader.setRoles(Sets.newSet(UserRole.DEPARTMENTLEADER));
        test_leader = userRepository.save(test_leader);

        User test_employee = new User();
        test_employee.setCreateDate(new Date());
        test_employee.setCreateUser(admin);
        test_employee.setUsername("USER_TEST_02");
        test_employee.setRoles(Sets.newSet(UserRole.EMPLOYEE));
        userRepository.save(test_employee);

        Department dept = new Department();
        dept.setName("DEPT_TEST_01");
        dept.setObjectCreatedUser(admin);
        dept.setObjectCreatedDateTime(new Date());
        dept.setDepartmentLeader(test_leader);
        departmentRepository.save(dept);

        //Create test admin to change departments with
        User test_admin = new User();
        test_admin.setCreateDate(new Date());
        test_admin.setCreateUser(admin);
        test_admin.setUsername("ADMIN_TEST_01");
        test_admin.setRoles(Sets.newSet(UserRole.ADMIN));
        userRepository.save(test_admin);
    }

    /**
     * Tests loading of department data
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void load_department_data()
    {
        Department dept = departmentService.getFirstByName("DEPT_TEST_01");
        Assert.assertNotNull("Could not load test department DEPT_TEST_01.", dept);

        User u = userRepository.findFirstByUsername("USER_TEST_01");
        User admin = userRepository.findFirstByUsername("admin");

        Assert.assertEquals("DEPT_TEST_01 department leader does not match USER_TEST_01." ,dept.getDepartmentLeader(), u);
        Assert.assertEquals("Creation user of DEPT_TEST_01 does not match admin.", dept.getObjectCreatedUser(), admin);
        Assert.assertTrue("Creation date not loaded properly from DEPT_TEST_01.",  (new Date()).getTime() -  dept.getObjectCreatedDateTime().getTime() < 1000 * 60);
        Assert.assertNull("DEPT_TEST_01 changed date time should be null, but is not", dept.getObjectChangedDateTime());
        Assert.assertNull("DEPT_TEST_01 changed user should be null, but is not", dept.getObjectChangedUser());
    }

    /**
     * Tests unauthorized loading of department data
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "EMPLOYEE"})
    public void load_department_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            departmentService.getFirstByName("DEPT_TEST_01");
        }, "Department loaded despite lacking authorization of ADMIN");
    }

    /**
     * Tests loading of department collection
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void load_departments()
    {
        Collection<Department> depts = departmentService.getAllDepartments();
        Assert.assertNotNull("Could not load list of departments", depts);
        Assert.assertTrue("Could not find DEPT_TEST_01 within list of departments", depts.stream().anyMatch(x -> x.getName().equals("DEPT_TEST_01")));
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
    @Test
    @WithMockUser(username = "ADMIN_TEST_01", authorities = {"ADMIN"})
    public void save_department() throws ProdigaGeneralExpectedException
    {
        User u = userRepository.findFirstByUsername("USER_TEST_02");

        Department dept = new Department();
        dept.setName("DEPT_TEST_02");
        dept.setDepartmentLeader(u);
        dept = departmentService.saveDepartment(dept);

        u = userRepository.findFirstByUsername("USER_TEST_02");

        Assert.assertEquals("Created department is not equal to department loaded from database.", departmentRepository.findFirstByName("DEPT_TEST_02").getDepartmentLeader(), dept.getDepartmentLeader());
        Assert.assertEquals("Created department is not equal to department loaded from database.", departmentRepository.findFirstByName("DEPT_TEST_02").getName(), dept.getName());
        Assert.assertEquals("Department creator ADMIN_TEST_01 did not become creator user of the DB object.", departmentRepository.findFirstByName("DEPT_TEST_02").getObjectCreatedUser().getUsername(), "ADMIN_TEST_01");
        Assert.assertTrue("Test user USER_TEST_02 was not made department leader when department was created.", u.getRoles().contains(UserRole.DEPARTMENTLEADER));
    }

    /**
     * Tests adding a department where the user that should lead the department is already a department leader
     */
    @Test
    @WithMockUser(username = "ADMIN_TEST_01", authorities = {"ADMIN"})
    public void save_department_with_dept_leader()
    {
        User u = userRepository.findFirstByUsername("USER_TEST_01");

        Department dept = new Department();
        dept.setName("DEPT_TEST_02");
        dept.setDepartmentLeader(u);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            departmentService.saveDepartment(dept);
        }, "Department was able to be created despite the fact the department leader was already an existing department leader.");
    }

    /**
     * Tests adding a department where the name is too short
     */
    @Test
    @WithMockUser(username = "ADMIN_TEST_01", authorities = {"ADMIN"})
    public void save_department_with_invalid_name()
    {
        User u = userRepository.findFirstByUsername("USER_TEST_02");

        Department dept = new Department();
        dept.setName("");
        dept.setDepartmentLeader(u);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            departmentService.saveDepartment(dept);
        }, "Department was able to be created despite the fact the department name was too short.");
    }


    /**
     * Tests adding a department with lacking authorizations
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"EMPLOYEE", "TEAMLEADER", "DEPARTMENTLEADER"})
    public void save_department_unauthorized() throws ProdigaGeneralExpectedException
    {
        User u = userRepository.findFirstByUsername("USER_TEST_02");

        Department dept = new Department();
        dept.setName("DEPT_TEST_02");
        dept.setDepartmentLeader(u);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            departmentService.saveDepartment(dept);
        }, "Department was able to be created despite lacking authorizations.");
    }

    /**
     * Tests changing a department and changing the leader of the department
     */
    @Test
    @WithMockUser(username = "ADMIN_TEST_01", authorities = {"ADMIN"})
    public void update_department_change_lead() throws ProdigaGeneralExpectedException
    {
        User u = userRepository.findFirstByUsername("USER_TEST_02");

        Department dept = departmentRepository.findFirstByName("DEPT_TEST_01");
        dept.setDepartmentLeader(u);
        dept = departmentService.saveDepartment(dept);

        //check if update user and time has been set
        Assert.assertEquals("Update User has not been properly set to ADMIN_TEST_01", dept.getObjectChangedUser().getUsername(), "ADMIN_TEST_01");
        Assert.assertTrue("Creation date not set properly for DEPT_TEST_01.",  (new Date()).getTime() -  dept.getObjectChangedDateTime().getTime() < 1000 * 60);

        //Load USER_TEST_01 from DB => should no longer be department leader
        Assert.assertFalse("USER_TEST_01 is still department leader after being removed from the department lead of DEPT_TEST_01", userRepository.findFirstByUsername("USER_TEST_01").getRoles().contains(UserRole.DEPARTMENTLEADER));
        Assert.assertTrue("USER_TEST_01 has not been assigned the employee role after being removed from the department lead of DEPT_TEST_01", userRepository.findFirstByUsername("USER_TEST_01").getRoles().contains(UserRole.EMPLOYEE));

        //Load USER_TEST_02 from DB => should now be department leader, and no employee
        Assert.assertTrue("USER_TEST_02 is not department leader after being selected as department lead of DEPT_TEST_01", userRepository.findFirstByUsername("USER_TEST_02").getRoles().contains(UserRole.DEPARTMENTLEADER));
        Assert.assertFalse("USER_TEST_02 is still an employee after being set as the department lead of DEPT_TEST_01", userRepository.findFirstByUsername("USER_TEST_02").getRoles().contains(UserRole.EMPLOYEE));
    }

    /**
     * Tests changing a department without changing the leader of the department
     */
    @Test
    @WithMockUser(username = "ADMIN_TEST_01", authorities = {"ADMIN"})
    public void update_department() throws ProdigaGeneralExpectedException
    {
        Department dept = departmentRepository.findFirstByName("DEPT_TEST_01");
        dept.setName("DEPT_TEST_02");
        dept = departmentService.saveDepartment(dept);

        //check if update user and time has been set
        Assert.assertEquals("Update User has not been properly set to ADMIN_TEST_01", dept.getObjectChangedUser().getUsername(), "ADMIN_TEST_01");
        Assert.assertTrue("Creation date not set properly for DEPT_TEST_01.",  (new Date()).getTime() -  dept.getObjectChangedDateTime().getTime() < 1000 * 60);

        //Load USER_TEST_01 from DB => still department lead and not employee
        Assert.assertTrue("USER_TEST_01 is not department leader anymore after changing DEPT_TEST_01", userRepository.findFirstByUsername("USER_TEST_01").getRoles().contains(UserRole.DEPARTMENTLEADER));
        Assert.assertFalse("USER_TEST_01 has falsely been assigned the employee role after changing DEPT_TEST_01", userRepository.findFirstByUsername("USER_TEST_01").getRoles().contains(UserRole.EMPLOYEE));

        //Check if name is updated
        Assert.assertEquals("Name of DEPT_TEST_01 was not updated accordingly", dept.getName(), "DEPT_TEST_02");
    }

    /**
     * Tests changing a department where the department user is changed to someone else
     */
    @Test
    @WithMockUser(username = "ADMIN_TEST_01", authorities = {"ADMIN"})
    public void update_department_with_faulty_user() {
        Department dept = departmentRepository.findFirstByName("DEPT_TEST_01");
        dept.setName("DEPT_TEST_02");
        dept.getDepartmentLeader().setId("wrongId");

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            departmentService.saveDepartment(dept);
        }, "Department Leader ID was changed, but department was still saved successfully.");
    }

    /**
     * Tests changing a department with lacking authentication
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "EMPLOYEE"})
    public void update_department_unauthorized() {
        Department dept = departmentRepository.findFirstByName("DEPT_TEST_01");
        dept.setName("DEPT_TEST_02");

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            departmentService.saveDepartment(dept);
        }, "Department was updated despite lacking authorization");
    }
}
