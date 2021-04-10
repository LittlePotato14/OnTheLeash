package com.example.ontheleash.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface OnTheLeashApi {

    @POST("sessions/")
    Call<JwtResponse> login(
            @Body RegistrationLoginRequest registrationLoginRequest
    );

    @DELETE("sessions/")
    Call<Void> logout(
            @Header("jwt") String jwt
    );

    @POST("users/")
    Call<JwtResponse> registration(
            @Body RegistrationLoginRequest registrationLoginRequest
    );

    @PUT("confirmation_code/")
    Call<JwtResponse> confirm(
            @Header("jwt") String jwt,
            @Body ConfirmationRequest confirmationRequest
    );

    @POST("confirmation_code/")
    Call<Void> newCode(
            @Header("jwt") String jwt
    );

    @PUT("users/new_password")
    Call<Void> forgotPassword(
            @Body ForgotPasswordRequest forgotPasswordRequest
    );
}
