package uibk.ac.at.prodigaclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import uibk.ac.at.prodigaclient.api.CubeControllerApi;

public class Client {

    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws InterruptedException {

        try {
            logger.info("RaspberryPi registered with internal ID " + Constants.getInternalId());
        } catch (Exception ex) {
            logger.error("Error while reading MAC-Address! Aborting!", ex);
            return;
        }

        CubeControllerApi cubeControllerApi = new ApiClient().createService(CubeControllerApi.class);

        HistorySyncThread historySyncThread = new HistorySyncThread(cubeControllerApi, new CubeManager());

        Thread thread = new Thread(historySyncThread);
        thread.start();
        thread.join();
    }
}