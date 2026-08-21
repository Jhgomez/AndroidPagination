package com.demo.pagination.feature

import android.net.http.HttpException
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room3.withWriteTransaction
import com.demo.pagination.api.TmdbService
import com.demo.pagination.db.AppDatabase
import com.demo.pagination.db.PageIndexesDao
import com.demo.pagination.db.PageIndexesEntity
import com.demo.pagination.db.TvShowQuery
import com.demo.pagination.db.toTvShowQuery
import java.io.IOException


/**
 * A mediator is in charge of fetching and saving data from a remote source
 */
@OptIn(ExperimentalPagingApi::class)
class NetworkPagingMediator(
    private val query: String,
    private val showsDb: AppDatabase,
    private val tmdbService: TmdbService
) : RemoteMediator<Page, TvShowQuery>() {
    private val tvShowDao = showsDb.tvShowDao()
    private val pageIndexesDao: PageIndexesDao = showsDb.pageIndexesDao()


    private val HIGHEST_INDEXES_ID = "HIGHEST_INDEX"
    private val LOWEST_INDEXES_ID = "LOWEST_INDEX"

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Page, TvShowQuery>
    ): MediatorResult {
        state.config.maxSize
        return try {
            // The network load method takes an optional after=<user.id>
            // parameter. For every page after the first, pass the last user
            // ID to let it continue from where it left off. For REFRESH,
            // pass null to load the first page.
            val index = when (loadType) {
                LoadType.REFRESH -> {
                    -1
                }
                // In this example, you never need to prepend, since REFRESH
                // will always load the first page in the list. Immediately
                // return, reporting end of pagination.
                LoadType.PREPEND ->
                    return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val indexInfo: PageIndexesEntity? = pageIndexesDao.select(HIGHEST_INDEXES_ID)

                    // The logic that determines what is the last item could be anything but in our
                    // case it is the index of 500, TMDB returns errors for page/index higher than 500
                    if (indexInfo?.index == 500) {
                        return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    }

                    (indexInfo?.index?: 0) + 1
                }
            }

            // Suspending network load via Retrofit. This doesn't need to be
            // wrapped in a withContext(Dispatcher.IO) { ... } block since
            // Retrofit's Coroutine CallAdapter dispatches on a worker
            // thread.
            val response = tmdbService.getPopularShows(page = index)

            showsDb.withWriteTransaction<Unit> {

                when (loadType) {
//                    userDao.deleteShowsWrapper()
                    LoadType.REFRESH -> TODO()
                    LoadType.PREPEND -> TODO()
                    LoadType.APPEND -> {
                        pageIndexesDao.upsert(
                            PageIndexesEntity(
                                key = HIGHEST_INDEXES_ID,
                                index = index
                            )
                        )

                        response.body()?.results?.also {
                            tvShowDao.insertAll(
                                Array(it.size) { index ->
                                    it[index].toTvShowQuery()
                                }
                            )
                        }
                    }
                }
            }

            MediatorResult.Success(
                endOfPaginationReached = index == 500
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}