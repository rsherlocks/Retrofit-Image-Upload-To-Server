package com.example.androidshaper.retrofitimageupload;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface OurRetrofitClient {


    @POST("upload.php")
    @FormUrlEncoded
    Call<ObjectClass> getResponse(@Field("name") String name,@Field("image") String image);
}
