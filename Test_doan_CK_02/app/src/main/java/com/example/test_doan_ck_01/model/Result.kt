package com.example.test_doan_ck_01.model

data class Result(

    val anilist: Anilist,
    val filename: String,
    val episode: Int?,
    val from: Double,
    val to: Double,
    val similarity: Double,
    val video: String,
    val image: String
)
