package uibk.ac.at.prodiga.tests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.DepartmentRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.services.DepartmentService;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Test class for the Department Service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@WebAppConfiguration
public class DepartmentServiceTest implements InitializingBean
{
    @Autowired
    DepartmentService departmentService;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    UserRepository userRepository;

    /**
     * Sets up the test environment - executed before each test and cleaned up after each test (@DirtiesContext)
     */
    @Override
    public void afterPropertiesSet()
    {
        //Grab admin user to set as creation user for test departments and users
        User admin = userRepository.findFirstByUsername("admin");

        //Before tests, initialize test departments and users
        Department dept = new Department();
        dept.setObjectCreatedDateTime(new Date());
        dept.setObjectCreatedUser(admin);
        dept.setName("DEPT_TEST_01");
        dept = departmentRepository.save(dept);

        Department dept2 = new Department();
        dept2.setObjectCreatedDateTime(new Date());
        dept2.setObjectCreatedUser(admin);
        dept2.setName("DEPT_TEST_02");
        dept2 = departmentRepository.save(dept2);

        User test_leader = new User();
        test_leader.setUsername("USER_TEST_01");
        test_leader.setRoles(Sets.newSet(UserRole.DEPARTMENTLEADER));
        test_leader.setCreateUser(admin);
        test_leader.setCreateDate(new Date());
        test_leader.setAssignedDepartment(dept);
        userRepository.save(test_leader);

        User test_employee = new User();
        test_employee.setUsername("USER_TEST_02");
        test_employee.setRoles(Sets.newSet(UserRole.EMPLOYEE));
        test_employee.setCreateUser(admin);
        test_employee.setCreateDate(new Date());
        test_employee.setAssignedDepartment(dept);
        userRepository.save(test_employee);

        User test_employee2 = new User();
        test_employee2.setUsername("USER_TEST_03");
        test_employee2.setRoles(Sets.newSet(UserRole.EMPLOYEE));
        test_employee2.setCreateUser(admin);
        test_employee2.setCreateDate(new Date());
        test_employee2.setAssignedDepartment(dept2);
        userRepository.save(test_employee2);

        User test_admin = new User();
        test_admin.setUsername("ADMIN_TEST_01");
        test_admin.setCreateUser(admin);
        test_admin.setCreateDate(new Date());
        test_admin.setRoles(Sets.newSet(UserRole.ADMIN));
        userRepository.save(test_admin);
    }

    /**
     * Tests loading of department data
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void load_department_data()
    {
        Department dept = departmentService.getFirstByName("DEPT_TEST_01");
        Assert.assertNotNull("Could not load test department DEPT_TEST_01.", dept);

        User u = userRepository.findFirstByUsername("USER_TEST_01");
        User admin = userRepository.findFirstByUsername("admin");

        Assert.assertEquals("Creation user of DEPT_TEST_01 does not match admin.", admin, dept.getObjectCreatedUser());
        Assert.assertTrue("Creation date not loaded properly from DEPT_TEST_01.",  (new Date()).getTime() -  dept.getObjectCreatedDateTime().getTime() < 1000 * 60);
        Assert.assertNull("DEPT_TEST_01 changed date time should be null, but is not", dept.getObjectChangedDateTime());
        Assert.assertNull("DEPT_TEST_01 changed user should be null, but is not", dept.getObjectChangedUser());
    }

    /**
     * Tests unauthorized loading of department data
     */
    @DirtiesContext
    @Test(expected = org.springframework.security.access.AccessDeniedException.class)
    @WithMockUser(username = "testuser", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "EMPLOYEE"})
    public void load_department_unauthorized()
    {
        departmentService.getFirstByName("DEPT_TEST_01");
        Assert.fail("Department loaded despite lacking authorization of ADMIN");
    }

    /**
     * Tests loading of department collection
     */
    @DirtiesContext
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
    @DirtiesContext
    @Test(expected = org.springframework.security.access.AccessDeniedException.class)
    @WithMockUser(username = "testuser", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "EMPLOYEE"})
    public void load_departments_unauthorized()
    {
        Collection<Department> dept = departmentService.getAllDepartments();
        Assert.fail("Department collection loaded despite lacking authorization of ADMIN");
    }

    /**
     * Tests saving a department with sufficient authorization
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "ADMIN_TEST_01", authorities = {"ADMIN"})
    public void save_department() throws ProdigaGeneralExpectedException
    {
        User u = userRepository.findFirstByUsername("USER_TEST_02");

        Department dept = new Department();
        dept.setName("DEPT_TEST_03");
        dept = departmentService.saveDepartment(dept);

        Assert.assertEquals("Created department is not equal to department loaded from database.", dept, departmentRepository.findFirstById(dept.getId()));
        Assert.assertEquals("Department creator ADMIN_TEST_01 did not become creator user of the DB object.", "ADMIN_TEST_01", dept.getObjectCreatedUser().getUsername());
    }

    /**
     * Tests adding a department where the name is too short
     */
    @DirtiesContext
    @Test(expected = ProdigaGeneralExpectedException.class)
    @WithMockUser(username = "ADMIN_TEST_01", authorities = {"ADMIN"})
    public void save_department_with_invalid_name() throws ProdigaGeneralExpectedException
    {
        Department dept = new Department();
        dept.setName("");
        departmentService.saveDepartment(dept);

        Assert.fail("Department was able to be created despite the fact the department name was too short.");
    }


    /**
     * Tests adding a department with lacking authorizations
     */
    @DirtiesContext
    @Test(expected = org.springframework.security.access.AccessDeniedException.class)
    @WithMockUser(username = "testuser", authorities = {"EMPLOYEE", "TEAMLEADER", "DEPARTMENTLEADER"})
    public void save_department_unauthorized() throws ProdigaGeneralExpectedException
    {
        Department dept = new Department();
        dept.setName("DEPT_TEST_02");
        departmentService.saveDepartment(dept);

        Assert.fail("Department was able to be created despite lacking authorizations.");
    }

    /**
     * Tests changing a department
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "ADMIN_TEST_01", authorities = {"ADMIN"})
    public void update_department() throws ProdigaGeneralExpectedException
    {
        Department dept = departmentRepository.findFirstByName("DEPT_TEST_01");
        dept.setName("DEPT_TEST_03");
        dept = departmentService.saveDepartment(dept);

        //check if update user and time has been set
        Assert.assertEquals("Update User has not been properly set to ADMIN_TEST_01", dept.getObjectChangedUser().getUsername(), "ADMIN_TEST_01");
        Assert.assertTrue("Creation date not set properly for DEPT_TEST_01.",  (new Date()).getTime() -  dept.getObjectChangedDateTime().getTime() < 1000 * 60);

        //Check if name is updated
        Assert.assertEquals("Name of DEPT_TEST_01 was not updated accordingly", "DEPT_TEST_03", dept.getName());
    }

    /**
     * Tests changing a department with lacking authentication
     */
    @DirtiesContext
    @Test(expected = org.springframework.security.access.AccessDeniedException.class)
    @WithMockUser(username = "testuser", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "EMPLOYEE"})
    public void update_department_unauthorized() throws ProdigaGeneralExpectedException
    {
        Department dept = departmentRepository.findFirstByName("DEPT_TEST_01");
        dept.setName("DEPT_TEST_02");
        departmentService.saveDepartment(dept);

        Assert.fail("Department was updated despite lacking authorization");
    }

    /**
     * Tests setting the department leader
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void set_department_leader() throws ProdigaGeneralExpectedException
    {
        Department dept = departmentRepository.findFirstByName("DEPT_TEST_01");
        User u2 = userRepository.findFirstByUsername("USER_TEST_02");
        departmentService.setDepartmentLeader(dept, u2);
        //reload users
        User u1 = userRepository.findFirstByUsername("USER_TEST_01");
        u2 = userRepository.findFirstByUsername("USER_TEST_02");

        Assert.assertTrue("USER_TEST_01 was not made employee.", u1.getRoles().contains(UserRole.EMPLOYEE) && !u1.getRoles().contains(UserRole.DEPARTMENTLEADER));
        Assert.assertTrue("USER_TEST_02 was not made departmentleader.", !u2.getRoles().contains(UserRole.EMPLOYEE) && u2.getRoles().contains(UserRole.DEPARTMENTLEADER));
    }

    /**
     * Tests setting the department leader with lacking authorization
     */
    @DirtiesContext
    @Test(expected = org.springframework.security.access.AccessDeniedException.class)
    @WithMockUser(username = "testuser", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "EMPLOYEE"})
    public void set_department_leader_unauthorized() throws ProdigaGeneralExpectedException
    {
        Department dept = departmentRepository.findFirstByName("DEPT_TEST_01");
        User u2 = userRepository.findFirstByUsername("USER_TEST_02");
        departmentService.setDepartmentLeader(dept, u2);

        Assert.fail("Department was updated despite lacking authorization");
    }

    /**
     * Tests setting the department leader to an employee outside the department
     */
    @DirtiesContext
    @Test(expected = ProdigaGeneralExpectedException.class)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void set_department_leader_outside() throws ProdigaGeneralExpectedException
    {
        Department dept = departmentRepository.findFirstByName("DEPT_TEST_01");
        User u2 = userRepository.findFirstByUsername("USER_TEST_03");
        departmentService.setDepartmentLeader(dept, u2);

        Assert.fail("Department was updated despite USER_TEST_03 not being from the right department.");
    }

    /**
     * Tests setting the department leader to an employee who is already teamleader/departmentleader
     */
    @DirtiesContext
    @Test(expected = ProdigaGeneralExpectedException.class)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void set_department_leader_to_teamleader() throws ProdigaGeneralExpectedException
    {
        Department dept = departmentRepository.findFirstByName("DEPT_TEST_01");
        User u2 = userRepository.findFirstByUsername("USER_TEST_02");
        Set<UserRole> u2Roles = u2.getRoles();
        u2Roles.add(UserRole.TEAMLEADER);
        u2.setRoles(u2Roles);
        u2 = userRepository.save(u2);

        departmentService.setDepartmentLeader(dept, u2);

        Assert.fail("Department was updated despite USER_TEST_02 being a teamleader..");
    }

    /**
     * Tests setting the department leader to a nonexisting DB user
     */
    @DirtiesContext
    @Test(expected = RuntimeException.class)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void set_department_leader_to_new_object() throws ProdigaGeneralExpectedException
    {
        Department dept = departmentRepository.findFirstByName("DEPT_TEST_01");
        User u2 = new User();

        departmentService.setDepartmentLeader(dept, u2);

        Assert.fail("Department was updated despite User not existing in the database.");
    }
}
