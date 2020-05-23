package uibk.ac.at.prodigaclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import uibk.ac.at.prodigaclient.threads.*;

import java.io.File;

public class Client {

    private static Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws InterruptedException {

        LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
        File f = new File("log4j2.xml");
        if(f.exists()) {
            context.setConfigLocation(f.toURI());
        }

        logger = LogManager.getLogger();

        if(args.length != 2) {
            logger.error("Usage: <server-address> <password>");
            return;
        }

        String serverAddress = args[0];
        String password = args[1];

        if(!serverAddress.startsWith("http://") || !serverAddress.endsWith("/")) {
            logger.error("Server Address needs to have form http://<ip-address>:<port>/");
            return;
        }

        Constants.setPassword(password);
        Constants.setServerAddress(serverAddress);

        // We start the client and read the MAC Address - this is the "ID" of the current client
        try {
            logger.info("RaspberryPi registered with internal ID " + Constants.getInternalId());
        } catch (Exception ex) {
            // If this fails we log it and kill the client
            logger.error("Error while reading MAC-Address! Aborting!", ex);
            return;
        }

        // Set up auth thread
        AuthThread authThread = new AuthThread();

        // Get a Function Pointer to invoke the auth thread - used by other threads to invoke the auth process
        Constants.setAuthAction(authThread::invokeAuth);

        // Set aup all other threads
        HistorySyncThread historySyncThread = new HistorySyncThread();
        FeedThread feedThread = new FeedThread();
        IntrinsicsThread intrinsicsThread = new IntrinsicsThread();
        PendingDiceThread pendingDiceThread = new PendingDiceThread();

        Thread authThreadThread = new Thread(authThread, "AuthThread");
        Thread historySyncThreadThread = new Thread(historySyncThread, "HistorySyncThread");
        Thread feedThreadThread = new Thread(feedThread, "FeedThreadThread");
        Thread intrinsicsThreadThread = new Thread(intrinsicsThread, "IntrinsicsThreadThread");
        Thread pendingDiceThreadThread = new Thread(pendingDiceThread, "PendingDiceThreadThread");

        // Then we start the auth thread
        authThreadThread.start();
        historySyncThreadThread.start();
        feedThreadThread.start();
        intrinsicsThreadThread.start();
        pendingDiceThreadThread.start();
        authThreadThread.join();
        historySyncThreadThread.join();
        feedThreadThread.join();
        intrinsicsThreadThread.join();
        pendingDiceThreadThread.join();
    }
}