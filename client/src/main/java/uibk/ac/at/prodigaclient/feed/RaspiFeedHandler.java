package uibk.ac.at.prodigaclient.feed;

import uibk.ac.at.prodigaclient.dtos.FeedDTO;

/**
 * Handles feed items for the raspi
 */
public class RaspiFeedHandler implements FeedHandler{

    /**
     * Implementation for the feed handler for the raspi
     * @param feed The given feed item
     */
    @Override
    public void handle(FeedDTO feed) {
        //TODO (Max): What can we do here?
    }

    /**
     * Returns whether the given needs to report back to the server
     * @param feedDTO The given feed item
     * @return Whether to respond to the server
     */
    @Override
    public boolean needsToReportToServer(FeedDTO feedDTO) {
        return false;
    }
}
