package uibk.ac.at.prodigaclient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uibk.ac.at.prodigaclient.dtos.GenericStringDTO;
import uibk.ac.at.prodigaclient.api.IntrinsicsControllerApi;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Client {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hallo, ich bin ein client!!!");

        CountDownLatch startSignal = new CountDownLatch(1);

        IntrinsicsControllerApi api = new ApiClient().createService(IntrinsicsControllerApi.class);

        api.pingUsingGET().enqueue(new Callback<GenericStringDTO>() {
            @Override
            public void onResponse(Call<GenericStringDTO> call, Response<GenericStringDTO> response) {
                System.out.println(response.body());
                startSignal.countDown();
            }

            @Override
            public void onFailure(Call<GenericStringDTO> call, Throwable throwable) {
                System.out.println(throwable.toString());
                startSignal.countDown();
            }
        });

        startSignal.await();
    }
}