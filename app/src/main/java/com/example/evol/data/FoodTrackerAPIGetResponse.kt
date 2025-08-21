package com.example.evol.data


data class FoodTrackerAPIGetResponse(
    val track: Map<String, Map<String, String>>,
    val configurations: Map<String, Map<String, String>>
)

data class FoodTrackerAPIUpdateResponse(
    val message: String
)
