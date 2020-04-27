package uibk.ac.at.prodigaclient.utils;

import org.apache.logging.log4j.Logger;
import retrofit2.Call;

/**
 * Helper class for logging requests
 */
public class CallLoggerHelper {

    /**
     * Logs the given failed call using the given logger
     * @param call The call
     * @param ex The exception which occurred
     * @param logger The logger to use
     */
    public static void logCallError(Call<?> call, Throwable ex, Logger logger) {
        String url = call.request().url().toString(); // No null check needed here - annotation and stuff
        String method = call.request().method();

        logger.error("Error while calling " + method + " " + url, ex);

    }

}
