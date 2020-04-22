package uibk.ac.at.prodigaclient.utils;

import org.apache.logging.log4j.Logger;
import org.graalvm.compiler.replacements.amd64.AMD64StringIndexOfNode;
import uibk.ac.at.prodigaclient.Constants;

/**
 * Since Java doesn't provide a ManualRestEvent nor a ManualRestEventSlim we create our own
 * Ported from https://referencesource.microsoft.com/#mscorlib/system/threading/ManualResetEventSlim.cs,07ccbd30abe2a211
 */
public class ManualResetEventSlim {

    private final Object lock = new Object();
    private volatile boolean state = false;

    public ManualResetEventSlim(boolean initialState) {
        this.state = initialState;
    }

    public void waitOne() throws InterruptedException {
        waitOne(-1);
    }

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

    public void waitDefaultAndLog(String message, Logger logger) {
        try {
            waitOne(Constants.DEFAULT_WAIT_TIMEOUT_MILLIS);
        } catch (InterruptedException e) {
            logger.error(message, e);
        }
    }

    public void set() {
        synchronized (lock) {
            state = true;
            lock.notifyAll();
        }
    }
}
