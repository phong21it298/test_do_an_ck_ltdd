package com.example.test_doan_ck_01.api

import retrofit2.Call
import com.example.test_doan_ck_01.model.AnimeResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("search")
    fun searchAnimeByUrl(
        @Query("cutBorders") cutBorders: Boolean = true,
        @Query("anilistInfo") anilistInfo: Boolean = true,
        @Query("url") imageUrl: String
    ): Call<AnimeResponse>
}