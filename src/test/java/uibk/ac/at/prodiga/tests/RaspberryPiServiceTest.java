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
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.Room;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.DiceRepository;
import uibk.ac.at.prodiga.repositories.RaspberryPiRepository;
import uibk.ac.at.prodiga.repositories.RoomRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.rest.controller.AuthController;
import uibk.ac.at.prodiga.services.RaspberryPiService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class RaspberryPiServiceTest {

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
    User notAdmin = null;

    @BeforeEach
    public void initEach(@Autowired UserRepository userRepository) {
        admin = DataHelper.createAdminUser("admin", userRepository);
        notAdmin = DataHelper.createUserWithRoles("notAdmin", Sets.newSet(UserRole.EMPLOYEE), userRepository);
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void raspiService_loadExistingRaspi_RaspiFound() {
        DataHelper.createRaspi("123", admin, null, raspberryPiRepository, roomRepository);

        Optional<RaspberryPi> raspi = raspberryPiService.findByInternalIdWithAuth("123");

        Assertions.assertNotNull(raspi, "Found optional raspi may not be null");
        Assertions.assertTrue(raspi.isPresent(), "Raspi not present in optional");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void raspiService_loadNotExistingRaspi_RaspiNotFound() {
        DataHelper.createRaspi("123", admin, null, raspberryPiRepository, roomRepository);

        Optional<RaspberryPi> raspi = raspberryPiService.findByInternalIdWithAuth("1234");

        Assertions.assertNotNull(raspi, "Not found optional raspi may not be null");
        Assertions.assertFalse(raspi.isPresent(), "Raspi present in optional");

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () ->
                raspberryPiService.findByInternalIdWithAuthAndThrow("1234"),
                "No exception thrown when accessing not existing raspi");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void raspiService_gettingAllConfiguredRaspis_correctNumberReturned() {
        List<RaspberryPi> raspis = raspberryPiService.getAllConfiguredRaspberryPis();

        Assertions.assertTrue(raspis.isEmpty(), "Found configured raspis, altough no raspi added");

        DataHelper.createRaspi("123", admin, null, raspberryPiRepository, roomRepository);

        raspis = raspberryPiService.getAllConfiguredRaspberryPis();

        Assertions.assertFalse(raspis.isEmpty(), "Found no configured raspis, although raspis are added");

        Assertions.assertEquals(1, raspis.size(), "Not the correct amount of although found");
    }


    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void raspiService_gettingAllNotConfiguredRaspis_correctNumberReturned() {
        List<RaspberryPi> raspis = raspberryPiService.getAllPendingRaspberryPis();

        Assertions.assertTrue(raspis.isEmpty(), "Found not configured raspis, although no raspi added");

        authController.register("123");

        raspis = raspberryPiService.getAllPendingRaspberryPis();

        Assertions.assertFalse(raspis.isEmpty(), "Could nit find not configured raspis, although no raspi added");

        Assertions.assertEquals(1, raspis.size(), "Not exactly one raspi was added");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void raspiService_saveValidRaspi_RaspiInDB() throws Exception {
        Room r = DataHelper.createRoom("test", admin, roomRepository);
        RaspberryPi raspi = new RaspberryPi();
        raspi.setInternalId("1234");
        raspi.setAssignedRoom(r);
        raspi.setPassword(DataHelper.TEST_PASSWORD);

        raspberryPiService.save(raspi);

        raspi = raspberryPiService.findByInternalIdWithAuthAndThrow("1234");

        Assertions.assertNotNull(raspi, "Saved raspi may not be null!");

        Assertions.assertNotEquals(DataHelper.TEST_PASSWORD, raspi.getPassword());

    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void raspiService_saveRaspiWithoutRoom_throwsException() {
        RaspberryPi raspi = new RaspberryPi();
        raspi.setInternalId("1234");
        raspi.setPassword(DataHelper.TEST_PASSWORD);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () ->
                raspberryPiService.save(raspi),
                "Save success without room");

    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void raspiService_saveRaspiWithoutPassword_throwsException() {
        Room r = DataHelper.createRoom("test", admin, roomRepository);
        RaspberryPi raspi = new RaspberryPi();
        raspi.setInternalId("1234");
        raspi.setAssignedRoom(r);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () ->
                        raspberryPiService.save(raspi),
                "Save success without password");

    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void raspiService_saveRaspiWithoutInternalId_throwsException() {
        Room r = DataHelper.createRoom("test", admin, roomRepository);
        RaspberryPi raspi = new RaspberryPi();
        raspi.setPassword(DataHelper.TEST_PASSWORD);
        raspi.setAssignedRoom(r);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () ->
                        raspberryPiService.save(raspi),
                "Save success without internal id");

    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void raspiService_deleteValidRaspi_RaspiNotInDB() throws Exception {
        RaspberryPi raspi = DataHelper.createRaspi("1234", admin, null,
                raspberryPiRepository, roomRepository);

        raspberryPiService.delete(raspi);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () ->
                raspberryPiService.findByInternalIdWithAuthAndThrow("1234"),
                "Deleted raspi found");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void raspiService_RaspiWithDice_throwsException() {
        RaspberryPi rapsi = DataHelper.createRaspi("1234", admin, null,
                raspberryPiRepository, roomRepository);

        DataHelper.createDice("1234", rapsi, admin, diceRepository, null, null);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () ->
                raspberryPiService.delete(rapsi),
                "Raspi saved with assigned cubes!");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void raspiService_findByInternalidWithoutRights_throwsException() {
        DataHelper.createRaspi("1234", admin, null, raspberryPiRepository, roomRepository);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> raspberryPiService.findByInternalIdWithAuth("1234"),
                "Unauthorized User can search for raspi");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void raspiService_getAllPendingRaspberryPisWithoutRights_throwsException() {
        authController.register("1234");

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> raspberryPiService.getAllPendingRaspberryPis(),
                "Unauthorized User can search for prending raspis");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void raspiService_getAllConfiguredRaspberryPisWithoutRights_throwsException() {
        DataHelper.createRaspi("1234", admin, null, raspberryPiRepository, roomRepository);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> raspberryPiService.getAllConfiguredRaspberryPis(),
                "Unauthorized User can search for all configured raspi");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void raspiService_saveWithoutRights_throwsException() {
        RaspberryPi raspi = DataHelper.createRaspi("1234", admin, null, raspberryPiRepository, roomRepository);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> raspberryPiService.save(raspi),
                "Unauthorized User can save raspi");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void raspiService_deleteWithoutRights_throwsException() {
        RaspberryPi raspi = DataHelper.createRaspi("1234", admin, null, raspberryPiRepository, roomRepository);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> raspberryPiService.delete(raspi),
                "Unauthorized User can delete raspi");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void raspiService_saveRaspiWithExistingInternalId_throws() {
        DataHelper.createRaspi("123", admin, null, raspberryPiRepository, roomRepository);

        RaspberryPi raspi = new RaspberryPi();
        raspi.setInternalId("123");

        Assertions.assertThrows(ProdigaGeneralExpectedException.class,
                () -> raspberryPiService.save(raspi),
                "Raspi can with existing internal Id can be saved");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void raspiService_saveRaspi_removedFromPending() throws Exception {
        authController.register("123");

        Assertions.assertEquals(1, raspberryPiService.getAllPendingRaspberryPis().size(),
                "Registered Raspi not in pending list");

        Room r = DataHelper.createRoom("test", admin, roomRepository);
        RaspberryPi raspi = new RaspberryPi();
        raspi.setInternalId("123");
        raspi.setAssignedRoom(r);
        raspi.setPassword(DataHelper.TEST_PASSWORD);

        raspberryPiService.save(raspi);

        Assertions.assertEquals(0, raspberryPiService.getAllPendingRaspberryPis().size(),
                "Saved Raspi still in pending list");
    }
}
