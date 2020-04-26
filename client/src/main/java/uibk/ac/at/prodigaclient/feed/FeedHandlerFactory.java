package uibk.ac.at.prodigaclient.feed;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uibk.ac.at.prodigaclient.dtos.FeedDTO;

/**
 * Factory class used to create the handler depending on the device type
 */
public class FeedHandlerFactory {

    private static final CubeFeedHandler cubeFeedHandler = new CubeFeedHandler();
    private static final RaspiFeedHandler raspiFeedHandler = new RaspiFeedHandler();
    private static final Logger logger = LogManager.getLogger();

    /**
     * Returns the handler for the given feed item
     * @param feed The feed item
     * @return The handler implementation
     */
    public static FeedHandler getFeedHandlerForFeed(FeedDTO feed) {
        switch (feed.getDeviceType()) {
            case CUBE:
                return cubeFeedHandler;
            case RAPSI:
                return raspiFeedHandler;
            default:
                logger.warn("No Feed Manager for device " + feed.getDeviceType() + " implemented");
                return null;
        }
    }
}
