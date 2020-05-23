package uibk.ac.at.prodiga.tests;

import org.assertj.core.util.Lists;
import org.javatuples.Pair;
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
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.repositories.*;
import uibk.ac.at.prodiga.rest.controller.DiceRestController;
import uibk.ac.at.prodiga.rest.dtos.NewDiceSideRequestDTO;
import uibk.ac.at.prodiga.rest.dtos.PendingDiceDTO;
import uibk.ac.at.prodiga.services.BookingService;
import uibk.ac.at.prodiga.services.DiceService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.utils.Constants;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.DiceConfigurationWrapper;

import java.util.*;

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

    @Autowired
    BookingCategoryRepository bookingCategoryRepository;

    @Autowired
    DiceSideRepository diceSideRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DiceRestController diceRestController;

    @Autowired
    BookingRepository bookingRepository;

    User admin = null;
    User notAdmin = null;

    @BeforeEach
    public void initEach() {
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

        Dice found = diceService.getDiceByInternalIdWithAuth("123");

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

        Dice found = diceService.getDiceByInternalIdWithAuth("1234");

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
        d.setObjectCreatedUser(admin);

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
        DataHelper.createDice("123", null, admin, diceRepository, raspberryPiRepository, roomRepository);

        Assertions.assertEquals(0, diceService.getAllDice().size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void diceService_saveWithoutRights_throws() {
        Dice d = new Dice();

        Assertions.assertThrows(ProdigaGeneralExpectedException.class,
                () -> diceService.save(d), "Unauthorized user can save");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void diceService_completeConfigurationWithoutSides_() {
        DataHelper.createBookingCategory("test", admin, bookingCategoryRepository);
        Dice d = DataHelper.createDice("1234", null, admin, diceRepository, raspberryPiRepository, roomRepository);
        diceService.addDiceToConfiguration(d);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> diceService.completeConfiguration(d), "Can complete without sides");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void diceService_completeConfigurationWithInvalidSides_throws() {
        Dice d = DataHelper.createDice("1234", null, admin, diceRepository, raspberryPiRepository, roomRepository);
        DiceConfigurationWrapper wrapper = diceService.addDiceToConfiguration(d);
        Map<Integer, Pair<Integer, BookingCategory>> sides = new HashMap<>();

        for(int i = 0; i < 5; i++) {
            sides.put(i, new Pair<>(i, DataHelper.createBookingCategory("test" + i, admin, bookingCategoryRepository)));
        }

        sides.remove(0);

        wrapper.setCompletedSides(sides);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> diceService.completeConfiguration(d), "Can complete without sides");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void diceService_completeConfigurationValid_diceConfigured() throws ProdigaGeneralExpectedException {
        Dice d = DataHelper.createDice("1234", null, admin, diceRepository, raspberryPiRepository, roomRepository);

        DiceConfigurationWrapper wrapper = diceService.addDiceToConfiguration(d);
        Map<Integer, Pair<Integer, BookingCategory>> sides = new HashMap<>();

        for(int i = 0; i < 5; i++) {
            sides.put(i, new Pair<>(i, DataHelper.createBookingCategory("test" + i, admin, bookingCategoryRepository)));
        }

        wrapper.setCompletedSides(sides);

        diceService.completeConfiguration(d);

        ArrayList<DiceSide> all = Lists.newArrayList(diceSideRepository.findAll());

        Assertions.assertEquals(5, all.size());

        Assertions.assertTrue(all.stream().allMatch(x -> x.getDice().getId().equals(d.getId())));

        Assertions.assertFalse(diceService.diceInConfigurationMode(d.getInternalId()), "Dice in configuration mode");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void diceService_configurationWorkflow_diceConfigured() throws ProdigaGeneralExpectedException {
        Dice d = DataHelper.createDice("123",null, admin, diceRepository, raspberryPiRepository, roomRepository);

        BookingCategory[] cats = new BookingCategory[12];
        for(int i=0;i<12;i++)
        {
            cats[i] = DataHelper.createBookingCategory("test" + i, admin, bookingCategoryRepository);
        }

        NewDiceSideRequestDTO request = new NewDiceSideRequestDTO();
        request.setInternalId("123");
        request.setSide(1);

        diceService.addDiceToConfiguration(d);

        diceService.registerNewSideCallback(UUID.randomUUID(), x -> {
            DiceConfigurationWrapper wrapper = x.getValue1();
            Optional<BookingCategory> mandatoryCat = bookingCategoryRepository.findById(Constants.DO_NOT_BOOK_BOOKING_CATEGORY_ID);
            if(wrapper.getCurrentSide() == 12 && mandatoryCat.isPresent())
            {
                wrapper.getCompletedSides().put(wrapper.getCurrentSide(), new Pair<>(wrapper.getCurrentSide(), mandatoryCat.get()));
            }
            wrapper.getCompletedSides().put(wrapper.getCurrentSide(), new Pair<>(wrapper.getCurrentSide(), cats[wrapper.getCurrentSide() - 1]));
        });


        Assertions.assertTrue(diceService.diceInConfigurationMode(d.getInternalId()), "Dice not in configuration mode");

        diceRestController.notifyNewSide(request);

        for(int i = 0; i < 12; i++) {
            request.setSide(i + 1);
            diceRestController.notifyNewSide(request);
        }

        diceService.completeConfiguration(d);

        ArrayList<DiceSide> all = Lists.newArrayList(diceSideRepository.findAll());

        Assertions.assertEquals(12, all.size());

        Assertions.assertTrue(all.stream().allMatch(x -> x.getDice().getId().equals(d.getId())));

        Assertions.assertFalse(diceService.diceInConfigurationMode(d.getInternalId()), "Dice in configuration mode");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_addToPending_inPending(){
        DataHelper.createRaspi("test", admin, null, raspberryPiRepository, roomRepository);

        List<PendingDiceDTO> pendings = new ArrayList<>();
        PendingDiceDTO p = new PendingDiceDTO();
        p.setDiceInternalId("123");
        p.setRaspiInternalId("test");
        pendings.add(p);
        diceRestController.register(pendings);

        Assertions.assertEquals(1, diceService.getPendingDices().size(), "Dice not in pending list");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_addToPendingWhichExists_notInPending(){
        DataHelper.createRaspi("test", admin, null, raspberryPiRepository, roomRepository);

        List<PendingDiceDTO> pendings = new ArrayList<>();
        PendingDiceDTO p = new PendingDiceDTO();
        p.setDiceInternalId("123");
        p.setRaspiInternalId("test");
        pendings.add(p);
        diceRestController.register(pendings);

        Assertions.assertEquals(1, diceService.getPendingDices().size(), "Dice not in pending list");

        diceRestController.register(pendings);

        Assertions.assertEquals(1, diceService.getPendingDices().size(), "Dice added to list");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_addToPendingWhichExistsInDB_notInPending(){
        DataHelper.createDice("123",null, admin, diceRepository, raspberryPiRepository, roomRepository);

        DataHelper.createRaspi("test", admin, null, raspberryPiRepository, roomRepository);

        List<PendingDiceDTO> pendings = new ArrayList<>();
        PendingDiceDTO p = new PendingDiceDTO();
        p.setDiceInternalId("123");
        p.setRaspiInternalId("test");
        pendings.add(p);
        diceRestController.register(pendings);

        Assertions.assertEquals(0, diceService.getPendingDices().size(), "Dice in pending list");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_addToPendingWhichNoRaspi_notInPending(){
        DataHelper.createDice("123",null, admin, diceRepository, raspberryPiRepository, roomRepository);

        List<PendingDiceDTO> pendings = new ArrayList<>();
        PendingDiceDTO p = new PendingDiceDTO();
        p.setDiceInternalId("123");
        p.setRaspiInternalId("test");
        pendings.add(p);
        diceRestController.register(pendings);

        Assertions.assertEquals(0, diceService.getPendingDices().size(), "Dice in pending list");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_addToPendingAndSave_notInPending() throws ProdigaGeneralExpectedException {
        DataHelper.createRaspi("test", admin, null, raspberryPiRepository, roomRepository);

        List<PendingDiceDTO> pendings = new ArrayList<>();
        PendingDiceDTO p = new PendingDiceDTO();
        p.setDiceInternalId("123");
        p.setRaspiInternalId("test");
        pendings.add(p);
        diceRestController.register(pendings);

        Assertions.assertEquals(1, diceService.getPendingDices().size(), "Dice not in pending list");

        Dice d = diceService.getPendingDices().get(0);

        d.setActive(false);

        diceService.save(d);

        Assertions.assertEquals(0, diceService.getPendingDices().size(), "Dice in pending list");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void diceService_deleteDiceWithBookings_diceDeletedNoBookings() throws ProdigaGeneralExpectedException {
        Dice d = DataHelper.createDice("123", null, admin, diceRepository, raspberryPiRepository, roomRepository);

        BookingCategory bc = DataHelper.createBookingCategory("Test", admin, bookingCategoryRepository);
        DataHelper.createBooking(bc, admin, d, bookingRepository);
        DataHelper.createDiceSide(d, bc, 1, admin, diceSideRepository);

        Assertions.assertEquals(1, Lists.newArrayList(bookingRepository.findAll()).size());
        Assertions.assertEquals(1, Lists.newArrayList(diceSideRepository.findAll()).size());

        diceService.deleteDice(d);

        Assertions.assertEquals(0, Lists.newArrayList(bookingRepository.findAll()).size());
        Assertions.assertEquals(0, diceService.getAllDice().size());
        Assertions.assertEquals(0, Lists.newArrayList(diceSideRepository.findAll()).size());
    }
}
