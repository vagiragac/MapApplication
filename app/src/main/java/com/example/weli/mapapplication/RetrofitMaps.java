package com.example.weli.mapapplication;

import com.example.weli.mapapplication.POJO.Example;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

public interface RetrofitMaps {

    @GET("api/directions/json?key=AIzaSyC5GNXanzH1sPiVBZlovUL_lZm90si-cwg")
    Call<Example> getDistanceDuration(@Query("units") String units, @Query("origin") String origin, @Query("destination") String destination, @Query("mode") String mode);
}
