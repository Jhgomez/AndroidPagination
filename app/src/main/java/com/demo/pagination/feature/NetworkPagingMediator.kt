package com.demo.pagination.feature

import androidx.compose.foundation.lazy.LazyListState
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
import kotlin.math.ceil


/**
 * A mediator is in charge of fetching and saving data from a remote source when the local paging
 * source returned by the the database doesn't have any more records. In this example I'm turning the
 * TMDB page-keyed API into some sort of item-keyed API, I had to "force" it since TMDB can not be
 * fetched using item-key logic, and since I want to support refresh from anywhere in the list but
 * without loosing current lazy list state, and supporting Append and Prepend actions from wherever
 * the list was refreshed
 */
@OptIn(ExperimentalPagingApi::class)
class NetworkPagingMediator(
    private val showsDb: AppDatabase,
    private val tmdbService: TmdbService,
    private val firstVisibleItemProducer: () -> Int,
    private val visibleItemsCountProducer: () -> Int
) : RemoteMediator<Int, TvShowQuery>() {
    private val tvShowDao = showsDb.tvShowDao()
    private val pageIndexesDao: PageIndexesDao = showsDb.pageIndexesDao()

    private val HIGHEST_INDEXES_ID = "HIGHEST_INDEX"
    private val LOWEST_INDEXES_ID = "LOWEST_INDEX"

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TvShowQuery>
    ): MediatorResult {
        val highestIndexInfo: PageIndexesEntity? =
            pageIndexesDao.select(HIGHEST_INDEXES_ID)

        val lowestIndexInfo: PageIndexesEntity? =
            pageIndexesDao.select(LOWEST_INDEXES_ID)

        var firstVisibleItem = -1
        var visibleItemsCount = -1

        return try {
            // The network load method takes an optional after=<user.id>
            // parameter. For every page after the first, pass the last user
            // ID to let it continue from where it left off. For REFRESH,
            // pass null to load the first page.
            val pageIndex = when (loadType) {
                LoadType.REFRESH -> {
                    val visibleItemsPageIndex = ceil(
                        firstVisibleItemProducer() /
                                state.config.pageSize.toFloat()
                    ).toInt()

                    val pagerPageIndex = visibleItemsPageIndex.coerceAtLeast(1)

                    firstVisibleItem = firstVisibleItemProducer() - ((pagerPageIndex - 1) * state.config.pageSize)
                    visibleItemsCount = firstVisibleItemProducer()

                    pagerPageIndex

                    // relying on anchor position in a mediator is not the best approach since
                    // when used in conjunction of functions like PagingState.closestPageToPosition()
                    // return incorrect values here, this is because of the underlying behaviors of
                    // the PageSource that Room creates
//                    state.anchorPosition?.let { anchorPosition ->
//
//                        // if we are here our index info is impossible to be null
//
//                        // also, in our logic either highest and lowest are null at same time
//                        // or they are not, it is impossible to just one be null
//                        if (highestIndexInfo == null && lowestIndexInfo == null) {
//                            1
//                        } else {
//                            // Here our index info is not null
//                            // if prevKey == null -> anchorPage is the first page.
//                            val currentLocalIndex =
//                                ceil(anchorPosition / state.config.pageSize.toFloat()).toInt() - 1
//
//                            lowestIndexInfo!!.index + currentLocalIndex
//                        }
//                    }
                }
                // In this example, you never need to prepend, since REFRESH
                // will always load the first page in the list. Immediately
                // return, reporting end of pagination.
                LoadType.PREPEND ->
                    null
                LoadType.APPEND -> {

                    // The logic that determines what is the last item could be anything but in our
                    // case it is the index of 500, TMDB returns errors for page/index higher than 500
                    if (highestIndexInfo?.index == 500) {
                        return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    }

                    (highestIndexInfo?.index?: 0) + 1
                }
            }

            if (pageIndex != null) {
                // Suspending network load via Retrofit. This doesn't need to be
                // wrapped in a withContext(Dispatcher.IO) { ... } block since
                // Retrofit's Coroutine CallAdapter dispatches on a worker
                // thread.
                val response = tmdbService.getPopularShows(page = pageIndex)

                showsDb.withWriteTransaction<Unit> {

                    when (loadType) {
//                    userDao.deleteShowsWrapper()
                        LoadType.PREPEND -> TODO()
                        LoadType.REFRESH -> {
                            if (highestIndexInfo != null && lowestIndexInfo != null) {
                                // this means data already exists and we need to delete it
                                tvShowDao.deleteAllTvShows()
                                pageIndexesDao.deleteAll()
                            }

                            response.body()?.results?.copyOfRange(firstVisibleItem, state.config.pageSize -1)?.also {
                                val baseIndex = (pageIndex - 1) * state.config.pageSize
                                tvShowDao.insertAll(
                                    Array(it.size) { listIndex ->
                                        it[listIndex].toTvShowQuery(listIndex + baseIndex)
                                    }
                                )
                            }

                            pageIndexesDao.upsert(
                                PageIndexesEntity(
                                    key = HIGHEST_INDEXES_ID,
                                    index = pageIndex
                                )
                            )

                            pageIndexesDao.upsert(
                                PageIndexesEntity(
                                    key = LOWEST_INDEXES_ID,
                                    index = pageIndex
                                )
                            )
                        }
                        LoadType.APPEND -> {

                            response.body()?.results?.also {
                                val baseIndex = (pageIndex - 1) * state.config.pageSize
                                tvShowDao.insertAll(
                                    Array(it.size) { listIndex ->
                                        it[listIndex].toTvShowQuery(listIndex + baseIndex)
                                    }
                                )
                            }

                            pageIndexesDao.upsert(
                                PageIndexesEntity(
                                    key = HIGHEST_INDEXES_ID,
                                    index = pageIndex
                                )
                            )

                            if (lowestIndexInfo == null) {
                                pageIndexesDao.upsert(
                                    PageIndexesEntity(
                                        key = LOWEST_INDEXES_ID,
                                        index = pageIndex
                                    )
                                )
                            }
                        }
                    }
                }
            }

            MediatorResult.Success(
                endOfPaginationReached = pageIndex == 500
            )
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}