package uibk.ac.at.prodiga.tests;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.DepartmentRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.services.DepartmentService;
import uibk.ac.at.prodiga.tests.helper.TestHelper;

import java.util.Date;

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

    @Override
    public void afterPropertiesSet() throws Exception
    {
        //Grab admin user to set as creation user for test departments and users
        User admin = userRepository.findFirstByUsername("admin");

        //Before tests, initialize test department and user
        User test_leader = new User();
        test_leader.setCreateDate(new Date());
        test_leader.setCreateUser(admin);
        test_leader.setUsername("USER_TEST_01");
        test_leader.setRoles(Sets.newSet(UserRole.DEPARTMENTLEADER));
        test_leader = userRepository.save(test_leader);

        Department dept = new Department();
        dept.setName("DEPT_TEST_01");
        dept.setObjectCreatedUser(admin);
        dept.setObjectCreatedDateTime(new Date());
        dept.setDepartmentLeader(test_leader);
        departmentRepository.save(dept);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void load_department()
    {
        Department dept = departmentService.getFirstByName("DEPT_TEST_01");
        Assert.assertNotNull("Could not load department.", dept);
    }

}
