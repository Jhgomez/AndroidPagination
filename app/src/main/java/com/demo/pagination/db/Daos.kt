package com.demo.pagination.db

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Delete
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

    @Transaction
    @Delete
    suspend fun deleteGenres(genres: List<GenreReference>)

    @Transaction
    @Delete
    suspend fun deleteCountries(countries: List<OriginCountryReference>)

    @Transaction
    @Delete
    suspend fun deleteTvShows(shows: List<TvShowEntity>)

    @Transaction
    suspend fun deleteShowsWrapper(shows: List<TvShowQuery>) {
        shows.forEach { show ->
            deleteGenres(show.genreIds)
            deleteCountries(show.originCountry)
        }

        deleteTvShows(shows.map(TvShowQuery::tvShow))
    }

//    @Query("DELETE * FROM TvShow WHERE id = :id")
//    suspend fun deleteShowsWrapperByQuery(shows: List<TvShowQuery>)

    @Query("DELETE * FROM TvShow WHERE id = :id")
    suspend fun deleteShowsWrapperByQuery(shows: List<TvShowQuery>) {
        shows.forEach { show ->
            deleteGenres(show.genreIds)
            deleteCountries(show.originCountry)
        }

        deleteTvShows(shows.map(TvShowQuery::tvShow))
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

@Dao
interface PageIndexesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(pageIndexes: PageIndexesEntity)

    @Query("SELECT * FROM PageIndexesEntity WHERE key = :id")
    fun select(id: String): PageIndexesEntity

    @Query("DELTE * FROM PageIndexesEntity WHERE key = :id")
    fun delete(id: String)
}