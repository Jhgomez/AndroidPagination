package com.demo.pagination.feature

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.demo.pagination.api.TmdbService
import com.demo.pagination.api.TvShow

class NetworkPagingSource(
    val service: TmdbService
): PagingSource<Page, TvShow>() {
    init {
        // the data source is expected to be immutable
        // invalidate PagingSource if data source
        // has updated
//        service.addDatabaseOnChangedListener {
//            invalidate()
//        }
    }

    override suspend fun load(params: LoadParams<Page>): LoadResult<Page, TvShow> {
        try {
            // Start refresh at page 1 if undefined.
            val nextPageNumber = params.key ?: Page(number = 1)
            val response = service.getPopularShows(page = nextPageNumber.number)
            return LoadResult.Page(
                data = response.body()?.results?.toList() ?: emptyList(),
                prevKey = null, // Only paging forward.
                nextKey = Page(number = nextPageNumber.number + 1)
            )
        } catch (e: Exception) {
            // Handle errors in this block and return LoadResult.Error for
            // expected errors (such as a network failure).
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Page, TvShow>): Page? {
        // Try to find the page key of the closest page to anchorPosition from
        // either the prevKey or the nextKey; you need to handle nullability
        // here.
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey are null -> anchorPage is the
        //    initial page, so return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)


            anchorPage?.prevKey?.let { prevPage ->
                Page(number = prevPage.number + 1 )
            }
                ?: anchorPage?.nextKey?.let { nextPage ->
                    Page(number = nextPage.number - 1)
                }
        }
    }
}
