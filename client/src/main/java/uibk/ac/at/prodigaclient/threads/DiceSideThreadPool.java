package uibk.ac.at.prodigaclient.threads;

import jdk.internal.net.http.common.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Small thread pool implementation for every dice
 */
public class DiceSideThreadPool {

    private final Map<String, Pair<DiceSideListenerThread, Thread>> threadPool = new HashMap<>();
    private final Logger logger = LogManager.getLogger();

    /**
     * Invokes the thread for the given dice
     * @param internalId The dices internal id
     */
    public void ensureRunningForDice(String internalId) {
        if(!threadPool.containsKey(internalId)) {
            DiceSideListenerThread listenerThread = new DiceSideListenerThread(internalId);
            Thread realThread = new Thread(listenerThread, "DiceSideListenerThread " + internalId);
            Pair<DiceSideListenerThread, Thread> pair = new Pair<>(listenerThread, realThread);
            threadPool.put(internalId, pair);
        }
        // First make sure the thread impl runs and the start the thread
        Pair<DiceSideListenerThread, Thread> thread = threadPool.get(internalId);
        thread.first.ensureRunning();
        thread.second.start();
    }

    /**
     * Stops the thread for the given dice
     * @param internalId The dices internal id
     */
    public void ensureStoppedForDice(String internalId) {
        if(threadPool.containsKey(internalId)) {
            Pair<DiceSideListenerThread, Thread> thread = threadPool.get(internalId);
            // First stop it
            thread.first.ensureStopped();
            try {
                // The we wait for the thread to exit
                thread.second.join();
            } catch (Exception ex) {
                logger.error("Error while joining thread " + thread.second.getName(), ex);
            }
        }

    }
}
