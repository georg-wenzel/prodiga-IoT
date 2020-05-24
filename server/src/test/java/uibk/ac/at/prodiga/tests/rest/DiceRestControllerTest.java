package uibk.ac.at.prodiga.tests.rest;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.repositories.*;
import uibk.ac.at.prodiga.rest.controller.DiceRestController;
import uibk.ac.at.prodiga.rest.dtos.HistoryEntryDTO;
import uibk.ac.at.prodiga.tests.helper.DataHelper;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class DiceRestControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private DiceRestController diceRestController;

    @Autowired
    private DiceRepository diceRepository;

    @Autowired
    private RaspberryPiRepository raspberryPiRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingCategoryRepository bookingCategoryRepository;

    @Autowired
    private DiceSideRepository diceSideRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeamRepository teamRepository;

    User notAdmin = null;

    @BeforeEach
    public void init() {
        User admin = DataHelper.createAdminUser("admin", userRepository);

        Department d = DataHelper.createRandomDepartment(admin, departmentRepository);

        Team t = DataHelper.createRandomTeam(d, admin, teamRepository);

        notAdmin = DataHelper.createUserWithRoles("notAdmin", Sets.newSet(UserRole.EMPLOYEE), admin, d, t, userRepository);
    }

    @Test
    @DirtiesContext
    public void diceController_createBookingWithInvalidDice_noActionsOccur() {
        int bookingNumberBefore = Lists.newArrayList(bookingRepository.findAll()).size();

        HistoryEntryDTO entry = new HistoryEntryDTO();
        entry.setCubeInternalId("test");
        entry.setSeconds(10);
        entry.setSide(1);

        diceRestController.addBooking(Lists.newArrayList(entry));

        Assertions.assertEquals(bookingNumberBefore, Lists.newArrayList(bookingRepository.findAll()).size(),
                "Booking created after invalid request");
    }

    @Test
    @DirtiesContext
    public void diceController_createBookingForDiceWithoutUser_noActionsOccur() {
        int bookingNumberBefore = Lists.newArrayList(bookingRepository.findAll()).size();

        Dice d = DataHelper.createDice("test", null, notAdmin, diceRepository, raspberryPiRepository, roomRepository);

        d.setUser(null);

        diceRepository.save(d);

        HistoryEntryDTO entry = new HistoryEntryDTO();
        entry.setCubeInternalId("test");
        entry.setSeconds(10);
        entry.setSide(1);

        diceRestController.addBooking(Lists.newArrayList(entry));

        Assertions.assertEquals(bookingNumberBefore, Lists.newArrayList(bookingRepository.findAll()).size(),
                "Booking created after invalid request");
    }

    @Test
    @DirtiesContext
    public void diceController_createBookingWithoutSide_noActionsOccur() {
        int bookingNumberBefore = Lists.newArrayList(bookingRepository.findAll()).size();

        DataHelper.createDice("test", null, notAdmin, diceRepository, raspberryPiRepository, roomRepository);

        HistoryEntryDTO entry = new HistoryEntryDTO();
        entry.setCubeInternalId("test");
        entry.setSeconds(10);
        entry.setSide(1);

        diceRestController.addBooking(Lists.newArrayList(entry));

        Assertions.assertEquals(bookingNumberBefore, Lists.newArrayList(bookingRepository.findAll()).size(),
                "Booking created after invalid request");
    }

    @Test
    @DirtiesContext
    public void diceController_createBookingWithDoNotBookSide_noActionsOccur() {
        int bookingNumberBefore = Lists.newArrayList(bookingRepository.findAll()).size();

        Dice d = DataHelper.createDice("test", null, notAdmin, diceRepository, raspberryPiRepository, roomRepository);

        BookingCategory bc = DataHelper.createBookingCategory("test", notAdmin, bookingCategoryRepository);

        DataHelper.createDiceSide(d, bc, 1, notAdmin, diceSideRepository);

        HistoryEntryDTO entry = new HistoryEntryDTO();
        entry.setCubeInternalId("test");
        entry.setSeconds(10);
        entry.setSide(1);

        diceRestController.addBooking(Lists.newArrayList(entry));

        Assertions.assertEquals(bookingNumberBefore, Lists.newArrayList(bookingRepository.findAll()).size(),
                "Booking created after invalid request");
    }

    @Test
    @DirtiesContext
    public void diceController_createBookingValidRequestNoExistingBooking_BookingCreated() throws Exception {
        Dice d = DataHelper.createDice("test", null, notAdmin, diceRepository, raspberryPiRepository, roomRepository);

        BookingCategory bc = DataHelper.createBookingCategory("test", notAdmin, bookingCategoryRepository);

        DataHelper.createDiceSide(d, bc, 1, notAdmin, diceSideRepository);

        BookingCategory bc2 = DataHelper.createBookingCategory("test1", notAdmin, bookingCategoryRepository);

        DataHelper.createDiceSide(d, bc2, 2, notAdmin, diceSideRepository);

        BookingCategory bc3 = DataHelper.createBookingCategory("test2", notAdmin, bookingCategoryRepository);

        DataHelper.createDiceSide(d, bc3, 3, notAdmin, diceSideRepository);

        HistoryEntryDTO entry1 = new HistoryEntryDTO();
        entry1.setCubeInternalId("test");
        entry1.setSeconds(60);
        entry1.setSide(2);

        HistoryEntryDTO entry2 = new HistoryEntryDTO();
        entry2.setCubeInternalId("test");
        entry2.setSeconds(100);
        entry2.setSide(3);

        diceRestController.addBooking(Lists.newArrayList(entry1, entry2));

        List<Booking> allBookings = Lists.newArrayList(bookingRepository.findAll());

        Assertions.assertEquals(2, allBookings.size(), "Not exactly 1 booking created");

        Assertions.assertEquals(1L, allBookings.stream()
            .filter(x -> x.getBookingCategory().getName().equals("test1")).count(),
                "no booking for first category");

        Booking b1 = allBookings.stream()
                .filter(x -> x.getBookingCategory().getName().equals("test1"))
                .findFirst().orElseThrow(() -> new Exception("Should never happen"));

        Assertions.assertEquals(60,
                (b1.getActivityEndDate().getTime() - b1.getActivityStartDate().getTime()) / 1000);

        Assertions.assertEquals(2, allBookings.size(), "Not exactly 1 booking created");

        Assertions.assertEquals(1L, allBookings.stream()
                .filter(x -> x.getBookingCategory().getName().equals("test1")).count(),
                "no booking for second category");

        Booking b2 = allBookings.stream()
                .filter(x -> x.getBookingCategory().getName().equals("test2"))
                .findFirst().orElseThrow(() -> new Exception("Should never happen"));

        Assertions.assertEquals(100,
                (b2.getActivityEndDate().getTime() - b2.getActivityStartDate().getTime()) / 1000);
    }

    @Test
    @DirtiesContext
    public void diceController_createBookingValidRequestExistingBooking_BookingCreated() throws Exception {
        Dice d = DataHelper.createDice("test", null, notAdmin, diceRepository, raspberryPiRepository, roomRepository);

        BookingCategory bc = DataHelper.createBookingCategory("test", notAdmin, bookingCategoryRepository);

        DataHelper.createDiceSide(d, bc, 1, notAdmin, diceSideRepository);

        BookingCategory bc2 = DataHelper.createBookingCategory("test1", notAdmin, bookingCategoryRepository);

        DataHelper.createDiceSide(d, bc2, 2, notAdmin, diceSideRepository);

        Booking existing = DataHelper.createBooking(bc, notAdmin, bookingRepository);

        BookingCategory bc3 = DataHelper.createBookingCategory("test2", notAdmin, bookingCategoryRepository);

        DataHelper.createDiceSide(d, bc3, 3, notAdmin, diceSideRepository);

        HistoryEntryDTO entry1 = new HistoryEntryDTO();
        entry1.setCubeInternalId("test");
        entry1.setSeconds(60);
        entry1.setSide(2);

        HistoryEntryDTO entry2 = new HistoryEntryDTO();
        entry2.setCubeInternalId("test");
        entry2.setSeconds(100);
        entry2.setSide(3);

        diceRestController.addBooking(Lists.newArrayList(entry1, entry2));

        List<Booking> allBookings = Lists.newArrayList(bookingRepository.findAll());

        Assertions.assertEquals(3, allBookings.size(), "Not exactly 3 booking created");

        Assertions.assertEquals(1L, allBookings.stream()
                        .filter(x -> x.getBookingCategory().getName().equals("test1")).count(),
                "no booking for first category");

        Booking b1 = allBookings.stream()
                .filter(x -> x.getBookingCategory().getName().equals("test1"))
                .findFirst().orElseThrow(() -> new Exception("Should never happen"));

        Assertions.assertEquals(60,
                (b1.getActivityEndDate().getTime() - b1.getActivityStartDate().getTime()) / 1000);

        Assertions.assertEquals(120,
                (b1.getActivityEndDate().getTime() - existing.getActivityEndDate().getTime()) / 1000);

        Assertions.assertEquals(1L, allBookings.stream()
                        .filter(x -> x.getBookingCategory().getName().equals("test1")).count(),
                "no booking for second category");

        Booking b2 = allBookings.stream()
                .filter(x -> x.getBookingCategory().getName().equals("test2"))
                .findFirst().orElseThrow(() -> new Exception("Should never happen"));

        Assertions.assertEquals(100,
                (b2.getActivityEndDate().getTime() - b2.getActivityStartDate().getTime()) / 1000);
    }

}
