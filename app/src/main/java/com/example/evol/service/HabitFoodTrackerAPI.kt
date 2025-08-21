package com.example.evol.service

import com.example.evol.data.FoodTrackerAPIGetResponse
import com.example.evol.data.FoodTrackerAPIUpdateResponse
import com.example.evol.data.HabitTrackerAPIGetResponse
import com.example.evol.data.HabitTrackerAPIUpdateResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.PUT

data class UpdateTrackerRequestBody(
    val values:List<String>
)

interface GetTrackerApiService {
    @GET("/sheets/body")
    suspend fun fetchTrackers(): HabitTrackerAPIGetResponse
}

interface UpdateTrackerApiService {
    @PUT("/sheets/body")
    suspend fun updateTrackers(@Body body:UpdateTrackerRequestBody): HabitTrackerAPIUpdateResponse
}

interface GetFoodTrackerApiService {
    @GET("/sheets/food")
    suspend fun fetchTrackers(): FoodTrackerAPIGetResponse
}

interface UpdateFoodTrackerApiService {
    @PUT("/sheets/food")
    suspend fun updateTrackers(@Body body:UpdateTrackerRequestBody): FoodTrackerAPIUpdateResponse
}

object ApiClient {
    private const val BASE_URL = "https://opm1scg391.execute-api.us-east-1.amazonaws.com"

//    private val loggingInterceptor = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY
//    }

//    private val rawJsonInterceptor = Interceptor { chain ->
//        val response: Response = chain.proceed(chain.request())
//
//        val rawJson = response.body?.string() ?: ""
//        println("Raw JSON Response: $rawJson")
//
//        response.newBuilder()
//            .body(rawJson.toResponseBody(response.body?.contentType()))
//            .build()
//    }

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
    val getFoodTrackerApiService: GetFoodTrackerApiService = retrofit.create(GetFoodTrackerApiService::class.java)
    val updateFoodTrackerApiService: UpdateFoodTrackerApiService = retrofit.create(UpdateFoodTrackerApiService::class.java)
}