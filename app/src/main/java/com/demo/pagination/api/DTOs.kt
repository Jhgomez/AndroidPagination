package com.demo.pagination.api

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@JsonClass(generateAdapter = true)
data class PagingTmdbResponse(
    val page: Int,
    val results: Array<TvShow>,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "total_results") val totalResults: Int
)

@JsonClass(generateAdapter = true)
data class TvShow(
    @Json(name = "") val adult: Boolean,
    @Json(name = "backdrop_path") val backdropPath: String,
    @Json(name = "genre_ids") val genreIds: Array<Int>,
    @Json(name = "id") val id: Int,
    @Json(name = "origin_country") val originCountry: Array<String>,
    @Json(name = "original_language") val originalLanguage: String,
    @Json(name = "original_name") val originalName: String,
    @Json(name = "overview") val overview: String,
    @Json(name = "popularity") val popularity: Double,
    @Json(name = "poster_path") val posterPath: String,
    @Json(name = "first_air_date") val firstAirDate: LocalDate,
    @Json(name = "name") val name: String,
    @Json(name = "vote_average") val voteAverage: Float,
    @Json(name = "vote_count") val voteCount: Int
)

object LocalDateAdapter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @ToJson
    fun toJson(value: LocalDate): String {
        return value.format(formatter)
    }

    @FromJson
    fun fromJson(value: String): LocalDate {
        return LocalDate.parse(value, formatter)
    }
}