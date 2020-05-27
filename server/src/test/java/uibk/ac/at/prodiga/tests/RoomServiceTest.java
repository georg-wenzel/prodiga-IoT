package uibk.ac.at.prodiga.tests;

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
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.Room;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.RaspberryPiRepository;
import uibk.ac.at.prodiga.repositories.RoomRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.services.RoomService;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class RoomServiceTest
{

    @Autowired
    RoomService roomService;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RaspberryPiRepository raspberryPiRepository;

    User admin = null;

    @BeforeEach
    public void initEach(@Autowired UserRepository userRepository) {
        admin = DataHelper.createAdminUser("admin", userRepository);
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void roomService_getAllRooms_correctAmountReturned() {
        DataHelper.createRoom("max", admin, roomRepository);

        Assertions.assertEquals(1, roomService.getAllRooms().size());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void roomService_getFirstById_correctRoomReturned() {
        Room r = DataHelper.createRoom("max", admin, roomRepository);
        Assertions.assertNotNull(roomService.getFirstById(r.getId()));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void roomService_saveRoom_roomSaved() throws ProdigaGeneralExpectedException {
        DataHelper.createRoom("test", admin, roomRepository);
        Room r = new Room();
        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> roomService.saveRoom(r));
        r.setName("1");
        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> roomService.saveRoom(r));
        r.setName("test");
        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> roomService.saveRoom(r));
        r.setName("test2");
        roomService.saveRoom(r);

        Assertions.assertNotNull(roomRepository.findFirstByName("test2"));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void roomService_deleteRoom_roomDeleted() throws ProdigaGeneralExpectedException {
        Room r = DataHelper.createRoom("test", admin, roomRepository);
        RaspberryPi raspi = DataHelper.createRaspi("123", admin, r, raspberryPiRepository, null);

        Assertions.assertThrows(ProdigaGeneralExpectedException.class, () -> roomService.deleteRoom(r));
        raspberryPiRepository.delete(raspi);
        roomService.deleteRoom(r);
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void roomService_loadRoom_correctRoomReturned() {
        DataHelper.createRoom("test", admin, roomRepository);
        Assertions.assertEquals("test", roomService.loadRoom("test").getName());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void roomService_createRoom_newRoomReturned() {
        Assertions.assertTrue(roomService.createNewRoom().isNew());
    }
}