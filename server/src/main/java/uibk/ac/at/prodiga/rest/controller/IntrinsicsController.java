package uibk.ac.at.prodiga.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uibk.ac.at.prodiga.rest.dtos.FeedDTO;
import uibk.ac.at.prodiga.rest.dtos.GenericStringDTO;
import uibk.ac.at.prodiga.rest.dtos.InstrincsDTO;
import uibk.ac.at.prodiga.utils.FeedManager;

import java.util.List;
import java.util.UUID;

@RestController
public class IntrinsicsController {

    @GetMapping("/api/ping")
    public GenericStringDTO ping() {
        GenericStringDTO dto = new GenericStringDTO();
        dto.setResponse("Pong \uD83D\uDC1D");
        return dto;
    }

    @PostMapping("/api/instrincs")
    public void push(@RequestBody InstrincsDTO instrincs) {

    }

    @GetMapping("/api/feed")
    public List<FeedDTO> getFeedForDevices(@RequestBody List<String> internalId) {
        return FeedManager.getInstance().getFeed(internalId);
    }

    @PostMapping("/api/feed")
    public void completeFeed(UUID feedId) {
        FeedManager.getInstance().completeFeedItem(feedId);
    }
}
