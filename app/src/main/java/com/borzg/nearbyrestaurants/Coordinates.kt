package com.borzg.nearbyrestaurants

import com.apollographql.apollo.api.Input

/**
 * Simple class for handling location state
 */
data class Coordinates(val latitude: Double, val longitude: Double) {

    fun getLatitudeInput() = Input.fromNullable(latitude)

    fun getLongitudeInput() = Input.fromNullable(longitude)
}