package com.borzg.nearbyrestaurants

import SearchResponseQuery
import androidx.paging.PagingSource
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.coroutines.await
import com.borzg.nearbyrestaurants.database.AppDatabase
import com.borzg.nearbyrestaurants.database.BusinessDao
import com.borzg.nearbyrestaurants.utils.ApolloService

const val PAGE_SIZE = 20

class SearchSource(private val coordinates: Coordinates, val businessDao: BusinessDao): PagingSource<Int, SearchResponseQuery.Business>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResponseQuery.Business> {
        val offset = params.key ?: START_POSITION
        return try {
            // Results are sorted by review_count
            val response = ApolloService.apolloClient
                .query(SearchResponseQuery(
                    latitude = coordinates.getLatitudeInput(),
                    longitude = coordinates.getLongitudeInput(),
                    offset = Input.optional(offset),
                    sort_by = Input.optional(SORT_BY_REVIEW_COUNT)
                )).await()
            val result = response.data?.search?.business?.filterNotNull() ?: emptyList()
            businessDao.insertAllSearchResults(result)
            LoadResult.Page(
                data = result,
                prevKey = if (offset == START_POSITION) null else offset - PAGE_SIZE,
                nextKey = if (result.isEmpty()) null else offset + PAGE_SIZE
            )
        } catch (throwable: Throwable) {
            return LoadResult.Error(throwable)
        }
    }

    companion object {
        const val START_POSITION = 0
        const val SORT_BY_REVIEW_COUNT = "review_count"
        const val SORT_BY_BEST_MATCH = "best_match"
        const val SORT_BY_RATING = "rating"
        const val SORT_BY_DISTANCE = "distance"
    }
}