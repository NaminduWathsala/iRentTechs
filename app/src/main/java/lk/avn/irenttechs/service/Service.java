package lk.avn.irenttechs.service;

import java.util.List;
import java.util.Map;

import lk.avn.irenttechs.dto.LogInDTO;
import lk.avn.irenttechs.dto.ProfileUpdateDTO;
import lk.avn.irenttechs.dto.SignUpDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Service {
    @POST("test")
    Call<List> call();

  @POST("auth/signup")
    Call<Map<String,String>> send(@Body SignUpDTO signup);
  @POST("auth/signin")
    Call<Map<String,String>> sendauth(@Body LogInDTO logInDTO);
  @POST("auth/signup_google")
    Call<Map<String,String>> googleauth(@Body SignUpDTO signup);
  @POST("auth/profile_update")
    Call<Map<String,String>> profileupdate(@Body ProfileUpdateDTO profileUpdate);
  @POST("auth/profile_user_update")
    Call<Map<String,String>> profileUserupdate(@Body ProfileUpdateDTO profileUpdate);

}
