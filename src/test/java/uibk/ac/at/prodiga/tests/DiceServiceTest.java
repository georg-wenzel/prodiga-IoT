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
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.DiceRepository;
import uibk.ac.at.prodiga.repositories.RaspberryPiRepository;
import uibk.ac.at.prodiga.repositories.RoomRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.services.DiceService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class DiceServiceTest {

    @Autowired
    DiceService diceService;

    @Autowired
    DiceRepository diceRepository;

    @Autowired
    RaspberryPiRepository raspberryPiRepository;

    @Autowired
    RoomRepository roomRepository;

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
    public void diceService_loadExisitingDiceById_DiceFound() throws ProdigaGeneralExpectedException {
        Dice dice = DataHelper.createDice("123", null, admin, diceRepository, raspberryPiRepository, roomRepository);

        Dice found = diceService.loadDice(dice.getId());

        Assertions.assertNotNull(found, "Saved dice not found");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_loadExisitingDiceByInternalId_DiceFound() {
        DataHelper.createDice("123", null, admin, diceRepository, raspberryPiRepository, roomRepository);

        Dice found = diceService.getDiceByInternalId("123");

        Assertions.assertNotNull(found, "Saved dice not found");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_loadNotExisitingDiceById_DiceNotFound() {
        Dice dice = DataHelper.createDice("123", null, admin, diceRepository, raspberryPiRepository, roomRepository);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            diceService.loadDice(dice.getId() + 1);
        }, "Not saved dice found");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_loadNotExisitingDiceByInternalId_DiceNotFound() {
        DataHelper.createDice("123", null, admin, diceRepository, raspberryPiRepository, roomRepository);

        Dice found = diceService.getDiceByInternalId("1234");

        Assertions.assertNull(found, "Not saved dice found");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_loadAllDice_correctNumberFound() {
        int number = 4;

        for(int i = 0; i < number; i++) {
            DataHelper.createDice(String.valueOf(i), null, admin, diceRepository, raspberryPiRepository, roomRepository);
        }

        Assertions.assertEquals(number, diceService.getAllDice().size(),
                "Correct number of dice not found");

        DataHelper.createDice(String.valueOf(number), null, admin, diceRepository, raspberryPiRepository, roomRepository);

        Assertions.assertEquals(number + 1, diceService.getAllDice().size(),
                "Correct number of dice not found");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_loadByUser_correctDiceReturned() {
        DataHelper.createDice("123", null, admin, diceRepository, raspberryPiRepository, roomRepository);

        Dice found = diceService.getDiceByUser(admin);

        Assertions.assertNotNull(found, "Found dice may not be null");

        Assertions.assertEquals(admin.getUsername(), found.getUser().getUsername(), "Not the same user found");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_loadByRaspi_correctNumberReturned() {
        RaspberryPi raspi = DataHelper.createRaspi("123", admin, null, raspberryPiRepository, roomRepository);

        int number = 4;

        for(int i = 0; i < number; i++) {
            DataHelper.createDice(String.valueOf(i), raspi, admin, diceRepository, raspberryPiRepository, roomRepository);
        }

        Assertions.assertEquals(number, diceService.getAllByRaspberryPi(raspi).size(),
                "Not the same amount of dice found");

        DataHelper.createDice("Nero Forte", raspi, admin, diceRepository, raspberryPiRepository, roomRepository);
        DataHelper.createDice("Anti Social", null, admin, diceRepository, raspberryPiRepository, roomRepository);

        Assertions.assertEquals(number + 1, diceService.getAllByRaspberryPi(raspi).size(),
                "Not the same amount of dice found");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_getAllAvailableDice_returnsCorrectAmount() {
        Dice d = DataHelper.createDice("Sway", null, admin, diceRepository, raspberryPiRepository, roomRepository);

        d.setUser(admin);
        d = diceRepository.save(d);

        Assertions.assertEquals(0, diceService.getAllAvailableDices().size(),
                "Correct amount of dice found");

        d.setUser(admin);
        d.setActive(false);
        d = diceRepository.save(d);

        Assertions.assertEquals(0, diceService.getAllAvailableDices().size(),
                "Correct amount of dice found");

        d.setUser(null);
        d.setActive(true);
        diceRepository.save(d);

        Assertions.assertEquals(1, diceService.getAllAvailableDices().size(),
                "Correct amount of dice found");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_saveInactiveDice_diceInDB() throws ProdigaGeneralExpectedException {
        Dice d = new Dice();
        d.setActive(false);

        diceService.save(d);

        Assertions.assertEquals(1, diceService.getAllDice().size(),
                "Dice not saved");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_saveDiceWithoutRaspi_throws() {
        Dice d = new Dice();
        d.setActive(true);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class,
                () -> diceService.save(d), "Active dice without raspi can be saved");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_saveDiceWithoutInternalId_throws() {
        RaspberryPi raspi = DataHelper.createRaspi("123", admin, null, raspberryPiRepository, roomRepository);

        Dice d = new Dice();
        d.setActive(true);
        d.setAssignedRaspberry(raspi);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class,
                () -> diceService.save(d), "Active dice without internal Id can be saved");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_saveDiceWithAssignedUser_throws() {
        RaspberryPi raspi = DataHelper.createRaspi("123", admin, null, raspberryPiRepository, roomRepository);
        DataHelper.createDice("3", raspi, admin, diceRepository, null, null);

        Dice d = new Dice();
        d.setActive(true);
        d.setAssignedRaspberry(raspi);
        d.setInternalId("Under the Bridge");
        d.setUser(admin);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class,
                () -> diceService.save(d), "Active dice where user already has dice can be saved");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_saveExistingDiceWithAssignedUser_throws() throws ProdigaGeneralExpectedException {
        RaspberryPi raspi = DataHelper.createRaspi("123", admin, null, raspberryPiRepository, roomRepository);
        DataHelper.createDice("3", raspi, admin, diceRepository, null, null);

        Dice d = new Dice();
        d.setActive(true);
        d.setAssignedRaspberry(raspi);
        d.setInternalId("Under the Bridge");

        Dice d2 = diceService.save(d);

        d2.setUser(admin);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class,
                () -> diceService.save(d2), "Active dice where user already has dice can be saved");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void diceService_getAllWithoutRights_throws() {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> diceService.getAllDice(), "Unauthorized user can see all dice");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void diceService_saveWithoutRights_throws() {
        Dice d = new Dice();

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> diceService.save(d), "Unauthorized user can save");
    }
}
