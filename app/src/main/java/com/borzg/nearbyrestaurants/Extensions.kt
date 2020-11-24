package com.borzg.nearbyrestaurants

import com.borzg.nearbyrestaurants.database.BusinessEntity

fun Double.toMeters(): String {
    return "${toInt()}m"
}

fun SearchResponseQuery.Business.getEntity(): BusinessEntity {
    return BusinessEntity(
        this.id ?: "no id",
        this.__typename,
        this.name,
        this.review_count,
        this.photos?.get(0)
    )
}

fun BusinessEntity.getSearchResult(): SearchResponseQuery.Business {
    return SearchResponseQuery.Business(
        this.__typename ?: "",
        this.id ?: "no id",
        this.name,
        distance = 0.0,
        this.review_count,
        listOf(this.photo)
    )
}