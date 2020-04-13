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
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class BookingCategoryServiceTest {

    @Autowired
    BookingCategoryService bookingCategoryService;

    @Autowired
    BookingCategoryRepository bookingCategoryRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    BookingTypeRepository bookingTypeRepository;

    @Autowired
    DiceRepository diceRepository;

    @Autowired
    RaspberryPiRepository raspberryPiRepository;

    @Autowired
    RoomRepository roomRepository;

    User admin = null;
    User notAdmin = null;

    @BeforeEach
    public void init(@Autowired UserRepository userRepository) {
        admin = DataHelper.createAdminUser("admin", userRepository);
        notAdmin = DataHelper.createUserWithRoles("notAdmin", Sets.newSet(UserRole.EMPLOYEE), userRepository);
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void bookingCategoryService_findAll_returnsCorrectAmount() {
        int amount = 5;
        for(int i = 0; i < amount; i++) {
            DataHelper.createBookingCategory("test" + i, admin, bookingCategoryRepository);
        }

        Assertions.assertEquals(amount, bookingCategoryService.findAllCategories().size(), "Could not find correct amount of booking categories.");
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void bookingCategoryService_findAllByTeam_returnCorrectAmount() {
        int amount = 5;
        Department d = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team t = DataHelper.createRandomTeam(d, admin, teamRepository);
        for(int i = 0; i < amount; i++) {
            BookingCategory cat = DataHelper.createBookingCategory("test" + i, admin, bookingCategoryRepository);

            if(i % 2 == 0) {
                cat.setTeams(Sets.newSet(t));
                bookingCategoryRepository.save(cat);
            }
        }

        Assertions.assertNotNull(bookingCategoryService.findAllCategoriesByTeam(null), "Service returns null when accessing with null");

        Assertions.assertEquals(3, bookingCategoryService.findAllCategoriesByTeam(t).size(), "Could not find correct amount of booking categories.");
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void bookingCategoryService_findAllNotUsedByTeam_returnCorrectAmount() {
        int amount = 5;
        Department d = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team t = DataHelper.createRandomTeam(d, admin, teamRepository);
        for(int i = 0; i < amount; i++) {
            BookingCategory cat = DataHelper.createBookingCategory("test" + i, admin, bookingCategoryRepository);

            if(i % 2 == 0) {
                cat.setTeams(Sets.newSet(t));
                bookingCategoryRepository.save(cat);
            }
        }

        Assertions.assertNotNull(bookingCategoryService.findAllCategoriesNotUsedByTeam(null), "Service returns null when accessing with null");

        Assertions.assertEquals(2, bookingCategoryService.findAllCategoriesNotUsedByTeam(t).size(), "Could not find correct amount of booking categories.");
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void bookingCategoryService_saveWithoutName_throws() {
        BookingCategory cat = new BookingCategory();

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () ->
                bookingCategoryService.save(cat), "Saving without name does not throw");
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void bookingCategoryService_saveValid_entryInDB() throws ProdigaGeneralExpectedException {
        BookingCategory cat = new BookingCategory();
        cat.setName("Test123");

        bookingCategoryService.save(cat);

        BookingCategory dbCat = bookingCategoryService.findAllCategories().stream()
                .findFirst().orElse(null);

        Assertions.assertNotNull(dbCat, "Booking Category was not saved");
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void bookingCategoryService_deleteWithTeam_throws() {
        Department d = DataHelper.createRandomDepartment(admin, departmentRepository);
        Team t = DataHelper.createRandomTeam(d, admin, teamRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test", admin, bookingCategoryRepository);

        cat.setTeams(Sets.newSet(t));

        BookingCategory newCat = bookingCategoryRepository.save(cat);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> bookingCategoryService.delete(newCat));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void bookingCategoryService_deleteWithBooking_throws() {
        Dice d = DataHelper.createDice("123", null, admin, diceRepository, raspberryPiRepository, roomRepository);
        BookingType t = DataHelper.createBookingType(1, true, admin, bookingTypeRepository);
        Booking b = DataHelper.createBooking(t, admin, d, bookingRepository);
        BookingCategory cat = DataHelper.createBookingCategory("test", admin, bookingCategoryRepository);

        b.setBookingCategory(cat);

        bookingRepository.save(b);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> bookingCategoryService.delete(cat));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void bookingCategoryService_findAllUnauthorized_throws() {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> bookingCategoryService.findAllCategories(),
                "EMPLYOEE can access Categories");
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void bookingCategoryService_findByTeamUnauthorized_throws() {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> bookingCategoryService.findAllCategoriesByTeam(null),
                "EMPLYOEE can access Categories");
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void bookingCategoryService_findByNotInTeamUnauthorized_throws() {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> bookingCategoryService.findAllCategoriesNotUsedByTeam(null),
                "EMPLYOEE can access Categories");
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void bookingCategoryService_saveUnauthorized_throws() {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> bookingCategoryService.save(null), "EMPLYOEE can save Categories");
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "notAdmin", authorities = {"EMPLOYEE"})
    public void bookingCategoryService_deleteUnauthorized_throws() {
        Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> bookingCategoryService.delete(null), "EMPLYOEE can delete Categories");
    }
}
