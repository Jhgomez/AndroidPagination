package com.demo.pagination.db

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.time.LocalDate

@Entity
data class TvShow(
    @PrimaryKey 
    val id: Int,
    val adult: Boolean,
    @ColumnInfo("backdrop_path") 
    val backdropPath: String,
    @ColumnInfo("genre_ids") 
    val genreIds: Array<Int>,
    @ColumnInfo("origin_country") 
    val originCountry: Array<String>,
    @ColumnInfo("original_language") 
    val originalLanguage: String,
    @ColumnInfo("original_name") 
    val originalName: String,
    val overview: String,
    val popularity: Float,
    @ColumnInfo("poster_path") 
    val posterPath: String,
    @ColumnInfo("first_air_date") 
    val firstAirDate: LocalDate,
    val name: String,
    @ColumnInfo("vote_average") 
    val voteAverage: Float,
    @ColumnInfo("vote_count") 
    val voteCount: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TvShow

        if (adult != other.adult) return false
        if (id != other.id) return false
        if (popularity != other.popularity) return false
        if (voteAverage != other.voteAverage) return false
        if (voteCount != other.voteCount) return false
        if (backdropPath != other.backdropPath) return false
        if (!genreIds.contentEquals(other.genreIds)) return false
        if (!originCountry.contentEquals(other.originCountry)) return false
        if (originalLanguage != other.originalLanguage) return false
        if (originalName != other.originalName) return false
        if (overview != other.overview) return false
        if (posterPath != other.posterPath) return false
        if (firstAirDate != other.firstAirDate) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = adult.hashCode()
        result = 31 * result + id
        result = 31 * result + popularity.hashCode()
        result = 31 * result + voteAverage.hashCode()
        result = 31 * result + voteCount
        result = 31 * result + backdropPath.hashCode()
        result = 31 * result + genreIds.contentHashCode()
        result = 31 * result + originCountry.contentHashCode()
        result = 31 * result + originalLanguage.hashCode()
        result = 31 * result + originalName.hashCode()
        result = 31 * result + overview.hashCode()
        result = 31 * result + posterPath.hashCode()
        result = 31 * result + firstAirDate.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}
