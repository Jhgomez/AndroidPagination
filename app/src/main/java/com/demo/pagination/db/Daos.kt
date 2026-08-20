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

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class) // we could annotate DB instead
interface TvShowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shows: Array<TvShowEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(genres: Array<GenreReference>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(countries: Array<OriginCountryReference>)

//    @Transaction
    @Query("SELECT * FROM TvShowEntity")
    fun pagingSource(): PagingSource<Int, TvShowEntity>

    @Transaction
    suspend fun insertAll(query: Array<TvShowQuery>) {
        val showArray = arrayOfNulls<TvShowEntity>(query.size)
        val genresByShowArray = arrayOfNulls<Array<GenreReference>>(query.size)
        val originCountryShowArray = arrayOfNulls<Array<OriginCountryReference>>(query.size)

        query.forEachIndexed { index, query ->
            showArray[index] = query.tvShow
            genresByShowArray[index] = query.genreIds
            originCountryShowArray[index] = query.originCountry
        }

        insertAll(showArray as Array<TvShowEntity>)

        for (i in 0..<query.size) {
            insertAll(genresByShowArray[i]!!)
            insertAll(originCountryShowArray[i]!!)
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
    genreIds = Array(genreIds.size) { index ->
        GenreReference(
            genreId = genreIds[index],
            showId = id
        )
    },
    originCountry = Array(originCountry.size) { index ->
        OriginCountryReference(
            originCountryId = originCountry[index],
            showId = id
        )
    }
)