package com.borzg.nearbyrestaurants.utils

import com.apollographql.apollo.ApolloClient
import okhttp3.OkHttpClient

object ApolloService {

    private const val BASE_URL = "https://api.yelp.com/v3/graphql"

    val apolloClient: ApolloClient by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor)
            .build()

        ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(okHttpClient)
            .build()
    }

}