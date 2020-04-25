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

import java.util.ArrayList;
import java.util.List;

public class FeedThread implements Runnable {

    private final IntrinsicsControllerApi intrinsicsControllerApi;
    private final CubeManager cubeManager;
    private final Logger logger = LogManager.getLogger();

    public FeedThread() {
        this.intrinsicsControllerApi = Constants.getIntrinsicsControllerApi();
        this.cubeManager = Constants.getCubeManager();
    }

    @Override
    public void run() {
        logger.info("Feed Thread started!");
        try {
            while(true) {
                try {
                    logger.info("Feed Thread has awoken");

                    List<String> allInternalIds = new ArrayList<>();
                    allInternalIds.add(Constants.getInternalId());
                    allInternalIds.addAll(cubeManager.getCubeIDList());

                    ManualResetEventSlim mre = new ManualResetEventSlim(false);

                    ProdigaCallback<List<FeedDTO>> callback = new ProdigaCallback<>(mre,
                        Constants.getAuthAction(),
                        (call, response) -> {
                            if(response.body() != null && response.body().size() > 0) {
                                response.body().forEach(x -> {
                                    FeedHandler handler = FeedHandlerFactory.getFeedHandlerForFeed(x);
                                    if(handler != null) {
                                        handler.handle(x);
                                    }
                                });
                            } else {
                                logger.info("No feed for current devices");
                            }
                        });

                    intrinsicsControllerApi.getFeedForDevicesUsingGET(allInternalIds).enqueue(callback);

                    mre.waitDefaultAndLog("Error while waiting for server request on getting feed", logger);

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
}
