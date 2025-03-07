package com.example.test_doan_ck_01.api

import com.example.test_doan_ck_01.model.ImgBBResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.Call

interface ImgBBService {

    @Multipart
    @POST("upload")
    fun uploadImage(
        @Part("key") apiKey: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<ImgBBResponse>
}