package com.demo.pagination.feature

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.demo.pagination.api.PagingTmdbResponse
import com.demo.pagination.api.TmdbService
import com.demo.pagination.api.TvShow

class NetworkPagingSource(
    val service: TmdbService,
    val pageKey: Int
): PagingSource<Int, TvShow>() {
    init {
        // the data source is expected to be immutable
        // invalidate PagingSource if data source
        // has updated
//        service.addDatabaseOnChangedListener {
//            invalidate()
//        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TvShow> {
        try {
            // Start refresh at page 1 if undefined.
            val nextPageNumber = params.key ?: 1
            val response = service.getPopularShows(page = nextPageNumber)
            return LoadResult.Page(
                data = response.body()?.results?.toList() ?: emptyList(),
                prevKey = null, // Only paging forward.
                nextKey = nextPageNumber + 1
            )
        } catch (e: Exception) {
            // Handle errors in this block and return LoadResult.Error for
            // expected errors (such as a network failure).
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, TvShow>): Int? {
        // Try to find the page key of the closest page to anchorPosition from
        // either the prevKey or the nextKey; you need to handle nullability
        // here.
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey are null -> anchorPage is the
        //    initial page, so return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
