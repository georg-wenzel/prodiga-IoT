package uibk.ac.at.prodiga.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.repositories.VacationRepository;
import uibk.ac.at.prodiga.services.VacationService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;

/**
 * Test class for the Vacation Service
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class VacationServiceTest
{
    @Autowired
    UserRepository userRepository;

    @Autowired
    VacationRepository vacationRepository;

    @Autowired
    VacationService vacationService;

    /**
     * Tests loading of vacation data
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void load_vacation_data()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Vacation v1 = DataHelper.createVacation(5,10, u1, vacationRepository);

        Date fromDate = Date.from(LocalDate.now().plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate = Date.from(LocalDate.now().plusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Vacation v1_service = vacationService.getVacationById(v1.getId());

        Assertions.assertNotNull(v1_service, "Could not load test vacation.");
        Assertions.assertEquals(v1.getUser(), v1_service.getUser(), "Service returned user does not match DB state.");
        Assertions.assertEquals(v1.getBeginDate(), v1_service.getBeginDate(), "Service returned begin date does not match DB state.");
        Assertions.assertEquals(v1.getEndDate(), v1_service.getEndDate(), "Service returned end date does not match DB state.");
        Assertions.assertEquals(fromDate, v1_service.getBeginDate(), "Begin Date stored in database does not match up with target date.");
        Assertions.assertEquals(toDate, v1_service.getEndDate(), "End Date stored in database does not match up with target date.");
        Assertions.assertEquals(u1, v1_service.getUser(), "User was not correctly stored in DB.");
    }

    /**
     * Tests unauthorized loading of vacation data
     */
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"TEAMLEADER", "DEPARTMENTLEADER", "ADMIN"})
    public void load_vacation_data_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            vacationService.getVacationById((long)1);
        }, "Vacation loaded despite lacking authorization of EMPLOYEE");
    }

    /**
     * Tests loading all vacations for currently logged in user.
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"EMPLOYEE"})
    public void load_vacations()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("vacation_test_user_01", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        User u2 = DataHelper.createUserWithRoles("vacation_test_user_02", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Vacation v1 = DataHelper.createVacation(5,10, u1, vacationRepository);
        Vacation v2 = DataHelper.createVacation(14,21, u1, vacationRepository);
        Vacation v3 = DataHelper.createVacation(12,15,u2,vacationRepository);

        Collection<Vacation> vacs = vacationService.getAllVacations();

        Assertions.assertTrue(vacs.contains(v1), "Vacation v1 not found in vacation set for user u1.");
        Assertions.assertTrue(vacs.contains(v2), "Vacation v2 not found in vacation set for user u1.");
        Assertions.assertFalse(vacs.contains(v3), "Vacation v3 found in vacation set for user u1, but should not be.");
    }

    /**
     * Tests unauthorized loading of all user vacations
     */
    @Test
    @WithMockUser(username = "vacation_test_user_01", authorities = {"TEAMLEADER", "DEPARTMENTLEADER", "ADMIN"})
    public void load_vacations_unauthorized()
    {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            vacationService.getAllVacations();
        }, "Vacations loaded despite lacking authorization of EMPLOYEE");
    }

}
