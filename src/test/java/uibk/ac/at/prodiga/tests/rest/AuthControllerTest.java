package uibk.ac.at.prodiga.tests.rest;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.RaspberryPiRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.rest.controller.AuthController;
import uibk.ac.at.prodiga.rest.dtos.JwtRequestDTO;
import uibk.ac.at.prodiga.tests.helper.TestHelper;
import uibk.ac.at.prodiga.utils.Constants;

import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        AuthControllerTest.class})
@SpringBootTest
@WebAppConfiguration
public class AuthControllerTest extends AbstractTestExecutionListener {

    @Autowired
    private AuthController authController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RaspberryPiRepository raspberryPiRepository;

    private RaspberryPi raspi = null;
    private User u = null;

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        TestHelper.autoWireTestClass(testContext, this);

        u = userRepository.findFirstByUsername("admin");

        raspi = new RaspberryPi();
        raspi.setInternalId("Test123");
        raspi.setPassword(Constants.PASSWORD_ENCODER.encode("test"));
        raspi.setObjectChangedDateTime(new Date());
        raspi.setObjectCreatedDateTime(new Date());
        raspi.setObjectChangedUser(u);
        raspi.setObjectCreatedUser(u);

        raspberryPiRepository.save(raspi);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void authenticate_AuthenticateNotConfiguredRaspi_ReturnsNotFound() {
        JwtRequestDTO request = new JwtRequestDTO();
        request.setInternalId("GibtEsNicht");
        request.setPassword("Egal");

        try {
            authController.createToken(request);
        } catch (ResponseStatusException ex) {
            Assert.assertEquals(ex.getStatus().value(), 404);
        }
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void authenticate_AuthenticateWrongPassword_ReturnsForbidden() {
        JwtRequestDTO request = new JwtRequestDTO();
        request.setInternalId("Test123");
        request.setPassword("GibtEsNicht");

        try {
            authController.createToken(request);
        } catch (ResponseStatusException ex) {
            Assert.assertEquals(ex.getStatus().value(), 401);
        }
    }

}
