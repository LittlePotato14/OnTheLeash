package com.example.ontheleash.api;

import com.example.ontheleash.MyClusteringItem;
import com.example.ontheleash.dataClasses.Dog;
import com.example.ontheleash.dataClasses.User;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface OnTheLeashApi {

    @POST("sessions/")
    Call<JwtResponse> login(
            @Body Credentials credentials
    );

    @DELETE("sessions/")
    Call<Void> logout(
            @Header("jwt") String jwt
    );

    @POST("users/")
    Call<JwtResponse> registration(
            @Body Credentials credentials
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

    @GET("users/")
    Call<User> getCurrentUserInfo(
            @Header("jwt") String jwt
    );

    @GET("users/")
    Call<User> getUserInfo(
            @Header("jwt") String jwt,
            @Query("id") int id
    );

    @GET("breeds/")
    Call<List<String>> getBreeds(
    );

    @Multipart
    @POST("users/update")
    Call<Void> updateUser(
            @Header("jwt") String jwt,
            @Part MultipartBody.Part image,
            @Part("user") User user
    );

    @Multipart
    @POST("dogs/update")
    Call<Void> updateDog(
            @Query("id") int id,
            @Header("jwt") String jwt,
            @Part MultipartBody.Part image,
            @Part("dog") Dog dog
    );

    @Multipart
    @POST("dogs/")
    Call<DogResponse> createDog(
            @Header("jwt") String jwt,
            @Part MultipartBody.Part image,
            @Part("dog") Dog dog
    );

    @DELETE("dogs/")
    Call<Void> deleteDog(
            @Query("id") int id,
            @Header("jwt") String jwt
    );

    @PUT("users/")
    Call<Void> updateEmail(
            @Header("jwt") String jwt,
            @Body Email email
    );

    @PUT("users/")
    Call<Void> updatePassword(
            @Header("jwt") String jwt,
            @Body Password password
    );

    @DELETE("users/")
    Call<Void> deleteUser(
            @Header("jwt") String jwt
    );

    @DELETE("sessions/others")
    Call<Void> deleteOtherSessions(
            @Header("jwt") String jwt
    );

    @GET("markers/")
    Call<List<MyClusteringItem>> getMarkers(
            @Query("latitude_t") double latitude_t,
            @Query("latitude_b") double latitude_b,
            @Query("longitude_l") double longitude_l,
            @Query("longitude_r") double longitude_r,

            @Query("dogs") int dogs,
            @Query("dog_parks") int dog_parks,
            @Query("dog_friendly") int dog_friendly,
            @Query("animal_hospitals") int animal_hospitals,
            @Query("shelters") int shelters,
            @Query("pet_stores") int pet_stores,
            @Query("dog_hotels") int dog_hotels,
            @Query("training_clubs") int training_clubs,
            @Query("breeding") int breeding,

            @Query("male") int male,
            @Query("female") int female,
            @Query("castration") int castration,
            @Query("ready_to_mate") int ready_to_mate,
            @Query("for_sale") int for_sale,
            @Query("birthday") String birthday,
            @Query("breeds") String breeds
    );

    @GET("markers/")
    Call<jsonResponse> getMarker(
            @Query("id") int id,
            @Query("type") int type
    );

    @PUT("users/")
    Call<Void> updateLocation(
            @Header("jwt") String jwt,
            @Body LatitudeLongitude latLong
    );
}
