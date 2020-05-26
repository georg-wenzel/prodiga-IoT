package uibk.ac.at.prodiga.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.services.LogInformationService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.ui.controllers.LogInformationController;

import java.util.Date;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class LogInformationControllerTest {

    @Autowired
    LogInformationController logInformationController;

    @Autowired
    LogInformationService logInformationService;

    @Autowired
    UserRepository userRepository;

    User admin = null;

    @BeforeEach
    public void initEach() {
        admin = DataHelper.createAdminUser("admin", userRepository);
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN", "EMPLOYEE"})
    public void logInformationController_search_returnsCorrectResult() {
        logInformationService.logForCurrentUser("Test");

        logInformationController.search();
        Assertions.assertEquals(1, logInformationController.getResult().size());

        logInformationController.setUser(null);
        logInformationController.setEndDate(new Date(new Date().getTime() - (1000 * 60 * 60)));
        logInformationController.search();
        Assertions.assertEquals(0, logInformationController.getResult().size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN", "EMPLOYEE"})
    public void logInformationController_reset_emptyResult() {
        logInformationController.search();

        logInformationController.reset();

        Assertions.assertNull(logInformationController.getResult());
    }
}
