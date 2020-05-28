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
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.repositories.*;
import uibk.ac.at.prodiga.services.BookingCategoryService;
import uibk.ac.at.prodiga.services.DiceService;
import uibk.ac.at.prodiga.services.DiceSideService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.ui.controllers.DiceConfigurationController;
import uibk.ac.at.prodiga.utils.Constants;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class DiceConfigurationControllerTest
{

    @Autowired
    UserRepository userRepository;

    @Autowired
    DiceRepository diceRepository;

    @Autowired
    DiceSideRepository diceSideRepository;

    @Autowired
    RaspberryPiRepository raspberryPiRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    BookingCategoryRepository bookingCategoryRepository;

    @Autowired
    ProdigaUserLoginManager userLoginManager;

    @Autowired
    DiceService diceService;

    @Autowired
    DiceSideService diceSideService;

    @Autowired
    BookingCategoryService bookingCategoryService;

    User admin;
    User employee;
    Dice dice;
    BookingCategory mandt, conf1, conf2, free1, free2;
    DiceConfigurationController controller;

    @BeforeEach
    public void init_each()
    {
        //Create user with dice
        admin = DataHelper.createAdminUser("admin", userRepository);
        employee = DataHelper.createUserWithRoles("dice_testuser_1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        dice = DataHelper.createDice("testdice", null, employee, diceRepository, raspberryPiRepository, roomRepository);

        //preconfigure booking categories
        //create booking categories until mandatory id is reached (yes that is pretty much the only way to do this if the ID is not fixed...)
        for(int i=0;;i++)
        {
            mandt = DataHelper.createBookingCategory("testcat_" + i, admin, bookingCategoryRepository);
            if(mandt.getId().equals(Constants.DO_NOT_BOOK_BOOKING_CATEGORY_ID)) break;
        }

        //automatically create 4 others
        conf1 = DataHelper.createBookingCategory("booking_cat_1", admin, bookingCategoryRepository);
        conf2 = DataHelper.createBookingCategory("booking_cat_2", admin, bookingCategoryRepository);
        free1 = DataHelper.createBookingCategory("booking_cat_3", admin, bookingCategoryRepository);
        free2 = DataHelper.createBookingCategory("booking_cat_4", admin, bookingCategoryRepository);
        //assign mandatory and two configured to dice
        DataHelper.createDiceSide(dice, mandt, 1, admin, diceSideRepository);
        DataHelper.createDiceSide(dice, conf1, 2, admin, diceSideRepository);
        DataHelper.createDiceSide(dice, conf2, 3, admin, diceSideRepository);

        controller = new DiceConfigurationController(userLoginManager, diceService, diceSideService, bookingCategoryService);
    }

    /**
     * tests config procedure
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "dice_testuser_1", authorities = {"EMPLOYEE"})
    public void dice_config_procedure() throws ProdigaGeneralExpectedException
    {
        //status: waiting
        Assertions.assertEquals(1, controller.getStatus());

        //start configure procedure
        controller.startConfigureDice();

        //status: configuring
        Assertions.assertEquals(2, controller.getStatus());

        //check that categories are available
        Assertions.assertTrue(controller.getAvailableCategories().contains(mandt), "Mandatory category was not found in list of available categories.");
        Assertions.assertTrue(controller.getAvailableCategories().contains(conf1), "Category 1 was not found in list of available categories.");
        Assertions.assertTrue(controller.getAvailableCategories().contains(conf2), "Category 2 was not found in list of available categories.");
        Assertions.assertTrue(controller.getAvailableCategories().contains(free1), "Category 3 was not found in list of available categories.");
        Assertions.assertTrue(controller.getAvailableCategories().contains(free2), "Category 4 was not found in list of available categories.");

        //call wrapper and check for response, "dice friendly side" should start at 1
        diceService.onNewDiceSide("testdice", 5);
        Assertions.assertEquals(1, controller.getCurrentSide(), "Side was not translated to friendly side 1.");

        //switch side
        diceService.onNewDiceSide("testdice", 4);
        Assertions.assertEquals(2, controller.getCurrentSide(), "Side was not translated to friendly side 2.");

        //switch back
        diceService.onNewDiceSide("testdice", 5);
        Assertions.assertEquals(1, controller.getCurrentSide(), "Side was not translated to friendly side 1.");

        //set new sides in expected dataformat
        HashMap<Integer, String> newSides = new HashMap<>();
        newSides.put(1, mandt.getId().toString());
        newSides.put(2, free1.getId().toString());
        newSides.put(3, free2.getId().toString());
        for(int i=4;i<13;i++)
        {
            newSides.put(4, null);
        }
        controller.setNewSides(newSides);

        //save
        controller.confirmMapping();

        //check if saved
        Assertions.assertEquals(mandt, diceSideService.findByDiceAndSide(dice, 1).getBookingCategory(), "Mandatory category was not stored to side 1.");
        Assertions.assertEquals(free1, diceSideService.findByDiceAndSide(dice, 2).getBookingCategory(), "Free Category 1 was not stored to side 2.");
        Assertions.assertEquals(free2, diceSideService.findByDiceAndSide(dice, 3).getBookingCategory(), "Free Category 2 was not stored to side 3.");
        for(int i=4;i<13;i++)
        {
            Assertions.assertNull(diceSideService.findByDiceAndSide(dice, i), "Category was saved to side " + i + ", but shouldn't.");
        }
    }

    /**
     * tests config procedure being aborted after setting newSides
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "dice_testuser_1", authorities = {"EMPLOYEE"})
    public void dice_config_procedure_abort() throws ProdigaGeneralExpectedException
    {
        //start configure procedure
        controller.startConfigureDice();

        //set new sides in expected dataformat
        HashMap<Integer, String> newSides = new HashMap<>();
        newSides.put(1, mandt.getId().toString());
        newSides.put(2, free1.getId().toString());
        newSides.put(3, free2.getId().toString());
        for(int i=4;i<13;i++)
        {
            newSides.put(4, null);
        }
        controller.setNewSides(newSides);

        //abort
        controller.abortMapping();

        //check if saved
        Assertions.assertEquals(mandt, diceSideService.findByDiceAndSide(dice, 1).getBookingCategory(), "Mandatory category was not stored to side 1.");
        Assertions.assertEquals(conf1, diceSideService.findByDiceAndSide(dice, 2).getBookingCategory(), "Set Category 1 was removed from side 2.");
        Assertions.assertEquals(conf2, diceSideService.findByDiceAndSide(dice, 3).getBookingCategory(), "Set Category 2 was removed from side 3.");
        for(int i=4;i<13;i++)
        {
            Assertions.assertNull(diceSideService.findByDiceAndSide(dice, i), "Category was saved to side " + i + ", but shouldn't.");
        }
    }

    /**
     * tests edit procedure
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "dice_testuser_1", authorities = {"EMPLOYEE"})
    public void dice_edit_procedure() throws ProdigaGeneralExpectedException
    {
        //status: waiting
        Assertions.assertEquals(1, controller.getStatus());

        //start configure procedure
        controller.startEditDice();

        //status: editing
        Assertions.assertEquals(3, controller.getStatus());

        //check that categories are available
        Assertions.assertTrue(controller.getAvailableCategories().contains(mandt), "Mandatory category was not found in list of available categories.");
        Assertions.assertTrue(controller.getAvailableCategories().contains(conf1), "Category 1 was not found in list of available categories.");
        Assertions.assertTrue(controller.getAvailableCategories().contains(conf2), "Category 2 was not found in list of available categories.");
        Assertions.assertTrue(controller.getAvailableCategories().contains(free1), "Category 3 was not found in list of available categories.");
        Assertions.assertTrue(controller.getAvailableCategories().contains(free2), "Category 4 was not found in list of available categories.");

        //check current sides are still present
        Assertions.assertEquals(mandt.getId().toString(), controller.getNewSides().get(1), "Mandatory category is not in side 1.");
        Assertions.assertEquals(conf1.getId().toString(), controller.getNewSides().get(2), "Set Category 1 is not in side 2.");
        Assertions.assertEquals(conf2.getId().toString(), controller.getNewSides().get(3), "Set Category 2 is not in side 3.");
        for(int i=4;i<13;i++)
        {
            Assertions.assertNull(controller.getNewSides().get(i), "Category was saved to side " + i + ", but shouldn't.");
        }

        //set new sides in expected dataformat
        Map<Integer, String> newSides = controller.getNewSides();
        newSides.put(4, free1.getId().toString());
        newSides.put(5, free2.getId().toString());
        controller.setNewSides(newSides);

        //save
        controller.confirmEdit();

        //check if saved
        Assertions.assertEquals(mandt, diceSideService.findByDiceAndSide(dice, 1).getBookingCategory(), "Mandatory category was not stored to side 1.");
        Assertions.assertEquals(conf1, diceSideService.findByDiceAndSide(dice, 2).getBookingCategory(), "Set Category 1 was removed from side 2.");
        Assertions.assertEquals(conf2, diceSideService.findByDiceAndSide(dice, 3).getBookingCategory(), "Set Category 2 was removed from side 3.");
        Assertions.assertEquals(conf2, diceSideService.findByDiceAndSide(dice, 3).getBookingCategory(), "Free Category 1 was not stored to side 4.");
        Assertions.assertEquals(conf2, diceSideService.findByDiceAndSide(dice, 3).getBookingCategory(), "Free Category 2 was not stored to side 5.");
        for(int i=6;i<13;i++)
        {
            Assertions.assertNull(diceSideService.findByDiceAndSide(dice, i), "Category was saved to side " + i + ", but shouldn't.");
        }
    }

    /**
     * tests edit procedure with aborting
     */
    @DirtiesContext
    @Test
    @WithMockUser(username = "dice_testuser_1", authorities = {"EMPLOYEE"})
    public void dice_edit_procedure_abort() throws ProdigaGeneralExpectedException
    {
        //start configure procedure
        controller.startEditDice();

        //set new sides in expected dataformat
        Map<Integer, String> newSides = controller.getNewSides();
        newSides.put(4, free1.getId().toString());
        newSides.put(5, free2.getId().toString());
        controller.setNewSides(newSides);

        //abort
        controller.abortEdit();

        //check that old sides are still there, but new ones are not set
        Assertions.assertEquals(mandt, diceSideService.findByDiceAndSide(dice, 1).getBookingCategory(), "Mandatory category was not stored to side 1.");
        Assertions.assertEquals(conf1, diceSideService.findByDiceAndSide(dice, 2).getBookingCategory(), "Set Category 1 was removed from side 2.");
        Assertions.assertEquals(conf2, diceSideService.findByDiceAndSide(dice, 3).getBookingCategory(), "Set Category 2 was removed from side 3.");
        for(int i=6;i<13;i++)
        {
            Assertions.assertNull(diceSideService.findByDiceAndSide(dice, i), "Category was saved to side " + i + ", but shouldn't.");
        }
    }
}
