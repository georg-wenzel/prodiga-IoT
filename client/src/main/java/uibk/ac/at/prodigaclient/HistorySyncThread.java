package uibk.ac.at.prodigaclient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uibk.ac.at.prodigaclient.BluetoothUtility.CubeManager;
import uibk.ac.at.prodigaclient.BluetoothUtility.HistoryEntry;
import uibk.ac.at.prodigaclient.api.CubeControllerApi;
import uibk.ac.at.prodigaclient.dtos.HistoryEntryDTO;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HistorySyncThread implements Runnable {

    private final CubeControllerApi cubeControllerApi;
    private final CubeManager cubeManager;

    public HistorySyncThread(CubeControllerApi cubeControllerApi, CubeManager cubeManager) {
        this.cubeControllerApi = cubeControllerApi;
        this.cubeManager = cubeManager;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Set<String> connectedIds = cubeManager.getCubeIDList();
                CountDownLatch countDownLatch = new CountDownLatch(connectedIds.size());

                for(String str : connectedIds){
                    List<HistoryEntry> historyEntry = cubeManager.getHistory(str);
                    List<HistoryEntryDTO> historyEntryDTOS = historyEntry.stream().map(x -> {
                        HistoryEntryDTO historyEntryDTO = new HistoryEntryDTO();
                        historyEntryDTO.setCubeInternalId(str);
                        historyEntryDTO.setSeconds(x.getSeconds());
                        historyEntryDTO.setSide(x.getID());
                        return historyEntryDTO;
                    }).collect(Collectors.toList());

                    cubeControllerApi.addBookingUsingPOST(historyEntryDTOS).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            countDownLatch.countDown();
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable throwable) {
                            countDownLatch.countDown();
                        }
                    });

                }
                countDownLatch.await(1, TimeUnit.MINUTES);

                // sleeps for 15 minutes
                Thread.sleep(900000);

            } catch (Exception ex) {
                // ignore
            }
        }
    }

}
