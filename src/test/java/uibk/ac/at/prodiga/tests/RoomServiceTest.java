package uibk.ac.at.prodiga.tests;
/*
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.Room;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.RoomRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.services.RoomService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@WebAppConfiguration
public class RoomServiceTest implements InitializingBean
{

    @Autowired
    RoomService roomService;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    UserRepository userRepository;


    @Override
    public void afterPropertiesSet() throws Exception {
        //Grab admin user to set as creation user for test room
        User admin = userRepository.findFirstByUsername("admin");

        //Before tests, initialize test room
        Room test_room1 = new Room();
        test_room1.setObjectCreatedUser(admin);
        test_room1.setObjectCreatedDateTime(new Date());
        test_room1.setName("test_room_1");
        roomRepository.save(test_room1);

        Room test_room2 = new Room();
        test_room2.setObjectCreatedUser(admin);
        test_room2.setObjectCreatedDateTime(new Date());
        test_room2.setName("test_room_2");
        roomRepository.save(test_room2);

    }


    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void loadRoomData() {
        Assert.assertTrue(true);
        Room test_room1 = roomService.getFirstByName("test_room_1");
        Assert.assertNotNull("Could not load test_room_1.",test_room1);
    }


}
*/