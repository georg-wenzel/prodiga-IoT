package uibk.ac.at.prodiga.utils;

import org.springframework.util.StringUtils;
import uibk.ac.at.prodiga.rest.dtos.DeviceType;
import uibk.ac.at.prodiga.rest.dtos.FeedAction;
import uibk.ac.at.prodiga.rest.dtos.FeedDTO;

import java.util.*;

public class FeedManager {

    private final Map<String, List<FeedDTO>> openFeedItems = new HashMap<>();
    private final Map<UUID, FeedDTO> pendingFeedItems = new HashMap<>();

    private final Object lock = new Object();

    private static final class InstanceHolder {
        static final FeedManager instance = new FeedManager();
    }

    private FeedManager() { }

    public static FeedManager getInstance () {
        return InstanceHolder.instance;
    }

    public UUID addToFeed(String internalId, DeviceType deviceType, FeedAction action) {
        if(StringUtils.isEmpty(internalId)) {
            throw new IllegalArgumentException("internalId");
        }

        UUID id = UUID.randomUUID();

        FeedDTO feed = new FeedDTO();
        feed.setInternalId(internalId);
        feed.setDeviceType(deviceType);
        feed.setFeedAction(action);
        feed.setId(id);

        synchronized (lock) {
            List<FeedDTO> open = openFeedItems.getOrDefault(internalId, null);

            if(open == null) {
                open = new ArrayList<>();
                open.add(feed);
                openFeedItems.put(internalId, open);
            } else {
                open.add(feed);
            }
        }

        return id;
    }

    public List<FeedDTO> getFeed(List<String> internalIds) {
        List<FeedDTO> result = new ArrayList<>();

        internalIds.forEach(x -> result.addAll(getFeed(x)));

        return result;
    }

    public List<FeedDTO> getFeed(String internalId) {
        if(StringUtils.isEmpty(internalId)) {
            throw new IllegalArgumentException("internalId");
        }

        synchronized (lock) {
            List<FeedDTO> result =  openFeedItems.getOrDefault(internalId, new ArrayList<>());

            result.forEach(x -> pendingFeedItems.put(x.getId(), x));

            return result;
        }
    }

    public void completeFeedItem(UUID id) {
        if(id == null) {
            throw new IllegalArgumentException("id");
        }

        synchronized (lock) {
            pendingFeedItems.remove(id);
        }
    }
}
