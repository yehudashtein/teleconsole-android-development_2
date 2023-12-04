package com.telebroad.teleconsole.chat.models;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileUploadService {
    @Multipart
    @POST("file/u/?")
    Call<ResponseBody> uploadFile(
            @Header("x-tinode-apikey") String apiKey,
            @Header("x-tinode-auth") String authToken,
            @Part("id") RequestBody id,
            @Part MultipartBody.Part file
    );
}
