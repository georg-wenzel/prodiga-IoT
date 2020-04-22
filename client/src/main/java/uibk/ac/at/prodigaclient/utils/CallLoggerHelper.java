package uibk.ac.at.prodigaclient.utils;

import org.apache.logging.log4j.Logger;
import retrofit2.Call;

public class CallLoggerHelper {

    public static void loggCallError(Call<?> call, Throwable ex, Logger logger) {
        String url = call.request().url().toString(); // No null check needed here - annotation and stuff
        String method = call.request().method();

        logger.error("Error while calling " + method + " " + url, ex);

    }

}
