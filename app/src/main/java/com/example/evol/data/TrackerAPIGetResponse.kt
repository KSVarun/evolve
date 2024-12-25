package com.example.evol.data


data class TrackerAPIGetResponse(
    val track: Map<String, Map<String, String>>,
    val configurations: Map<String, Map<String, String>>
)

data class TrackerAPIUpdateResponse(
    val message: String
)
