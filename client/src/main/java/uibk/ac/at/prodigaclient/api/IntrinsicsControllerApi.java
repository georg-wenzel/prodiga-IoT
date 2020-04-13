package uibk.ac.at.prodigaclient.api;

import uibk.ac.at.prodigaclient.dtos.GenericStringDTO;
import uibk.ac.at.prodigaclient.dtos.InstrincsDTO;
import uibk.ac.at.prodigaclient.CollectionFormats.*;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    @retrofit2.http.Body InstrincsDTO body
  );

}
