package uibk.ac.at.prodigaclient.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import uibk.ac.at.prodigaclient.Constants;
import uibk.ac.at.prodigaclient.api.IntrinsicsControllerApi;
import uibk.ac.at.prodigaclient.dtos.FeedDTO;
import uibk.ac.at.prodigaclient.feed.FeedHandler;
import uibk.ac.at.prodigaclient.feed.FeedHandlerFactory;
import uibk.ac.at.prodigaclient.utils.ManualResetEventSlim;
import uibk.ac.at.prodigaclient.utils.ProdigaCallback;

import java.util.*;

public class FeedThread implements Runnable {

    private final IntrinsicsControllerApi intrinsicsControllerApi;
    private final Logger logger = LogManager.getLogger();
    private final List<FeedDTO> toCompleteItems = new ArrayList<>();

    public FeedThread() {
        this.intrinsicsControllerApi = Constants.getIntrinsicsControllerApi();
    }

    @Override
    public void run() {
        logger.info("Feed Thread started!");
        try {
            while(true) {
                try {
                    logger.info("Feed Thread has awoken");

                    handleNewItems();
                    handleToCompleteItems();

                    // 5 Seconds
                    Thread.sleep(5000);

                } catch (Exception ex) {
                    logger.error("Error in Feed Thread", ex);
                }
            }
        } catch (Exception ex) {
            logger.error("Error in Feed Thread, thread will quit now", ex);
        }
        logger.info("Feed Thread finished!");
    }

    private void handleNewItems() {
        List<String> allInternalIds = new ArrayList<>();
        allInternalIds.add(Constants.getInternalId());
        allInternalIds.addAll(CubeManager.getInstance().getCubeIDList());

        ManualResetEventSlim mre = new ManualResetEventSlim(false);

        ProdigaCallback<List<FeedDTO>> callback = new ProdigaCallback<>(mre,
            Constants.getAuthAction(),
            (call, response) -> {
                if(response.body() != null && response.body().size() > 0) {
                    response.body().forEach(x -> {
                        FeedHandler handler = FeedHandlerFactory.getFeedHandlerForFeed(x);
                        if(handler != null) {
                            handler.handle(x);
                            if(handler.needsToReportToServer(x)) {
                                toCompleteItems.add(x);
                            }
                        }
                    });
                } else {
                    logger.info("No feed for current devices");
                }
            });

        intrinsicsControllerApi.getFeedForDevicesUsingGET(allInternalIds).enqueue(callback);

        mre.waitDefaultAndLog("Error while waiting for server request on getting feed", logger);
    }

    private void handleToCompleteItems() {

        List<ManualResetEventSlim> mres = new ArrayList<>();

        for (FeedDTO feed: toCompleteItems) {
            ManualResetEventSlim mre = new ManualResetEventSlim(false);

            ProdigaCallback<Void> callback = new ProdigaCallback<>(mre, Constants.getAuthAction());

            logger.info("Completing feed item " + feed.getId());

            intrinsicsControllerApi.completeFeedUsingPOST(feed.getId()).enqueue(callback);

            mres.add(mre);

        }

        mres.forEach(x ->
                x.waitDefaultAndLog("Error while waiting for server request on completing feed item", logger));
    }
}
