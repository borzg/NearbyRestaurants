package com.borzg.nearbyrestaurants

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MainActivityViewModel : ViewModel() {

    private val _coordinateLiveData = MutableLiveData<Coordinates>()
    val coordinateLiveData: LiveData<Coordinates>
        get() = _coordinateLiveData

    fun newCoordinates(coordinates: Coordinates) {
        _coordinateLiveData.postValue(coordinates)
    }

    fun searchNearbyBusinesses(): Flow<PagingData<SearchResponseQuery.Business>> {
        if (coordinateLiveData.value == null) return flowOf()
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { SearchSource(coordinateLiveData.value!!) }
        ).flow.cachedIn(viewModelScope)
    }
}