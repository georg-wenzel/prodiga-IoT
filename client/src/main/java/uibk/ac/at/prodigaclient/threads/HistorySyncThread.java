package uibk.ac.at.prodigaclient.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import uibk.ac.at.prodigaclient.BluetoothUtility.HistoryEntry;
import uibk.ac.at.prodigaclient.Constants;
import uibk.ac.at.prodigaclient.api.CubeControllerApi;
import uibk.ac.at.prodigaclient.dtos.HistoryEntryDTO;
import uibk.ac.at.prodigaclient.utils.ManualResetEventSlim;
import uibk.ac.at.prodigaclient.utils.ProdigaCallback;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HistorySyncThread implements Runnable {

    private CubeControllerApi cubeControllerApi;
    private final Logger logger = LogManager.getLogger();

    public HistorySyncThread() {
        this.cubeControllerApi = Constants.getCubeControllerApi();
    }

    @Override
    public void run() {
        logger.info("History Sync Thread started");
        try{
            while (true) {
                try {
                    this.cubeControllerApi = Constants.getCubeControllerApi();

                    Set<String> connectedIds = CubeManager.getInstance().getCubeIDList();

                    logger.info("Found " + connectedIds.size() + " cubes");

                    for(String str : connectedIds) {
                        List<HistoryEntry> historyEntry = CubeManager.getInstance().getHistory(str);
                        List<HistoryEntryDTO> historyEntryDTOS = historyEntry.stream().map(x -> {
                            HistoryEntryDTO historyEntryDTO = new HistoryEntryDTO();
                            historyEntryDTO.setCubeInternalId(str);
                            historyEntryDTO.setSeconds(x.getSeconds());
                            historyEntryDTO.setSide(x.getID());
                            return historyEntryDTO;
                        }).collect(Collectors.toList());

                        logger.info("Syncing " + historyEntryDTOS.size() + " History Entries for Cube " + str);

                        ManualResetEventSlim mre = new ManualResetEventSlim(false);

                        ProdigaCallback<Void> callback = new ProdigaCallback<>(mre, Constants.getAuthAction());

                        cubeControllerApi.addBookingUsingPOST(historyEntryDTOS).enqueue(callback);

                        mre.waitDefaultAndLog("Error while waiting for server request on syncing history entries", logger);

                        connectedIds.forEach(x -> CubeManager.getInstance().deleteHistory(x));

                        logger.info("Finished Syncing History Entries for Cube " + str);
                    }

                    // sleeps for 15 minutes
                    Thread.sleep(900000);

                } catch (Exception ex) {
                    logger.error("Error in History Sync Thread", ex);

                }
            }
        } catch (Exception ex) {
            logger.error("Error in History Sync Thread, thread will quit now", ex);
        }
    }

}
