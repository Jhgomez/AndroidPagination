package com.demo.pagination.api

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


private val logger = HttpLoggingInterceptor().apply {
    setLevel(HttpLoggingInterceptor.Level.BODY)
}
private val baseClient =  OkHttpClient.Builder()
.addInterceptor(logger)
.build();

val moshi = Moshi.Builder()
    .add(LocalDateAdapter)
    .build()

val client = Retrofit.Builder()
    .baseUrl("https://api.themoviedb.org/3/")
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(baseClient)
    .build();

//    https://api.themoviedb.org/3/movie/11?api_key=YOUR-API-KEY
//
//https://api.themoviedb.org/3//tv/popular?language=en-US&page=1