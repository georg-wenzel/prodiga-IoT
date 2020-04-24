package uibk.ac.at.prodigaclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uibk.ac.at.prodigaclient.api.AuthControllerApi;
import uibk.ac.at.prodigaclient.dtos.JwtRequestDTO;
import uibk.ac.at.prodigaclient.dtos.JwtResponseDTO;
import uibk.ac.at.prodigaclient.utils.CallLoggerHelper;
import uibk.ac.at.prodigaclient.utils.ManualResetEventSlim;
import uibk.ac.at.prodigaclient.utils.ProdigaCallback;

public class AuthThread implements Runnable {

    private final Logger logger = LogManager.getLogger();

    private final Object monitor = new Object();

    private final AuthControllerApi authControllerApi;

    public AuthThread(AuthControllerApi authControllerApi) {
        this.authControllerApi = authControllerApi;
    }

    @Override
    public void run() {
        logger.info("Auth Thread started!");

        try {
            while(true) {
                try {
                    logger.info("Auth Thread has awoken");
                    handleRegister();
                    handleLogin();

                    // 5 Minutes
                    try {
                        monitor.wait(300000);
                    } catch (Exception ex) {
                        logger.info("Auth Thread has timeout");
                    }
                } catch (Exception ex) {
                    logger.error("Error in Auth Thread", ex);
                }
            }
        } catch (Exception ex) {
            logger.error("Error in Auth Thread, thread will quit now", ex);
        }
        logger.info("Auth Thread finished!");
    }

    public void invokeAuth() {
        monitor.notifyAll();
    }

    private void handleRegister() {
        ManualResetEventSlim mre = new ManualResetEventSlim(false);
        logger.info("Registering the raspi!");

        ProdigaCallback<Void> callback = new ProdigaCallback<>(mre);

        authControllerApi.registerUsingPOST(Constants.getInternalId()).enqueue(callback);

        mre.waitDefaultAndLog("Error while waiting for server request on register", logger);

        logger.info("Finished registering the raspi!");
    }

    private void handleLogin() {
        logger.info("Raspi login!");
        try {
            for(int i = 0; i < 10; i++) {
                logger.info("Raspi login current loop " + i);

                JwtRequestDTO request = new JwtRequestDTO();
                request.setInternalId(Constants.getInternalId());
                request.setPassword(Constants.getPassword());

                ManualResetEventSlim mre = new ManualResetEventSlim(false);

                final boolean[] success = {false};

                ProdigaCallback<JwtResponseDTO> callback = new ProdigaCallback<>(mre, null, (call, response) -> {
                    if(response.code() == 200 && response.body() != null) {
                        Constants.setJwt(response.body().getToken());
                        logger.info("Got token " + Constants.getJwt());
                        success[0] = true;
                    }
                });

                authControllerApi.createTokenUsingPOST(request).enqueue(callback);

                mre.waitDefaultAndLog("Error while waiting for server request on login", logger);

                if (success[0]) {
                    break;
                }
            }
        } catch (Exception ex) {
            logger.error("Error in Login loop, loop will quit now", ex);
        }
        logger.info("Login loop finished!");
    }
}
































