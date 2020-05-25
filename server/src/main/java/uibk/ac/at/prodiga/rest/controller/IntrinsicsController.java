package uibk.ac.at.prodiga.rest.controller;

import org.springframework.web.bind.annotation.*;
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.rest.dtos.FeedDTO;
import uibk.ac.at.prodiga.rest.dtos.GenericStringDTO;
import uibk.ac.at.prodiga.rest.dtos.IntrinsicsDTO;
import uibk.ac.at.prodiga.services.DiceService;
import uibk.ac.at.prodiga.services.RaspberryPiService;
import uibk.ac.at.prodiga.utils.FeedManager;

import java.util.List;
import java.util.UUID;

@RestController
public class IntrinsicsController {

    private final RaspberryPiService raspberryPiService;
    private final DiceService diceService;

    public IntrinsicsController(RaspberryPiService raspberryPiService, DiceService diceService) {
        this.raspberryPiService = raspberryPiService;
        this.diceService = diceService;
    }

    @GetMapping("/api/ping")
    public GenericStringDTO ping() {
        GenericStringDTO dto = new GenericStringDTO();
        dto.setResponse("Pong \uD83D\uDC1D");
        return dto;
    }

    @PostMapping("/api/instrincs")
    public void push(@RequestBody IntrinsicsDTO instrincs) {
        if(instrincs == null) {
            return;
        }

        if(instrincs.getCubeIntrinsics().size() > 0 &&
                raspberryPiService.findByInternalId(instrincs.getInternalId()).isPresent()) {
            instrincs.getCubeIntrinsics().forEach(x -> {
                Dice d = diceService.getDiceByInternalId(x.getInternalId());
                if(d != null){
                    d.setLastBatteryStatus(x.getBatteryStatus());
                    try {
                        diceService.saveWithoutAuth(d, d.getUser());
                    } catch (Exception ex) {
                        // Ignore - nothing we can do about it here
                    }
                }
            });
        }
    }

    @PostMapping("/api/feed")
    public List<FeedDTO> getFeedForDevices(@RequestBody List<String> internalId) {
        return FeedManager.getInstance().getFeed(internalId);
    }

    @PatchMapping("/api/feed")
    public void completeFeed(@RequestBody UUID feedId) {
        FeedManager.getInstance().completeFeedItem(feedId);
    }
}
