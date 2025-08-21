package com.example.evol.data


data class HabitTrackerAPIGetResponse(
    val track: Map<String, Map<String, String>>,
    val configurations: Map<String, Map<String, String>>
)

data class HabitTrackerAPIUpdateResponse(
    val message: String
)
