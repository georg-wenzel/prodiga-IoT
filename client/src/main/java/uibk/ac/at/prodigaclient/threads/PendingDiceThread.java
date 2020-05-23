package uibk.ac.at.prodigaclient.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import uibk.ac.at.prodigaclient.Constants;
import uibk.ac.at.prodigaclient.api.CubeControllerApi;
import uibk.ac.at.prodigaclient.dtos.PendingDiceDTO;
import uibk.ac.at.prodigaclient.utils.ManualResetEventSlim;
import uibk.ac.at.prodigaclient.utils.ProdigaCallback;

import java.util.List;
import java.util.stream.Collectors;

public class PendingDiceThread implements Runnable {

    private CubeControllerApi cubeControllerApi = null;
    private final Logger logger = LogManager.getLogger();

    public PendingDiceThread() {
        cubeControllerApi = Constants.getCubeControllerApi();
    }

    @Override
    public void run() {
        logger.info("Pending Dice Thread started");
        try {
            while(true) {
                try {
                    cubeControllerApi = Constants.getCubeControllerApi();

                    logger.info("Pending Dice Thread has awoken");

                    List<PendingDiceDTO> dices = CubeManager.getInstance().getCubeIDList().stream()
                            .map(x -> {
                                PendingDiceDTO d = new PendingDiceDTO();
                                d.setDiceInternalId(x);
                                d.setRaspiInternalId(Constants.getInternalId());

                                return d;
                            }).collect(Collectors.toList());

                    logger.info("Sending " + dices.size() + " dices to server");

                    ManualResetEventSlim mre = new ManualResetEventSlim(false);

                    ProdigaCallback<Void> callback = new ProdigaCallback<>(mre, Constants.getAuthAction());

                    cubeControllerApi.registerUsingPOST(dices).enqueue(callback);

                    mre.waitDefaultAndLog("Error while waiting for server request on new dices", logger);

                    logger.info("Finished sending new dices");

                    // 1 min
                    Thread.sleep(60000);

                } catch (Exception ex) {
                    logger.error("Error in Pending Dice Thread", ex);
                }
            }
        } catch (Exception ex) {
            logger.error("Error in Pending Dice Thread, thread will quit now", ex);
        }
    }
}
