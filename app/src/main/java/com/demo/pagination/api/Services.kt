package com.demo.pagination.api

import com.demo.pagination.BuildConfig

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbService {
    @GET("tv/popular")
    suspend fun listRepos(
        @Query("api_key") apiKey: String = BuildConfig.API_KEY,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int
    ): Response<PaginTmdbResponse> // Response instead of Call object which is used when we need
    // a callback based http request
}

val tmdbService = client.create(TmdbService::class.java)