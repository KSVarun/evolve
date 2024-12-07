package com.example.evol.service

import com.example.evol.data.TrackerAPIGetResponse
import com.example.evol.data.TrackerAPIUpdateResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.Body
import retrofit2.http.PUT

data class UpdateTrackerRequestBody(
    val values:List<String>
)

interface GetTrackerApiService {
    @GET("/sheets")
    suspend fun fetchTrackers(): TrackerAPIGetResponse
}

interface UpdateTrackerApiService {
    @PUT("/sheets")
    suspend fun updateTrackers(@Body body:UpdateTrackerRequestBody): TrackerAPIUpdateResponse
}

object ApiClient {
    private const val BASE_URL = "https://opm1scg391.execute-api.us-east-1.amazonaws.com"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val rawJsonInterceptor = Interceptor { chain ->
        val response: Response = chain.proceed(chain.request())

        val rawJson = response.body?.string() ?: ""
        println("Raw JSON Response: $rawJson")

        response.newBuilder()
            .body(rawJson.toResponseBody(response.body?.contentType()))
            .build()
    }

    private val client:OkHttpClient = OkHttpClient.Builder()
//        .addInterceptor(loggingInterceptor)
//        .addInterceptor(rawJsonInterceptor)  // Captures raw JSON before deserialization
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val getTrackerApiService: GetTrackerApiService = retrofit.create(GetTrackerApiService::class.java)
    val updateTrackerApiService: UpdateTrackerApiService = retrofit.create(UpdateTrackerApiService::class.java)
}