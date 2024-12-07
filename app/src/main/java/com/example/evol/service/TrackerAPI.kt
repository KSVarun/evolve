package com.example.evol.service

import com.example.evol.data.TrackerAPIResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import okhttp3.logging.HttpLoggingInterceptor



interface TrackerApiService {
    @GET("/sheets")
    suspend fun fetchTrackers(): TrackerAPIResponse
}

object ApiClient {
    private const val BASE_URL = "https://opm1scg391.execute-api.us-east-1.amazonaws.com"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log the body of the response
    }


    // Step 2: Create a custom interceptor to capture and inspect the raw JSON response
    private val rawJsonInterceptor = Interceptor { chain ->
        val response: Response = chain.proceed(chain.request())

        // Step 3: Log the raw JSON response before it's passed to Gson
        val rawJson = response.body?.string() ?: ""
        println("Raw JSON Response: $rawJson") // Inspect the raw JSON here

        // Recreate the response with the raw JSON (so Retrofit can still process it)
        response.newBuilder()
            .body(rawJson.toResponseBody(response.body?.contentType()))
            .build()
    }

    // Step 4: Create an OkHttpClient with the interceptors
    private val client:OkHttpClient = OkHttpClient.Builder()
//        .addInterceptor(loggingInterceptor)
//        .addInterceptor(rawJsonInterceptor)  // Captures raw JSON before deserialization
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: TrackerApiService = retrofit.create(TrackerApiService::class.java)
}