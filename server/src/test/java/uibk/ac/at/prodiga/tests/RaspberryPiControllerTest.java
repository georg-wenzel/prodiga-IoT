package uibk.ac.at.prodiga.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.exceptions.ExceptionIncludingMockitoWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.DiceRepository;
import uibk.ac.at.prodiga.repositories.RaspberryPiRepository;
import uibk.ac.at.prodiga.repositories.RoomRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.rest.controller.AuthController;
import uibk.ac.at.prodiga.services.RaspberryPiService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.ui.controllers.RaspberryPiController;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class RaspberryPiControllerTest {

    @Autowired
    RaspberryPiController raspberryPiController;

    @Autowired
    RaspberryPiService raspberryPiService;

    @Autowired
    RaspberryPiRepository raspberryPiRepository;

    @Autowired
    AuthController authController;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    DiceRepository diceRepository;

    User admin = null;

    @BeforeEach
    public void initEach(@Autowired UserRepository userRepository) {
        admin = DataHelper.createAdminUser("admin", userRepository);
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void RaspberryPiRepository_getAllPendingRaspberryPis_returnsCorrectAmount() {
        raspberryPiService.tryAddPendingRaspberry("123");
        Assertions.assertEquals(1, raspberryPiController.getAllPendingRaspberryPis().size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void RaspberryPiRepository_getAllConfiguredRaspberryPis_returnsCorrectAmount() {
        DataHelper.createRaspi("123", admin, null, raspberryPiRepository, roomRepository);
        Assertions.assertEquals(1, raspberryPiController.getAllConfiguredRaspberryPis().size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void RaspberryPiRepository_deletePendingRaspberry_pendingRaspiDeleted() throws Exception {
        raspberryPiService.tryAddPendingRaspberry("123");
        Assertions.assertEquals(1, raspberryPiController.getAllPendingRaspberryPis().size());

        RaspberryPi raspi = raspberryPiController.getAllPendingRaspberryPis().stream().findFirst().orElseThrow(Exception::new);

        raspberryPiController.deletePendingRaspberry(null);
        Assertions.assertEquals(1, raspberryPiController.getAllPendingRaspberryPis().size());
        raspberryPiController.deletePendingRaspberry(raspi);
        Assertions.assertEquals(0, raspberryPiController.getAllConfiguredRaspberryPis().size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void RaspberryPiRepository_save_raspiSaved() throws Exception {
        RaspberryPi r = DataHelper.createRaspi("123", admin, null, raspberryPiRepository, roomRepository);
        raspberryPiController.setRaspberryPi(r);
        raspberryPiController.getRaspberryPi().setInternalId("1234");
        raspberryPiController.doSaveRaspi();
        Assertions.assertNotNull(raspberryPiRepository.findFirstByInternalId("1234"));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void RaspberryPiRepository_deleteRaspi_raspiDeleted() throws Exception {
        RaspberryPi r = DataHelper.createRaspi("123", admin, null, raspberryPiRepository, roomRepository);
        raspberryPiController.delete(r);
        Assertions.assertFalse(raspberryPiRepository.findFirstByInternalId("123").isPresent());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void RaspberryPiRepository_setRaspyByInternal_RaspiCreated() {
        raspberryPiController.setRaspyByInternal("1234");
        Assertions.assertTrue(raspberryPiController.getRaspberryPi().isNew());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void RaspberryPiRepository_getRaspyByInternal_correctRaspiReturned() {
        Assertions.assertNull(raspberryPiController.getRaspyByInternal());
        RaspberryPi r = DataHelper.createRaspi("123", admin, null, raspberryPiRepository, roomRepository);
        raspberryPiController.setRaspberryPi(r);
        Assertions.assertEquals("123", raspberryPiController.getRaspyByInternal());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void RaspberryPiRepository_setById_correctRaspiReturned() throws Exception {
        Assertions.assertNull(raspberryPiController.getRaspyById());
        RaspberryPi r = DataHelper.createRaspi("123", admin, null, raspberryPiRepository, roomRepository);
        raspberryPiController.setRaspyById(r.getId());
        Assertions.assertEquals(r.getId(), raspberryPiController.getRaspyById());
    }
}
