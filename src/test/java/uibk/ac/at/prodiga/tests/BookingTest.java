package uibk.ac.at.prodiga.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.repositories.*;
import uibk.ac.at.prodiga.services.BookingService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class BookingTest
{
    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    DiceRepository diceRepository;

    @Autowired
    BookingTypeRepository bookingTypeRepository;

    @Autowired
    RaspberryPiRepository raspberryPiRepository;

    @Autowired
    BookingService bookingService;

    /**
     * Tests loading of booking by id.
     */
    @Test
    @WithMockUser(username = "user1", authorities = {"EMPLOYEE"})
    public void load_booking_by_id()
    {
        //Data setup
        User admin = DataHelper.createAdminUser("admin", userRepository);
        User u1 = DataHelper.createUserWithRoles("user1", Sets.newSet(UserRole.EMPLOYEE), userRepository);
        Room room = DataHelper.createRoom("testroom", admin, roomRepository);
        RaspberryPi pi = DataHelper.createRaspi("testpi", admin, room, raspberryPiRepository, roomRepository);
        Dice d = DataHelper.createDice("testdice", pi, admin, diceRepository, raspberryPiRepository, roomRepository);
        u1.setDice(d);
        u1 = userRepository.save(u1);
        d.setUser(u1);
        d = diceRepository.save(d);
        BookingType bt = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);
        Booking b = DataHelper.createBooking(bt, u1, bookingRepository);

        Booking booking_service = bookingService.loadBooking(b.getId());

        Assertions.assertEquals(b.getActivityStartDate(), booking_service.getActivityStartDate(), "Activity start date was not properly stored in DB.");
        Assertions.assertEquals(b.getActivityEndDate(), booking_service.getActivityEndDate(), "Activity end date was not stored properly in DB.");
        Assertions.assertEquals(d, booking_service.getDice(), "Dice was not properly stored in DB");
        Assertions.assertNull(booking_service.getObjectChangedDateTime(), "Booking changed date time should be null, but is not");
        Assertions.assertNull(booking_service.getObjectChangedUser(), "Booking changed user should be null, but is not");
        Assertions.assertEquals(u1, booking_service.getObjectCreatedUser(), "Creation user of booking type does not match user1.");
        Assertions.assertEquals(b.getObjectCreatedDateTime(), booking_service.getObjectCreatedDateTime(), "Creation date not loaded properly for booking.");
    }
}
