package com.demo.pagination.api

import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val moshi = Moshi.Builder()
    .add(LocalDateAdapter)
    .build()

val client = Retrofit.Builder()
    .baseUrl("https://api.themoviedb.org/3")
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build();

//    https://api.themoviedb.org/3/movie/11?api_key=YOUR-API-KEY
//
//https://api.themoviedb.org/3//tv/popular?language=en-US&page=1