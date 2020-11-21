package com.borzg.nearbyrestaurants.utils

import com.borzg.nearbyrestaurants.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

object ApiKeyInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", BuildConfig.API_KEY)
            .build()

        return chain.proceed(request)
    }
}