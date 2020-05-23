package uibk.ac.at.prodigaclient.api;

import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import uibk.ac.at.prodigaclient.dtos.*;

import java.util.List;

public interface CubeControllerApi {
    /**
     * createToken
     *
     * @param body request (required)
     * @return Call&lt;HistoryEntryDTO&gt;
     */
    @Headers({
            "Content-Type:application/json"
    })
    @POST("api/booking")
    Call<Void> addBookingUsingPOST(@retrofit2.http.Body List<HistoryEntryDTO> body);

    /**
     * Notifies the server about a new dice side
     *
     * @param body request (required)
     * @return Call&lt;Void&gt;
     */
    @Headers({
            "Content-Type:application/json"
    })
    @POST("api/newSide")
    Call<Void> notifyNewSideUsingPOST(@retrofit2.http.Body NewDiceSideRequestDTO body);

    /**
     * Notifies the server about a new dices
     *
     * @param body request (required)
     * @return Call&lt;Void&gt;
     */
    @Headers({
            "Content-Type:application/json"
    })
    @POST("api/dice")
    Call<Void> registerUsingPOST(@retrofit2.http.Body List<PendingDiceDTO> body);

}
