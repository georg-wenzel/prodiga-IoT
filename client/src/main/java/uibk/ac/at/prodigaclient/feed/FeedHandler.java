package uibk.ac.at.prodigaclient.feed;

import uibk.ac.at.prodigaclient.dtos.FeedDTO;

public interface FeedHandler {

    void handle(FeedDTO feed);

    boolean needsToReportToServer(FeedDTO feedDTO);
}
