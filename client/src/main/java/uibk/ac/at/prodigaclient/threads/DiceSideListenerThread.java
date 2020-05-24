package uibk.ac.at.prodigaclient.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import uibk.ac.at.prodigaclient.Constants;
import uibk.ac.at.prodigaclient.api.CubeControllerApi;
import uibk.ac.at.prodigaclient.dtos.NewDiceSideRequestDTO;
import uibk.ac.at.prodigaclient.utils.ManualResetEventSlim;
import uibk.ac.at.prodigaclient.utils.ProdigaCallback;

/**
 * Thread which notifies the server about new dice side - when the dice changes sides
 */
public class DiceSideListenerThread implements Runnable{

    private final Logger logger = LogManager.getLogger();
    private CubeControllerApi cubeControllerApi;
    private final String internalId;
    private final Object lock = new Object();
    private volatile boolean running = false;

    public DiceSideListenerThread(String internalId) {
        cubeControllerApi = Constants.getCubeControllerApi();
        this.internalId = internalId;
    }

    /**
     * Main loop runs when ensureRunning is called and stops when ensureStopped is called
     */
    @Override
    public void run() {
        logger.info("Feed Thread started!");
        try {
            while(true) {
                try {
                    cubeControllerApi = Constants.getCubeControllerApi();

                    logger.info("Dice Side Listener Thread has awoken");

                    ManualResetEventSlim mre = new ManualResetEventSlim(false);

                    ProdigaCallback<Void> callback = new ProdigaCallback<>(mre, Constants.getAuthAction());

                    NewDiceSideRequestDTO request = new NewDiceSideRequestDTO();
                    request.setInternalId(internalId);
                    request.setSide(CubeManager.getInstance().getCurrentSide(this.internalId));

                    cubeControllerApi.notifyNewSideUsingPOST(request).enqueue(callback);

                    mre.waitDefaultAndLog("Error while waiting for server request on new dice sied", logger);

                    synchronized (lock) {
                        if(!running) {
                            break;
                        }
                    }

                    Thread.sleep(1000);
                } catch (Exception ex) {
                    logger.error("Error in Feed Thread", ex);
                }
            }
        } catch (Exception ex) {
            logger.error("Error in Feed Thread, thread will quit now", ex);
        }
        logger.info("Feed Thread finished!");
    }

    public void ensureRunning() {
        synchronized (lock) {
            running = true;

            CubeManager.getInstance().connectToCube(this.internalId);
        }
    }

    public void ensureStopped() {
        synchronized (lock) {
            running = false;
        }

        CubeManager.getInstance().disconnectFromCube(this.internalId);
    }
}
