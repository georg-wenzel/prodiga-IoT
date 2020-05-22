package uibk.ac.at.prodigaclient.api;

import uibk.ac.at.prodigaclient.dtos.FeedDTO;
import uibk.ac.at.prodigaclient.dtos.GenericStringDTO;

import retrofit2.Call;
import retrofit2.http.*;

import uibk.ac.at.prodigaclient.dtos.IntrinsicsDTO;

import java.util.*;

public interface IntrinsicsControllerApi {
  /**
   * ping
   * 
   * @return Call&lt;GenericStringDTO&gt;
   */
  @GET("api/ping")
  Call<GenericStringDTO> pingUsingGET();
    

  /**
   * push
   * 
   * @param body instrincs (required)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/instrincs")
  Call<Void> pushUsingPOST(
                    @retrofit2.http.Body IntrinsicsDTO body
  );

  /**
   * Gets the feed for the given devices
   *
   * @param internalIds Internal Ids used by this client
   * @return Call&lt;List<FeedDTO>&gt;
   */
  @Headers({
          "Content-Type:application/json"
  })
  @POST("api/feed")
  Call<List<FeedDTO>> getFeedForDevicesUsingPOST(
          @retrofit2.http.Body List<String> internalIds
  );

  /**
   * Completes the given feed item on the server
   *
   * @param feedId The feeds id
   * @return Call&lt;Void&gt;
   */
  @Headers({
          "Content-Type:application/json"
  })
  @PATCH("api/feed")
  Call<Void> completeFeedUsingPATCH(
          @retrofit2.http.Body UUID feedId
          );
}
