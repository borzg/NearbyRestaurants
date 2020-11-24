package com.borzg.nearbyrestaurants

import BusinessDetailsQuery
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.coroutines.await
import com.borzg.nearbyrestaurants.database.BusinessDao
import com.borzg.nearbyrestaurants.utils.ApolloService
import kotlinx.coroutines.launch

class DetailBusinessViewModel: ViewModel() {

    private val _business = MutableLiveData<BusinessDetailsQuery.Business>()
    val business: LiveData<BusinessDetailsQuery.Business>
        get() = _business

    var businessDao: BusinessDao? = null

    fun getBusinessDetails(isConnected: Boolean, id: String) {
        viewModelScope.launch {
            if (isConnected) {
                val response = ApolloService.apolloClient
                    .query(BusinessDetailsQuery(
                        id = Input.fromNullable(id)
                    )).await()
                val result = response.data?.business
                if (result != null) _business.postValue(result)
            }
            else {
                val res = businessDao?.getBusinessById(id)
                if (res != null)
                    _business.postValue(BusinessDetailsQuery.Business(
                        id = res.id,
                        name = res.name,
                        distance = 0.0,
                        review_count = res.review_count,
                        photos = listOf(res.photo),
                        phone = "",
                        is_closed = false,
                        rating = null
                    ))
            }
        }

    }
}