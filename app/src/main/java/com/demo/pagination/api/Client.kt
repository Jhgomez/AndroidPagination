package com.demo.pagination.api

import retrofit2.Retrofit




val client = Retrofit.Builder()
    .baseUrl("https://api.themoviedb.org/3")
    .build();

//    https://api.themoviedb.org/3/movie/11?api_key=YOUR-API-KEY
//
//https://api.themoviedb.org/3//tv/popular?language=en-US&page=1