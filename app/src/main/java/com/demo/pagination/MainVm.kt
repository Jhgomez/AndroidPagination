package com.demo.pagination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.pagination.api.TvShow
import com.demo.pagination.api.tmdbService
import kotlinx.coroutines.launch

class MainVm: ViewModel() {
    val service = tmdbService

    val shows = mutableListOf<TvShow>()

    fun getNextPage() {
        viewModelScope.launch {
            service.listRepos(page = 1).body()?.results?.also {
                shows.addAll(it)
            }
        }
    }
}