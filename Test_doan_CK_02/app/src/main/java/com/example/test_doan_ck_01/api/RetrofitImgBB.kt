package com.example.test_doan_ck_01.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitImgBB {

    private const val BASE_URL_ImgBB = "https://api.imgbb.com/1/"

    val instance_imgBB: ImgBBService by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL_ImgBB)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImgBBService::class.java)
    }
}