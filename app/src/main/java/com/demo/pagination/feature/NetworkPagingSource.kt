package com.demo.pagination.feature

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.demo.pagination.api.TmdbService
import com.demo.pagination.api.TvShow

/**
 * Defines the type of data a PaginData returns, PaginData is what a Pager object returns, it is
 * turned into a flow and is collected from the UI layer
 */
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

            val shows = response.body()?.results?.toList() ?: emptyList()

            return LoadResult.Page(
                data = shows,
                // enables back nav when data is invalidated, in this example 1 is the lowest page number
                prevKey = if (nextPageNumber.number > 1)
                    Page(nextPageNumber.number - 1)
                else
                        null,
                // usually an empty list means the last page so we return null
                nextKey = if (shows.isNotEmpty())
                    Page(number = nextPageNumber.number + 1)
                else
                    null
            )

        } catch (e: Exception) {
            // Handle errors in this block and return LoadResult.Error for
            // expected errors (such as a network failure).
            return LoadResult.Error(e)
        }
    }


    // PagingState has a list of Pages, each page would know its key and its type, in this case it
    // would be LoadResult.Page<Page, TvShow>
    override fun getRefreshKey(state: PagingState<Page, TvShow>): Page? {
        // This function is called when data is invalidated, invalidation could happen on user request(refresh)
        // or like in this class/example, if we have a mechanisms from the backend that notifies us if the
        // data has changed, for example an item was inserted, deleted or modified, then we either wait
        // for the backend to notifies us back or if feasible find the page key of the closest page to anchorPosition from
        // either the prevKey or the nextKey; you need to handle nullability
        // here.
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey are null -> anchorPage is the
        //    initial page, so return null.

        // anchorPosition is the Most recently accessed index in the list, including placeholders, its
        // null if no access in the PagingData has been made yet. E.g., if this snapshot was generated
        // before or during the first load.
        val page = state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)


            anchorPage?.prevKey?.let { prevPage ->
                Page(number = prevPage.number + 1 )
            }
                ?: anchorPage?.nextKey?.let { nextPage ->
                    Page(number = nextPage.number - 1)
                }
        }

        return page
    }
}
