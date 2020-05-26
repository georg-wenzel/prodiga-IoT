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
import uibk.ac.at.prodiga.model.Room;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.RaspberryPiRepository;
import uibk.ac.at.prodiga.repositories.RoomRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.ui.controllers.RoomDetailController;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class RoomDetailControllerTest {

    @Autowired
    RoomDetailController roomDetailController;

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
    public void roomDetailController_saveRoom_roomSaved() throws Exception {
        Room r = DataHelper.createRoom("test", admin, roomRepository);
        roomDetailController.setRoom(r);
        roomDetailController.getRoom().setName("test1");
        roomDetailController.doSaveRoom();
        Assertions.assertNotNull(roomRepository.findFirstByName("test1"));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void roomDetailController_deleteRoom_roomDeleted() throws Exception {
        Room r = DataHelper.createRoom("test", admin, roomRepository);
        roomDetailController.setRoom(r);
        roomDetailController.doDeleteRoom();
        Assertions.assertNull(roomRepository.findFirstByName("test1"));
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void roomDetailController_doReloadRoom_correctRoomReturned() throws Exception {
        DataHelper.createRoom("test", admin, roomRepository);
        roomDetailController.doReloadRoom(null);
        Assertions.assertTrue(roomDetailController.getRoom().isNew());
        roomDetailController.doReloadRoom("");
        Assertions.assertTrue(roomDetailController.getRoom().isNew());
        roomDetailController.doReloadRoom("test");
        Assertions.assertEquals("test", roomDetailController.getRoom().getName());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void roomDetailController_getSetRoomByName_correctRoomReturned()  {
        DataHelper.createRoom("test", admin, roomRepository);

        roomDetailController.setRoomByName("test");
        Assertions.assertEquals("test", roomDetailController.getRoomByName());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void roomDetailController_getSetRoomById_correctRoomReturned()  {
        Room r = DataHelper.createRoom("test", admin, roomRepository);

        roomDetailController.setRoomById(null);
        Assertions.assertTrue(roomDetailController.getRoom().isNew());

        roomDetailController.setRoomById(r.getId());
        Assertions.assertEquals(r.getId(), roomDetailController.getRoomById());
    }
}
