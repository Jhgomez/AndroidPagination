package com.demo.pagination.db

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.demo.pagination.api.TvShow

@Dao
interface TvShowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shows: Array<TvShowEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(genres: Array<GenreReference>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(countries: Array<OriginCountryReference>)

//    @Transaction
//    @Query("SELECT * FROM TvShowEntity")
//    fun pagingSource(): PagingSource<Int, TvShowQuery>

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

        for (i in 0..query.size) {
            insertAll(genresByShowArray[i]!!)
            insertAll(originCountryShowArray[i]!!)
        }
    }
}

