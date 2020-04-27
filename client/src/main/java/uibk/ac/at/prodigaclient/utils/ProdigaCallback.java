package uibk.ac.at.prodigaclient.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.function.BiConsumer;

public class ProdigaCallback<T> implements Callback<T> {

    private final Logger logger = LogManager.getLogger();

    private final BiConsumer<Call<T>, Response<T>> responseAction;
    private final BiConsumer<Call<T>, Throwable> failureAction;
    private final ManualResetEventSlim mre;
    private final Action invokeAuthAction;

    public ProdigaCallback(ManualResetEventSlim mre) {
        this(mre, null, null, null);
    }

    public ProdigaCallback(ManualResetEventSlim mre, Action invokeAuthAction) {
        this(mre, invokeAuthAction, null, null);
    }

    public ProdigaCallback(ManualResetEventSlim mre,
                           Action invokeAuthAction,
                           BiConsumer<Call<T>, Response<T>> responseAction) {
        this(mre, invokeAuthAction, responseAction, null);
    }

    public ProdigaCallback(ManualResetEventSlim mre,
                           Action invokeAuthAction,
                           BiConsumer<Call<T>, Response<T>> responseAction,
                           BiConsumer<Call<T>, Throwable> failureAction) {
        if(mre == null) {
            throw new IllegalArgumentException("mre");
        }

        this.mre = mre;
        this.invokeAuthAction = invokeAuthAction;
        this.responseAction = responseAction;
        this.failureAction = failureAction;
    }

    @Override
    public void onResponse(@NotNull Call<T> call, @NotNull Response<T> response) {
        try {

            if(invokeAuthAction != null && response.code() == 401) {
                invokeAuthAction.invoke();
            }

            if(responseAction != null) {
                responseAction.accept(call, response);
            }

        } catch (Exception ex) {
            CallLoggerHelper.logCallError(call, ex, logger);
        } finally {
            mre.set();
        }
    }

    @Override
    public void onFailure(@NotNull Call<T> call, @NotNull Throwable throwable) {
        try{
            CallLoggerHelper.logCallError(call, throwable, logger);

            if(failureAction != null) {
                failureAction.accept(call, throwable);
            }

        } catch (Exception ex) {
            CallLoggerHelper.logCallError(call, ex, logger);
        } finally {
            mre.set();
        }
    }
}
