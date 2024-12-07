package com.example.evol.data


data class TrackerAPIResponse(
    val track: Map<String, Map<String, String>>,
    val configurations: Map<String, Map<String, String>>
)

enum class ConfigKeys(val key: String) {
    MAX_THRESHOLD_VALUE("max-threshold-value"),
    THRESHOLD_INCREMENT("threshold-increment");

    companion object {
        private val map = entries.associateBy(ConfigKeys::key)
        fun fromString(key: String): ConfigKeys? = map[key]
    }
}