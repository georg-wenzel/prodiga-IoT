package uibk.ac.at.prodiga.utils;

import java.util.concurrent.Future;

public class AsyncHelper {

    public static <T> T getAsyncResultOrThrow(Future<T> task) throws Exception {
        try {
            return task.get();
        } catch (Exception ex) {
            ProdigaGeneralExpectedException.throwWrappedException(ex);
        }
        throw new ProdigaGeneralExpectedException("This should never happen!! : ^)", MessageType.ERROR);
    }

}
