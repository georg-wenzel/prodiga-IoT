package uibk.ac.at.prodiga.utils;

import uibk.ac.at.prodiga.rest.dtos.FeedDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FeedManager {

    private Map<String, FeedDTO> openFeedItems = new HashMap<>();
    private Map<UUID, >

    private static final class InstanceHolder {
        static final FeedManager instance = new FeedManager();
    }

    private FeedManager() { }

    public static FeedManager getInstance () {
        return InstanceHolder.instance;
    }


}
