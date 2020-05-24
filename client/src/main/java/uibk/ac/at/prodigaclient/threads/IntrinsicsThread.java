package uibk.ac.at.prodigaclient.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import uibk.ac.at.prodigaclient.Constants;
import uibk.ac.at.prodigaclient.api.IntrinsicsControllerApi;
import uibk.ac.at.prodigaclient.dtos.CubeIntrinsicsDTO;
import uibk.ac.at.prodigaclient.dtos.IntrinsicsDTO;
import uibk.ac.at.prodigaclient.utils.ManualResetEventSlim;
import uibk.ac.at.prodigaclient.utils.ProdigaCallback;

import java.util.Set;
import java.util.stream.Collectors;

public class IntrinsicsThread implements Runnable {

    private IntrinsicsControllerApi intrinsicsControllerApi;
    private final Logger logger = LogManager.getLogger();

    public IntrinsicsThread() {
        intrinsicsControllerApi = Constants.getIntrinsicsControllerApi();
    }

    @Override
    public void run() {
        logger.info("Intrinsics Thread started");
        try{
            while(true) {
                try{
                    logger.info("Intrinsics Thread has awoken");
                    intrinsicsControllerApi = Constants.getIntrinsicsControllerApi();

                    Set<String> connectedCubes = CubeManager.getInstance().getCubeIDList();

                    IntrinsicsDTO dto = new IntrinsicsDTO();
                    dto.setInternalId(Constants.getInternalId());
                    dto.setCubeIntrinsics(connectedCubes.stream().map(x -> {
                        CubeIntrinsicsDTO cube = new CubeIntrinsicsDTO();
                        cube.setBatteryStatus(CubeManager.getInstance().getBattery(x));
                        cube.setInternalId(x);
                        return cube;
                    }).collect(Collectors.toList()));

                    logger.info("Sending " + connectedCubes.size() + " intrinsics");

                    ManualResetEventSlim mre = new ManualResetEventSlim(false);

                    ProdigaCallback<Void> callback = new ProdigaCallback<>(mre, Constants.getAuthAction());

                    intrinsicsControllerApi.pushUsingPOST(dto).enqueue(callback);

                    mre.waitDefaultAndLog("Error while waiting for server request on sending intrinsics", logger);

                    logger.info("Finished sending intrinsics");

                    // 10 min
                    Thread.sleep(600000);

                } catch (Exception ex) {
                    logger.error("Error in Intrinsics Thread", ex);
                }
            }
        } catch (Exception ex){
            logger.error("Error in Intrinsics Thread, thread will quit now", ex);
        }
    }
}
