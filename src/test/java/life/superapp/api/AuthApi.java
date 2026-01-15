package life.superapp.api;

import life.superapp.api.model.auth.AuthUserRequest;
import life.superapp.api.model.auth.AuthUserResponse;
import life.superapp.api.model.auth.OttGatewayRequest;
import life.superapp.api.model.auth.OttGatewayResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("auth/gateway")
    @Headers({
            "Content-type: application/json",
            "X-Gateway-Secret: life_test"
    })
    Call<OttGatewayResponse> gateway(@Body OttGatewayRequest body);

    @POST("auth/user")
    @Headers("Content-type: application/json")
    Call<AuthUserResponse> auth(@Body AuthUserRequest body);
}
