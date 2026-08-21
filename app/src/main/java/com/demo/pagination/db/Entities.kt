package com.demo.pagination.db

import androidx.room3.ColumnInfo
import androidx.room3.ColumnTypeConverter
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Relation
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity
data class TvShowEntity(
    @PrimaryKey 
    val id: Int,
    val adult: Boolean,
    @ColumnInfo("backdrop_path") 
    val backdropPath: String?,
    @ColumnInfo("original_language") 
    val originalLanguage: String,
    @ColumnInfo("original_name") 
    val originalName: String,
    val overview: String,
    val popularity: Float,
    @ColumnInfo("poster_path")
    val posterPath: String?,
    @ColumnInfo("first_air_date") 
    val firstAirDate: LocalDate,
    val name: String,
    @ColumnInfo("vote_average") 
    val voteAverage: Float,
    @ColumnInfo("vote_count") 
    val voteCount: Int
)

@Entity(primaryKeys = ["genreId", "showId"])
data class GenreReference(
    val genreId: Int,
    val showId: Int
)

@Entity(primaryKeys = ["originCountryId", "showId"])
data class OriginCountryReference(
    val originCountryId: String,
    val showId: Int
)


data class TvShowQuery(
    @Embedded val tvShow: TvShowEntity,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["showId"]
    )
    val genreIds: List<GenreReference>,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["showId"]
    )
    val originCountry: List<OriginCountryReference>
)

class DateConverters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @ColumnTypeConverter
    fun fromLocalDate(value: LocalDate): String {
        return value.format(formatter)
    }

    @ColumnTypeConverter
    fun toLocalDate(value: String): LocalDate {
        return value.let {
            LocalDate.parse(it, formatter)
        }
    }
}

@Entity
data class PageIndexesEntity(
    @PrimaryKey
    val key: String,
    @ColumnInfo(defaultValue = "-1")
    val index: Int = -1
)