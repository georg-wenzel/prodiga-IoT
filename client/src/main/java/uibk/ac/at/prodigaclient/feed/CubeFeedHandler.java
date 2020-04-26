package uibk.ac.at.prodigaclient.feed;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uibk.ac.at.prodigaclient.dtos.FeedDTO;
import uibk.ac.at.prodigaclient.threads.DiceSideThreadPool;

/**
 * Handles incoming feeds for cubes
 */
public class CubeFeedHandler implements FeedHandler {

    private final Logger logger = LogManager.getLogger();
    private final DiceSideThreadPool diceSideThreadPool = new DiceSideThreadPool();

    /**
     * Implementation of the handle method for cubes
     * @param feed The feed item to handle
     */
    @Override
    public void handle(FeedDTO feed) {
        switch(feed.getFeedAction()) {
            case ENTER_CONFIG_MODE:
                diceSideThreadPool.ensureRunningForDice(feed.getInternalId());
                break;
            case LEAVE_CONFIG_MODE:
                diceSideThreadPool.ensureStoppedForDice(feed.getInternalId());
                break;
            default:
                logger.warn("Feed action " + feed.getFeedAction() + " not implemented for Cube");
        }
    }
}
