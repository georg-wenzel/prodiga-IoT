package uibk.ac.at.prodiga.tests.rest;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.DiceRepository;
import uibk.ac.at.prodiga.repositories.RaspberryPiRepository;
import uibk.ac.at.prodiga.repositories.RoomRepository;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.rest.controller.IntrinsicsController;
import uibk.ac.at.prodiga.rest.dtos.*;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.utils.FeedManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class IntrinsicsControllerTest {

    @Autowired
    IntrinsicsController intrinsicsController;

    @Autowired
    DiceRepository diceRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RaspberryPiRepository raspberryPiRepository;

    @Autowired
    RoomRepository roomRepository;

    User admin;

    @BeforeEach
    public void init() {
        admin = DataHelper.createAdminUser("admin", userRepository);
    }

    @Test
    @DirtiesContext
    public void intrinsicsController_ping_pong() {
        GenericStringDTO response = intrinsicsController.ping();

        Assertions.assertNotNull(response);
        Assertions.assertEquals("Pong \uD83D\uDC1D", response.getResponse());
    }

    @Test
    @DirtiesContext
    public void intrinsicsController_updateBattery_batterySaved() {
        RaspberryPi raspi = DataHelper.createRaspi("test", admin, null, raspberryPiRepository, roomRepository);

        DataHelper.createDice("123", raspi, admin, diceRepository, null, null);
        DataHelper.createDice("1234", raspi, admin, diceRepository, null, null);

        IntrinsicsDTO request = new IntrinsicsDTO();
        request.setInternalId("test");
        CubeIntrinsicsDTO cube1 = new CubeIntrinsicsDTO();
        cube1.setInternalId("123");
        cube1.setBatteryStatus(10);
        CubeIntrinsicsDTO cube2 = new CubeIntrinsicsDTO();
        cube2.setInternalId("1234");
        cube2.setBatteryStatus(20);
        List<CubeIntrinsicsDTO> cubes = new ArrayList<>();
        cubes.add(cube1);
        cubes.add(cube2);
        request.setCubeIntrinsics(cubes);

        intrinsicsController.push(request);

        Assertions.assertEquals(10, diceRepository.findFirstByInternalId("123").getLastBatteryStatus());
        Assertions.assertEquals(20, diceRepository.findFirstByInternalId("1234").getLastBatteryStatus());
    }

    @Test
    @DirtiesContext
    public void intrinsicsController_pushEmptyRequest_noChange() {
        RaspberryPi raspi = DataHelper.createRaspi("test", admin, null, raspberryPiRepository, roomRepository);

        Dice d1 = DataHelper.createDice("123", raspi, admin, diceRepository, null, null);
        Dice d2 = DataHelper.createDice("1234", raspi, admin, diceRepository, null, null);

        d1.setLastBatteryStatus(3);
        d2.setLastBatteryStatus(4);

        diceRepository.save(d1);
        diceRepository.save(d2);

        intrinsicsController.push(null);

        Assertions.assertEquals(3, diceRepository.findFirstByInternalId("123").getLastBatteryStatus());
        Assertions.assertEquals(4, diceRepository.findFirstByInternalId("1234").getLastBatteryStatus());
    }

    @Test
    @DirtiesContext
    public void intrinsicsController_getFeedNoFeed_emptyResponse() {
        Assertions.assertEquals(0, intrinsicsController.getFeedForDevices(Lists.newArrayList()).size());
    }

    @Test
    @DirtiesContext
    public void intrinsicsController_getFeedForRaspi_corretFeedReturned() {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        FeedManager.getInstance().addToFeed(id1, DeviceType.RAPSI, FeedAction.ENTER_CONFIG_MODE);
        FeedManager.getInstance().addToFeed(id2, DeviceType.CUBE, FeedAction.LEAVE_CONFIG_MODE);

        List<FeedDTO> response = intrinsicsController.getFeedForDevices(Lists.newArrayList(id1, id2));

        Assertions.assertEquals(2, response.size());

        FeedDTO first = response.stream().filter(x -> x.getInternalId().equals(id1)).findFirst().orElseThrow();
        FeedDTO second = response.stream().filter(x -> x.getInternalId().equals(id2)).findFirst().orElseThrow();

        Assertions.assertEquals(first.getDeviceType(), DeviceType.RAPSI);
        Assertions.assertEquals(first.getFeedAction(), FeedAction.ENTER_CONFIG_MODE);

        Assertions.assertEquals(second.getDeviceType(), DeviceType.CUBE);
        Assertions.assertEquals(second.getFeedAction(), FeedAction.LEAVE_CONFIG_MODE);

        Assertions.assertNotEquals(first.getId().toString(), second.getId().toString());
    }

    @Test
    @DirtiesContext
    public void intrinsicsController_getFeedForRaspi_feedRemoved() {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        FeedManager.getInstance().addToFeed(id1, DeviceType.RAPSI, FeedAction.ENTER_CONFIG_MODE);
        FeedManager.getInstance().addToFeed(id2, DeviceType.CUBE, FeedAction.LEAVE_CONFIG_MODE);

        List<FeedDTO> response = intrinsicsController.getFeedForDevices(Lists.newArrayList(id1, id2));

        Assertions.assertEquals(2, response.size());

        response = intrinsicsController.getFeedForDevices(Lists.newArrayList(id1, id2));

        Assertions.assertEquals(0 ,response.size());
    }

    @Test
    @DirtiesContext
    public void intrinsicsController_completeFeedForRaspi_feedRemoved() {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        FeedManager.getInstance().addToFeed(id1, DeviceType.RAPSI, FeedAction.ENTER_CONFIG_MODE);
        FeedManager.getInstance().addToFeed(id2, DeviceType.CUBE, FeedAction.LEAVE_CONFIG_MODE);

        List<FeedDTO> response = intrinsicsController.getFeedForDevices(Lists.newArrayList(id2, id1));

        Assertions.assertEquals(2, response.size());

        intrinsicsController.completeFeed(response.get(0).getId());
        intrinsicsController.completeFeed(response.get(1).getId());

        response = intrinsicsController.getFeedForDevices(Lists.newArrayList(id2, id1));

        Assertions.assertEquals(0 ,response.size());
    }

}
