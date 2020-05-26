package uibk.ac.at.prodiga.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.model.Vacation;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.repositories.VacationRepository;
import uibk.ac.at.prodiga.services.VacationService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.ui.controllers.BookingController;
import uibk.ac.at.prodiga.ui.controllers.VacationController;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class VacationControllerTest
{
    User admin;
    User employee;
    List<Vacation> userVacations;
    VacationController controller;

    @Autowired
    VacationService vacationService;

    @Autowired
    ProdigaUserLoginManager userLoginManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    VacationRepository vacationRepository;

    @BeforeEach
    public void init_each()
    {
        admin = DataHelper.createAdminUser("admin", userRepository);
        employee = DataHelper.createUserWithRoles("vacation_test_user_1", Sets.newSet(UserRole.EMPLOYEE), userRepository);

        userVacations = new ArrayList<>();
        //Create 3 vacations in the current year, totalling 15 vacation days
        userVacations.add(DataHelper.createTrueVacation(LocalDate.of(LocalDate.now().getYear(), 6, 15), 4, employee, vacationRepository));
        userVacations.add(DataHelper.createTrueVacation(LocalDate.of(LocalDate.now().getYear(), 3, 10), 5, employee, vacationRepository));
        userVacations.add(DataHelper.createTrueVacation(LocalDate.of(LocalDate.now().getYear(), 1, 1), 6, employee, vacationRepository));

        //Create 2 vacations in the next year, totalling 10 vacation days
        userVacations.add(DataHelper.createTrueVacation(LocalDate.of(LocalDate.now().getYear() + 1, 6, 15), 4, employee, vacationRepository));
        userVacations.add(DataHelper.createTrueVacation(LocalDate.of(LocalDate.now().getYear() + 1, 1, 1), 6, employee, vacationRepository));

        controller = new VacationController(vacationService, userLoginManager);

        //instantiate lists
        controller.getCurrentVacations();
        controller.getPastVacations();
    }

    /**
     * Tests if controller properly filters dates into past and ongoing vacations
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_1", authorities = {"EMPLOYEE"})
    public void split_vacations()
    {
        Collection<Vacation> currentVacations = controller.getCurrentVacations();
        Collection<Vacation> pastVacations = controller.getPastVacations();

        for(Vacation vacation : userVacations)
        {
            boolean pastVacation = vacation.getEndDate().before(new Date());
            Assertions.assertEquals(pastVacation, pastVacations.contains(vacation));
            Assertions.assertEquals(!pastVacation, currentVacations.contains(vacation));
        }
    }

    /**
     * Tests if controller properly stores a vacation
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_1", authorities = {"EMPLOYEE"})
    public void save_vacation() throws ProdigaGeneralExpectedException
    {
        //always valid vacation
        Vacation v = new Vacation();
        v.setBeginDate(Date.from(LocalDate.of(LocalDate.now().getYear() + 1, 10, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        v.setEndDate(Date.from(LocalDate.of(LocalDate.now().getYear() + 1, 10, 10).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        controller.setVacation(v);
        controller.doSaveVacation();


        Assertions.assertEquals(v.getBeginDate(), controller.getVacation().getBeginDate(), "Begin dates of vacations do not match up.");
        Assertions.assertEquals(v.getEndDate(), controller.getVacation().getEndDate(), "End dates of vacations do not match up.");
        Assertions.assertEquals(employee, controller.getVacation().getUser(), "Users of vacations do not match up.");
        Assertions.assertFalse(v.isNew(), "Vacation was not stored in database");
    }

    /**
     * Tests setting vacation by id
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_1", authorities = {"EMPLOYEE"})
    public void set_vacation_by_id()
    {
        Vacation v = userVacations.get(0);
        controller.setVacationById(v.getId());

        Assertions.assertEquals(v, controller.getVacation());
    }

    /**
     * Tests deleting vacation from the controller
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_1", authorities = {"EMPLOYEE"})
    public void delete_vacation() throws Exception
    {
        Vacation v = userVacations.get(0);
        controller.setVacation(v);
        controller.doDeleteVacation();

        Assertions.assertFalse(vacationRepository.findAllByUser(employee).contains(v));
    }

    /**
     * Tests getting remaining vacation days count
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "vacation_test_user_1", authorities = {"EMPLOYEE"})
    public void get_vacation_day_count() throws Exception
    {
        Assertions.assertEquals(10, controller.getCurrentYearDays());
        Assertions.assertEquals(15, controller.getNextYearDays());
    }
}
