package com.borzg.nearbyrestaurants.database

import SearchResponseQuery
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business")
data class BusinessEntity(
    @PrimaryKey val id: String,
    val __typename: String?,
    val name: String?,
    val review_count: Int?,
    val photo: String?
)