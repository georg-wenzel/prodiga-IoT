package uibk.ac.at.prodigaclient.api;

import uibk.ac.at.prodigaclient.dtos.JwtRequestDTO;
import uibk.ac.at.prodigaclient.dtos.JwtResponseDTO;
import uibk.ac.at.prodigaclient.CollectionFormats.*;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AuthControllerApi {
  /**
   * createToken
   * 
   * @param internalId The raspis internal Id
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/register")
  Call<Void> registerUsingPOST(
                    @retrofit2.http.Query(value = "internalId") String internalId
  );


  /**
   * Registers the raspberry pi
   *
   * @param body request (required)
   * @return Call&lt;JwtResponseDTO&gt;
   */
  @Headers({
          "Content-Type:application/json"
  })
  @POST("api/auth")
  Call<JwtResponseDTO> createTokenUsingPOST(
          @retrofit2.http.Body JwtRequestDTO body
  );
}
