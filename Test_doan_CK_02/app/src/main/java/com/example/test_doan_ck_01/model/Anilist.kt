package com.example.test_doan_ck_01.model

data class Anilist(

    val id: Int,
    val idMal: Int,
    val title: Title,
    val synonyms: List<String>,
    val isAdult: Boolean
)
