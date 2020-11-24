package com.borzg.nearbyrestaurants

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.borzg.nearbyrestaurants.database.BusinessDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class MainActivityViewModel : ViewModel() {

    var businessDao: BusinessDao? = null

    private val _coordinateLiveData = MutableLiveData<Coordinates>()
    val coordinateLiveData: LiveData<Coordinates>
        get() = _coordinateLiveData

    fun newCoordinates(coordinates: Coordinates) {
        _coordinateLiveData.postValue(coordinates)
    }

    fun searchNearbyBusinesses(isConnected: Boolean): Flow<PagingData<SearchResponseQuery.Business>> {
        if (!isConnected) return Pager(
            config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false
           ),
            pagingSourceFactory = { businessDao!!.getAllBusinesses() }).flow.map {
                it.map { entity ->
                    entity.getSearchResult()
                }
        }
        if (coordinateLiveData.value == null) return flowOf()
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { SearchSource(coordinateLiveData.value!!, businessDao!!) }
        ).flow.cachedIn(viewModelScope)
    }
}