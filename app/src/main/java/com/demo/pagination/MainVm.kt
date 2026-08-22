package com.demo.pagination

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.demo.pagination.api.TvShow
import com.demo.pagination.api.tmdbService
import com.demo.pagination.db.TvShowQuery
import com.demo.pagination.db.getAppDatabase
import com.demo.pagination.feature.NetworkPagingMediator
import com.demo.pagination.feature.NetworkPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.collections.addAll

class MainVm(
    application: Application,
    private val firstVisibleItemProducer: () -> Int,
    private val visibleItemsCountProducer: (Int) -> Unit
): AndroidViewModel(application) {
    val service = tmdbService
    val db = getAppDatabase(getApplication<Application>().applicationContext)

    val shows = mutableStateListOf<TvShow>()

    fun getNextPage() {
        viewModelScope.launch {
            service.getPopularShows(page = 1).body()?.results?.also {
                shows.addAll(it)
            }
        }
    }

    private var source = NetworkPagingSource(service)

    // see here https://developer.android.com/jetpack/androidx/releases/paging#3.5.0-alpha01
    // we have more flow options now, my current transitive dep is lower but I could use it
    @OptIn(ExperimentalPagingApi::class)
    val tvShowsPagingFlow: Flow<PagingData<TvShowQuery>> = Pager(
        // Configure how data is loaded by passing additional properties to
        // PagingConfig, such as pageSize and enabling or disabling placeholders.
        config = PagingConfig(
            pageSize = 20, // TMBD default page size is 20 and that cant be change, so we better match it up here
            enablePlaceholders = true,
            prefetchDistance = 1,
            initialLoadSize = 1,
        ),
        pagingSourceFactory = {
//            source = NetworkPagingSource(service)
//            source
            db.tvShowDao().pagingSource()
        },
        remoteMediator = NetworkPagingMediator(
            showsDb = db,
            tmdbService = service,
            firstVisibleItemProducer = firstVisibleItemProducer,
            jumpToPrevIndex = visibleItemsCountProducer
        )
    )
        .flow
        .cachedIn(viewModelScope)

    fun invalidate() {
        // source.invalidate() // should not call invalidate according to
        // LazyPagingItems.refresh() documentation, we shouldn't call this from this layer, here we
        // should call that method instead (LazyPagingItems.refresh()), and in the repository layer
        // we should call PagingSource.Invalidate()
    }
}