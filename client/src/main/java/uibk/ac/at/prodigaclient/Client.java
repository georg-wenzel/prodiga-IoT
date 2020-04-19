package uibk.ac.at.prodigaclient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import uibk.ac.at.prodigaclient.api.CubeControllerApi;
import uibk.ac.at.prodigaclient.dtos.GenericStringDTO;
import uibk.ac.at.prodigaclient.api.IntrinsicsControllerApi;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Client {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hallo, ich bims eins client!!!");

        CubeControllerApi cubeControllerApi = new ApiClient().createService(CubeControllerApi.class);

        HistorySyncThread historySyncThread = new HistorySyncThread(cubeControllerApi, new CubeManager());

        Thread thread = new Thread(historySyncThread);
        thread.start();
        thread.join();
    }
}