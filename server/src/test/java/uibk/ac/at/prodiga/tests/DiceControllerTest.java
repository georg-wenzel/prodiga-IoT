package uibk.ac.at.prodiga.tests;

import org.assertj.core.util.Lists;
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
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.model.UserRole;
import uibk.ac.at.prodiga.repositories.DiceRepository;
import uibk.ac.at.prodiga.repositories.RaspberryPiRepository;
import uibk.ac.at.prodiga.repositories.RoomRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.rest.dtos.PendingDiceDTO;
import uibk.ac.at.prodiga.services.DiceService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.ui.controllers.DiceController;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class DiceControllerTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    DiceController diceController;

    @Autowired
    DiceRepository diceRepository;

    @Autowired
    RaspberryPiRepository raspberryPiRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    DiceService diceService;

    User admin = null;
    User notAdmin = null;
    Dice d = null;

    @BeforeEach
    public void initEach() {
        admin = DataHelper.createAdminUser("admin", userRepository);
        notAdmin = DataHelper.createUserWithRoles("notAdmin", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        d = DataHelper.createDice("123", null, admin, diceRepository, raspberryPiRepository, roomRepository);
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceController_getDiceUser_correctUserNameReturned() {
        Assertions.assertEquals("", diceController.getDiceUser());
        diceController.setDice(d);
        Assertions.assertEquals("admin", diceController.getDiceUser());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceController_setDiceUser_correctUserNameReturned() {
        diceController.setDice(d);
        diceController.setDiceUser("notAdmin");
        Assertions.assertEquals("notAdmin", diceController.getDiceUser());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceController_getAllDices_correctAmountReturned() {
        Assertions.assertEquals(1, diceController.getAllDices().size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceController_saveDice_diceSaved() throws Exception {
        diceController.setDice(d);
        diceController.getDice().setLastBatteryStatus(10);
        diceController.doSaveDice();
        Assertions.assertEquals(10, diceRepository.findFirstByInternalId("123").getLastBatteryStatus());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceController_getAllPendingDices_correctAmountReturned() {
        PendingDiceDTO dto = new PendingDiceDTO();
        dto.setRaspiInternalId("123");
        dto.setDiceInternalId("1234");
        diceService.addDicesToPending(Lists.newArrayList(dto));
        Assertions.assertEquals(1, diceController.getAllPendingDices().size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceController_savePendingDice_diceSaved() throws ProdigaGeneralExpectedException {
        PendingDiceDTO dto = new PendingDiceDTO();
        dto.setRaspiInternalId("123");
        dto.setDiceInternalId("12345");
        diceService.addDicesToPending(Lists.newArrayList(dto));
        Dice d = diceController.getAllPendingDices().get(0);
        diceController.savePendingDice(d);
        Assertions.assertNotNull(diceRepository.findFirstByInternalId("12345"));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceController_getSetDiceById_DiceSetCorrectly() throws Exception {
        diceController.setDiceById(null);
        Assertions.assertTrue(diceController.getDice().isNew());

        diceController.setDiceById(d.getId());

        Assertions.assertEquals(d.getId(), diceController.getDiceById());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceController_deleteDice_diceDeleted() throws Exception {
        diceController.setDice(d);
        diceController.deleteDice(d);
        Assertions.assertNull(diceRepository.findFirstByInternalId("123"));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceController_getDiceBatteryInfoForCurrentUser_correctBatteryReturned() {
        diceController.setDice(d);
        Assertions.assertEquals("n/a", diceController.getDiceBatteryInfoForCurrentUser());
    }
}
