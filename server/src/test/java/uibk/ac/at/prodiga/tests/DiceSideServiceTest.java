package uibk.ac.at.prodiga.tests;

import org.h2.store.DataHandler;
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
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.DiceSide;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.*;
import uibk.ac.at.prodiga.services.BookingCategoryService;
import uibk.ac.at.prodiga.services.DiceSideService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class DiceSideServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DiceSideRepository diceSideRepository;

    @Autowired
    private DiceSideService diceSideService;

    @Autowired
    private RaspberryPiRepository raspberryPiRepository;

    @Autowired
    private DiceRepository diceRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingCategoryRepository bookingCategoryRepository;

    @Autowired
    private BookingCategoryService bookingCategoryService;

    User admin = null;

    @BeforeEach
    public void initEach() {
        admin = DataHelper.createAdminUser("admin", userRepository);
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN", "EMPLOYEE"})
    public void diceSideService_existingDiceSideNewWithNull_ExistingGetsDeleted(){
        Dice d = DataHelper.createDice("123", null, admin, diceRepository, raspberryPiRepository, roomRepository);

        for(int i = 0; i < 12; i++) {
            BookingCategory bc = DataHelper.createBookingCategory("test" + i, admin, bookingCategoryRepository);

            DataHelper.createDiceSide(d, bc, i, admin, diceSideRepository);
        }

        Assertions.assertNotNull(diceSideService.findByDiceAndSide(d, 0), "Created Dice Side not found");


        diceSideService.onNewConfiguredDiceSide(0, null, d);

        Assertions.assertNull(diceSideService.findByDiceAndSide(d, 0), "Dice Side not deleted");
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN", "EMPLOYEE"})
    public void diceSideService_existingDiceSideNewWithCategory_newCategorySet(){
        Dice d = DataHelper.createDice("123", null, admin, diceRepository, raspberryPiRepository, roomRepository);

        for(int i = 0; i < 12; i++) {
            BookingCategory bc = DataHelper.createBookingCategory("test" + i, admin, bookingCategoryRepository);

            DataHelper.createDiceSide(d, bc, i, admin, diceSideRepository);
        }

        BookingCategory newBc = bookingCategoryService.findById(1);

        Assertions.assertNotNull(newBc, "Mandatory Booking Category does not exist");

        Assertions.assertNotNull(diceSideService.findByDiceAndSide(d, 0), "Created Dice Side not found");

        diceSideService.onNewConfiguredDiceSide(0, newBc, d);

        DiceSide modifiedDs = diceSideService.findByDiceAndSide(d, 0);

        Assertions.assertNotNull(modifiedDs, "Dice Side deleted");

        Assertions.assertEquals(newBc.getId(), modifiedDs.getBookingCategory().getId());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN", "EMPLOYEE"})
    public void diceSideService_newDiceSide_newSideCreated(){
        Dice d = DataHelper.createDice("123", null, admin, diceRepository, raspberryPiRepository, roomRepository);

        DataHelper.createBookingCategory("test", admin, bookingCategoryRepository);

        DiceSide existingDs = diceSideService.findByDiceAndSide(d, 0);

        Assertions.assertNull(existingDs, "Dice Side already exists");

        BookingCategory newBc = bookingCategoryService.findById(1);

        Assertions.assertNotNull(newBc, "Mandatory Booking Category does not exist");

        diceSideService.onNewConfiguredDiceSide(0, newBc, d);

        DiceSide modifiedDs = diceSideService.findByDiceAndSide(d, 0);

        Assertions.assertNotNull(modifiedDs, "Dice Side deleted");

        Assertions.assertEquals(newBc.getId(), modifiedDs.getBookingCategory().getId());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN", "EMPLOYEE"})
    public void diceSideService_newDiceSideWithoutCategory_noChanges(){
        Dice d = DataHelper.createDice("123", null, admin, diceRepository, raspberryPiRepository, roomRepository);

        int currentAmount = diceSideRepository.findAllByDice(d).size();

        DataHelper.createBookingCategory("test", admin, bookingCategoryRepository);

        BookingCategory newBc = bookingCategoryService.findById(1);

        Assertions.assertNotNull(newBc, "Mandatory Booking Category does not exist");

        diceSideService.onNewConfiguredDiceSide(0, null, d);

        Assertions.assertEquals(currentAmount, diceSideRepository.findAllByDice(d).size());
    }
}
