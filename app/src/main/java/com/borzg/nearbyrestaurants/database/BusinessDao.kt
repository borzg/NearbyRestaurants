package com.borzg.nearbyrestaurants.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.borzg.nearbyrestaurants.getEntity

@Dao
abstract class BusinessDao {

    @Insert
    abstract suspend fun insertAll(businesses: List<BusinessEntity>)

    suspend fun insertAllSearchResults(businesses: List<SearchResponseQuery.Business>) {
        insertAll(businesses.map { it.getEntity() })
    }

    @Query("SELECT * FROM business")
    abstract fun getAllBusinesses(): PagingSource<Int, BusinessEntity>

    @Query("SELECT * FROM business WHERE id = :id")
    abstract suspend fun getBusinessById(id: String): BusinessEntity
}