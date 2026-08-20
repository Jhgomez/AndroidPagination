package com.demo.pagination.db

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import com.demo.pagination.api.TvShow
import com.demo.pagination.feature.Page

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class) // we could annotate DB instead
interface TvShowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllShows(shows: List<TvShowEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllGenres(genres: List<GenreReference>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllCountries(countries: List<OriginCountryReference>)

    @Transaction
    @Query("SELECT * FROM TvShowEntity")
    fun pagingSource(): PagingSource<Int, TvShowQuery>

    @Transaction
    suspend fun insertAllGenres(query: Array<TvShowQuery>) {
        insertAllShows(query.map(TvShowQuery::tvShow))

        query.forEachIndexed { index, query ->
            insertAllGenres(query.genreIds)
            insertAllCountries(query.originCountry)
        }
    }
}

fun TvShow.toTvShowQuery(): TvShowQuery = TvShowQuery(
    tvShow = TvShowEntity(
        id = id,
        adult = adult,
        backdropPath = backdropPath,
        originalLanguage = originalLanguage,
        originalName = originalName,
        overview = overview,
        popularity = popularity,
        posterPath = posterPath,
        firstAirDate = firstAirDate,
        name = name,
        voteAverage = voteAverage,
        voteCount = voteCount
    ),
    genreIds = genreIds.map {
        GenreReference(
            genreId = it,
            showId = id
        )
    },
    originCountry = originCountry.map {
        OriginCountryReference(
            originCountryId = it,
            showId = id
        )
    }
)