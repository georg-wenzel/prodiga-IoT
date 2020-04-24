package uibk.ac.at.prodigaclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import uibk.ac.at.prodigaclient.api.AuthControllerApi;
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

        CubeControllerApi cubeControllerApi = Constants.getClient().createService(CubeControllerApi.class);
        AuthControllerApi authControllerApi = Constants.getClient().createService(AuthControllerApi.class);

        AuthThread authThread = new AuthThread(authControllerApi);

        Constants.setAuthAction(authThread::invokeAuth);

        HistorySyncThread historySyncThread = new HistorySyncThread(cubeControllerApi, new CubeManager());

        Thread historySyncThreadThread = new Thread(historySyncThread, "HistorySyncThread");
        Thread authThreadThread = new Thread(authThread, "AuthThread");

        historySyncThreadThread.start();
        authThreadThread.start();
        historySyncThreadThread.join();
        authThreadThread.join();
    }
}