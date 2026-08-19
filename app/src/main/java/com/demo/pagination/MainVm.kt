package com.demo.pagination

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.demo.pagination.api.TvShow
import com.demo.pagination.api.tmdbService
import com.demo.pagination.feature.NetworkPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.collections.addAll

class MainVm: ViewModel() {
    val service = tmdbService

    val shows = mutableStateListOf<TvShow>()

    fun getNextPage() {
        viewModelScope.launch {
            service.getPopularShows(page = 1).body()?.results?.also {
                shows.addAll(it)
            }
        }
    }

    // see here https://developer.android.com/jetpack/androidx/releases/paging#3.5.0-alpha01
    // we have more flow options now, my current transitive dep is lower but I could use it
    val tvShowsPagingFlow: Flow<PagingData<TvShow>> = Pager(
        // Configure how data is loaded by passing additional properties to
        // PagingConfig, such as pageSize and enabling or disabling placeholders.
        config = PagingConfig(
            pageSize = 10, // TMBD defualt page size is 20 and that cant be change, this setting doesn't do anything n this case
            enablePlaceholders = true,
            prefetchDistance = 1,
            initialLoadSize = 1
        ),
        pagingSourceFactory = {
            NetworkPagingSource(service)
        }
    )
        .flow
        .cachedIn(viewModelScope)
}