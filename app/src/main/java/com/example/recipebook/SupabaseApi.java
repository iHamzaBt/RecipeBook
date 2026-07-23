package com.example.recipebook;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SupabaseApi {
    @POST("storage/v1/object/{bucket}/{path}")
    Call<ResponseBody> uploadImage(
            @Header("Authorization") String authorization,
            @Header("apikey") String apikey,
            @Path("bucket") String bucket,
            @Path(value = "path", encoded = true) String path,
            @Body RequestBody file
    );

    @DELETE("storage/v1/object/{bucket}/{path}")
    Call<ResponseBody> deleteImage(
            @Header("Authorization") String authorization,
            @Header("apikey") String apikey,
            @Path("bucket") String bucket,
            @Path(value = "path", encoded = true) String path
    );
}