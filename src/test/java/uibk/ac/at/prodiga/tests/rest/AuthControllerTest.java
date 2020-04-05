package uibk.ac.at.prodiga.tests.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.RaspberryPiRepository;
import uibk.ac.at.prodiga.repositories.RoomRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.rest.controller.AuthController;
import uibk.ac.at.prodiga.rest.dtos.JwtRequestDTO;
import uibk.ac.at.prodiga.rest.dtos.JwtResponseDTO;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.utils.Constants;

import javax.xml.crypto.Data;
import java.util.Date;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class AuthControllerTest {

    private static final String PASSWORD = DataHelper.TEST_PASSWORD;
    private static final String INTERNAL_ID = "Test123";

    @Autowired
    private AuthController authController;

    /**
     * Initializes the test with test data
     * @param userRepository userRepository for creating users
     * @param raspberryPiRepository raspberryPiRepository for creating raspberry pis
     */
    @BeforeAll
    public static void init(@Autowired UserRepository userRepository,
                            @Autowired RaspberryPiRepository raspberryPiRepository,
                            @Autowired RoomRepository roomRepository)  {
        User u = DataHelper.createAdminUser("admin", userRepository);

        DataHelper.createRaspi(INTERNAL_ID, u, null, raspberryPiRepository, roomRepository);
    }

    /**
     * Tests whether a Raspi which is not registered can create an acess token
     */
    @Test
    public void authenticate_AuthenticateNotConfiguredRaspi_ReturnsNotFound() {
        JwtRequestDTO request = new JwtRequestDTO();
        request.setInternalId("GibtEsNicht");
        request.setPassword("Egal");

        try {
            authController.createToken(request);
            Assertions.fail("Token sucessfull created with not configured raspi");
        } catch (ResponseStatusException ex) {
            Assertions.assertEquals(ex.getStatus().value(), 404);
        }
    }

    /**
     *Tests whether a raspi which has the wriong password can create an access token
     */
    @Test
    public void authenticate_AuthenticateWrongPassword_ReturnsForbidden() {
        JwtRequestDTO request = new JwtRequestDTO();
        request.setInternalId(INTERNAL_ID);
        request.setPassword("GibtEsNicht");

        try {
            authController.createToken(request);
            Assertions.fail("Token sucessull created with wrong password");
        } catch (ResponseStatusException ex) {
            Assertions.assertEquals(ex.getStatus().value(), 401);
        }
    }

    /**
     * Tests whether a valid configured raspi and registered raspi can create a token
     */
    @Test
    public void authenticate_ValidRequest_ReturnsToken() {
        JwtRequestDTO request = new JwtRequestDTO();
        request.setInternalId(INTERNAL_ID);
        request.setPassword(PASSWORD);

        JwtResponseDTO response = authController.createToken(request);

        Assertions.assertNotNull(response, "authController.createToken(request) response may not be null.");
        Assertions.assertFalse(StringUtils.isEmpty(response.getToken()), "token in response may not be empty!");
    }
}
