package uibk.ac.at.prodigaclient.utils;

import org.apache.logging.log4j.Logger;
import uibk.ac.at.prodigaclient.Constants;

/**
 * Since Java doesn't provide a ManualRestEvent nor a ManualRestEventSlim
 * we create our own from C# <3
 * Ported from https://referencesource.microsoft.com/#mscorlib/system/threading/ManualResetEventSlim.cs,07ccbd30abe2a211
 */
public class ManualResetEventSlim {

    private final Object lock = new Object();
    private volatile boolean state = false;

    public ManualResetEventSlim(boolean initialState) {
        this.state = initialState;
    }

    /**
     * Waits until the set method is called
     * @throws InterruptedException Thrown when the thread is busy
     */
    public void waitOne() throws InterruptedException {
        waitOne(-1);
    }

    /**
     * Waits until the set method is called or the given timeout is reached
     * @param timeoutMillis The timeout
     * @throws InterruptedException Thrown when the thread is busy
     */
    public void waitOne(int timeoutMillis) throws InterruptedException {
        synchronized (lock) {
            while(!state) {
                if(timeoutMillis >= 0) {
                    lock.wait(timeoutMillis);
                } else {
                    lock.wait();
                }
            }
        }
    }

    /**
     * Waits for the default amount of time (20s) and logs all exceptions to the given logger
     * @param message The message for the logger
     * @param logger The logger
     */
    public void waitDefaultAndLog(String message, Logger logger) {
        try {
            waitOne(Constants.DEFAULT_WAIT_TIMEOUT_MILLIS);
        } catch (InterruptedException e) {
            logger.error(message, e);
        }
    }

    /**
     * Notifies all waiting threads
     */
    public void set() {
        synchronized (lock) {
            state = true;
            lock.notifyAll();
        }
    }
}
