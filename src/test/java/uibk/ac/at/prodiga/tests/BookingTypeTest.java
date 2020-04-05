package uibk.ac.at.prodiga.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.BookingType;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.BookingTypeRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.services.BookingTypeService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

import java.util.Collection;
import java.util.Date;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class BookingTypeTest {
    @Autowired
    BookingTypeRepository bookingTypeRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookingTypeService bookingTypeService;

    /**
     * Tests loading of booking type by id.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void load_booking_type_by_id() {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        BookingType bt = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);

        BookingType bt_service = bookingTypeService.loadBookingType(bt.getId());

        Assertions.assertEquals(bt.getActivityName(), bt_service.getActivityName(), "Name was not stored properly in DB.");
        Assertions.assertEquals(4, bt_service.getSide(), "Returned name did not match name in DB.");
        Assertions.assertTrue(bt_service.isActive(), "isActive flag not properly returned from DB.");
        Assertions.assertNull(bt_service.getObjectChangedDateTime(), "Booking type changed date time should be null, but is not");
        Assertions.assertNull(bt_service.getObjectChangedUser(), "Booking type changed user should be null, but is not");
        Assertions.assertEquals(admin, bt_service.getObjectCreatedUser(), "Creation user of booking type does not match admin.");
        Assertions.assertEquals(bt.getObjectCreatedDateTime(), bt_service.getObjectCreatedDateTime(), "Creation date not loaded properly for booking type.");
    }

    /**
     * Tests unauthorized loading of booking type by id.
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "EMPLOYEE"})
    public void load_booking_type_by_id_unauthorized() {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        BookingType bt = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingTypeService.loadBookingType(bt.getId());
        }, "Booking type loaded despite lacking authorization of ADMIN");
    }

    /**
     * Tests loading of booking type by active side
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void load_booking_type_by_active_side() {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        BookingType bt = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);

        BookingType bt_service = bookingTypeService.getActiveBookingForSide(4);

        Assertions.assertEquals(bt, bt_service, "Service did not return the correct booking type");
        Assertions.assertEquals(bt.getActivityName(), bt_service.getActivityName(), "Name was not stored properly in DB.");
        Assertions.assertEquals(4, bt_service.getSide(), "Returned name did not match name in DB.");
        Assertions.assertTrue(bt_service.isActive(), "isActive flag not properly returned from DB.");
        Assertions.assertNull(bt_service.getObjectChangedDateTime(), "Booking type changed date time should be null, but is not");
        Assertions.assertNull(bt_service.getObjectChangedUser(), "Booking type changed user should be null, but is not");
        Assertions.assertEquals(admin, bt_service.getObjectCreatedUser(), "Creation user of booking type does not match admin.");
        Assertions.assertEquals(bt.getObjectCreatedDateTime(), bt_service.getObjectCreatedDateTime(), "Creation date not loaded properly for booking type.");
    }

    /**
     * Tests unauthorized loading of booking type by active side.
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "EMPLOYEE"})
    public void load_booking_type_by_active_side_unauthorized() {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        BookingType bt = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingTypeService.getActiveBookingForSide(4);
        }, "Booking type loaded despite lacking authorization of ADMIN");
    }

    /**
     * Tests loading of active booking types
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void load_active_types() {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        BookingType bt = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);
        BookingType bt2 = DataHelper.createBookingType(5, true, admin, bookingTypeRepository);
        BookingType bt3 = DataHelper.createBookingType(6, false, admin, bookingTypeRepository);

        Collection<BookingType> btypes = bookingTypeService.getAllActiveBookingTypes();

        Assertions.assertTrue(btypes.contains(bt), "Could not find created booking type for side 4 in collection.");
        Assertions.assertTrue(btypes.contains(bt2), "Could not find created booking type for side 5 in collection.");
        Assertions.assertFalse(btypes.contains(bt3), "Found created booking type for side 6 in collection, even though it is not an active booking type.");
    }

    /**
     * Tests loading of all booking types
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void load_all_types() {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        BookingType bt = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);
        BookingType bt2 = DataHelper.createBookingType(5, true, admin, bookingTypeRepository);
        BookingType bt3 = DataHelper.createBookingType(6, false, admin, bookingTypeRepository);

        Collection<BookingType> btypes = bookingTypeService.getAllBookingTypes();

        Assertions.assertTrue(btypes.contains(bt), "Could not find created booking type for side 4 in collection.");
        Assertions.assertTrue(btypes.contains(bt2), "Could not find created booking type for side 5 in collection.");
        Assertions.assertTrue(btypes.contains(bt2), "Could not find created booking type for side 6 in collection.");
    }

    /**
     * Tests unauthorized loading of active booking types
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "EMPLOYEE"})
    public void load_active_types_unauthorized() {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        BookingType bt = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);
        BookingType bt2 = DataHelper.createBookingType(5, true, admin, bookingTypeRepository);
        BookingType bt3 = DataHelper.createBookingType(6, false, admin, bookingTypeRepository);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingTypeService.getAllActiveBookingTypes();
        }, "Booking types loaded despite lacking authorization of ADMIN");
    }

    /**
     * Tests unauthorized loading of all booking types
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "EMPLOYEE"})
    public void load_all_types_unauthorized() {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        BookingType bt = DataHelper.createBookingType(4, true, admin, bookingTypeRepository);
        BookingType bt2 = DataHelper.createBookingType(5, true, admin, bookingTypeRepository);
        BookingType bt3 = DataHelper.createBookingType(6, false, admin, bookingTypeRepository);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingTypeService.getAllBookingTypes();
        }, "Booking types loaded despite lacking authorization of ADMIN");
    }

    /**
     * Tests saving of booking type
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void save_type() throws ProdigaGeneralExpectedException {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        BookingType bt = new BookingType();
        bt.setActivityName("test_activity");
        bt.setSide(5);
        bt.setActive(true);
        bt = bookingTypeService.saveBookingType(bt);

        Assertions.assertEquals("test_activity", bt.getActivityName(), "Returned activity name does not match.");
        Assertions.assertEquals(5, bt.getSide(), "Returned side does not match.");
        Assertions.assertTrue(bt.isActive(), "Returned active value does not match.");
        Assertions.assertEquals(admin, bt.getObjectCreatedUser(), "admin did not become creator of the object.");
        Assertions.assertTrue((new Date()).getTime() - bt.getObjectCreatedDateTime().getTime() < 1000 * 60, "Creation date has not been properly set.");
        Assertions.assertNull(bt.getObjectChangedDateTime(), "Changed date should be null, but is not.");
        Assertions.assertNull(bt.getObjectChangedUser(), "Changed user should be null, but is not.");
    }

    /**
     * Tests saving of booking type when an active type is already enabled.
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void overwrite_active_type() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        BookingType old_bt = DataHelper.createBookingType(5, true, admin, bookingTypeRepository);

        BookingType bt = new BookingType();
        bt.setActivityName("test_activity");
        bt.setSide(5);
        bt.setActive(true);
        bt = bookingTypeService.saveBookingType(bt);

        Assertions.assertTrue(bt.isActive(), "Returned active value does not match.");
        old_bt = bookingTypeRepository.findFirstById(old_bt.getId());
        Assertions.assertFalse(old_bt.isActive(), "Old Booking Type active flag was not changed to false.");
    }

    /**
     * Tests unauthorized saving of booking type.
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"DEPARTMENTLEADER", "TEAMLEADER", "EMPLOYEE"})
    public void save_type_unauthorized()
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        BookingType old_bt = DataHelper.createBookingType(5, true, admin, bookingTypeRepository);

        BookingType bt = new BookingType();
        bt.setActivityName("test_activity");
        bt.setSide(5);
        bt.setActive(true);

        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookingTypeService.saveBookingType(bt);
        }, "Booking type saved despite lacking authorization of ADMIN");
    }

    /**
     * Tests changing an existing booking type name
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void change_type_name() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        BookingType bt = DataHelper.createBookingType(5, true, admin, bookingTypeRepository);

        bt.setActivityName("test_activity");
        bt = bookingTypeService.saveBookingType(bt);
        Assertions.assertEquals("test_activity", bt.getActivityName(), "Name was not updated properly.");
    }

    /**
     * Tests changing an existing booking type side
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void change_type_side() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        BookingType bt = DataHelper.createBookingType(5, true, admin, bookingTypeRepository);

        bt.setSide(3);
        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            bookingTypeService.saveBookingType(bt);
        }, "Updated booking type side, which should not be possible.");
    }

    /**
     * Tests changing an existing booking type active flag from true to false
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void change_type_to_inactive() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        BookingType bt = DataHelper.createBookingType(5, true, admin, bookingTypeRepository);

        bt.setActive(false);
        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> {
            bookingTypeService.saveBookingType(bt);
        }, "Updated booking type active flag to false, which should not be possible.");
    }

    /**
     * Tests changing an existing booking type active flag from true to false
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void change_type_to_active() throws ProdigaGeneralExpectedException
    {
        User admin = DataHelper.createAdminUser("admin", userRepository);
        BookingType bt_old = DataHelper.createBookingType(5, true, admin, bookingTypeRepository);
        BookingType bt = DataHelper.createBookingType(5, false, admin, bookingTypeRepository);

        bt.setActive(true);
        bt = bookingTypeService.saveBookingType(bt);
        bt_old = bookingTypeRepository.findFirstById(bt_old.getId());

        Assertions.assertTrue(bt.isActive(), "Active flag not properly set for booking type.");
        Assertions.assertFalse(bt_old.isActive(), "Active flag not properly disabled for old booking type.");
    }
}
