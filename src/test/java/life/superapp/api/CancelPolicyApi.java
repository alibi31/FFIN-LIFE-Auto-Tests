package life.superapp.api;


import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CancelPolicyApi {

    @DELETE("default/{policyNumber}/alikhan")
    Call<Void> cancelPolicy(
            @Path("policyNumber") String policyNumber,
            @Query("w") String reason
    );
}
