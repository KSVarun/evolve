package com.example.evol.data


data class TrackerAPIGetResponse(
    val track: Map<String, Map<String, String>>,
    val configurations: Map<String, Map<String, String>>
)

data class TrackerAPIUpdateResponse(
    val message: String
)

enum class ConfigKeys(val key: String) {
    MAX_THRESHOLD_VALUE("max-threshold-value"),
    THRESHOLD_INCREMENT("threshold-increment");

    companion object {
        private val map = entries.associateBy(ConfigKeys::key)
        fun fromString(key: String): ConfigKeys? = map[key]
    }
}