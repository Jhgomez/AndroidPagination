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
    // would be LoadResult.Page<Page, TvShow>, these pages is what we have produced in the load method
    // remember they contain the next and prev key/index which is what we use to determine the actual
    // current page
    override fun getRefreshKey(state: PagingState<Page, TvShow>): Page? {
        // This function is called when data is invalidated, invalidation could happen on user request(refresh)
        // or like in this class/example, if we have a mechanisms from the backend that notifies us if the
        // data has changed, for example an item was inserted, deleted or modified. Invalidate happens
        // when this.invalidate() method is called, when this happens we need to find the closest page
        // where the user was at, meaning this method basically returns the index of the page the user
        // was at when invalidation happens so the load method can retrieve the key/index and refetch the
        // new data, btw this PagingSource is passed in a factory to the Pager object, whenever we invalidate it,
        // the factory triggers again and therefore returns a new instance of this paging source with the
        // updated PagingState, this happens because these objects are immutable, as stated below, in
        // the PagingSource deffinition.

        // *  An instance of a [PagingSource] is used to load pages of data for an instance of [PagingData].
        // *
        // * A [PagingData] can grow as it loads more data, but the data loaded cannot be updated. If the
        // * underlying data set is modified, a new [PagingSource] / [PagingData] pair must be created to
        // * represent an updated snapshot of the data.

        // BTW PagingData is what the pager produces using this source, and is what the UI receives
        // through a flow


        // anchorPosition is the Most recently accessed index in the list, including placeholders, its
        // null if no access in the PagingData has been made yet. E.g., if this snapshot was generated
        // before or during the first load.
        val page = state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)


            // if prevKey == null -> anchorPage is the first page.
            anchorPage?.prevKey?.let { prevPage ->
                Page(number = prevPage.number + 1 )
            }
            // if nextKey == null -> anchorPage is the last page.
                ?: anchorPage?.nextKey?.let { nextPage ->
                    Page(number = nextPage.number - 1)
                }

            // if both prevKey and nextKey are null our load should use the default key provided
            // by th elvis operator
        }

        return page
    }
}
