package com.example.trabajofinalobjetos15_7_2019;

import com.example.trabajofinalobjetos15_7_2019.DTOs.ImageDTO;
import com.example.trabajofinalobjetos15_7_2019.DTOs.LocationDTO;
import com.example.trabajofinalobjetos15_7_2019.DTOs.UserDTO;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface Api_Interface {

    @POST("/api/User/SignIn")
    Call<UserDTO> signIn(@Body UserDTO user);

    @GET("/tesis-api/api/app/health")
    Call<String> health();
    
    @POST("/api/user/login")
    Call<UserDTO> login(@Body UserDTO user);

    @POST("/api/Location/")
    Call<LocationDTO> addLocation(@Body LocationDTO location, @Header("Authorization") String token);

    @POST("/api/Location/LocationsInArea")
    Call<List<LocationDTO>> getLocationsInArea(@Body LocationDTO location, @Header("Authorization") String token);

    @GET("api/Location/")
    Call<List<LocationDTO>> getLocations(@Header("Authorization") String token );

    @DELETE("api/Location/{location}")
    Call<String> deleteLocation(@Path("location") int locationId, @Header("Authorization") String token);

    @PUT("api/Location/{location}")
    Call<String> updateLocation(@Path("location") int locationId, @Header("Authorization") String token , @Body LocationDTO location);

    @POST("/api/Location/image")
    Call<ImageDTO> addImage(@Body ImageDTO image, @Header("Authorization") String token);

    @GET("/api/Location/image/{imageId}")
    Call<ImageDTO> getImage(@Path("imageId") int imageId, @Header("Authorization") String token );

}
