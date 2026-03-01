package com.example.evol.viewModel

import com.example.evol.database.AppDatabase
import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.room.Room
import com.example.evol.data.FoodTrackerAPIGetResponse
import com.example.evol.entity.Consistency
import com.example.evol.entity.FoodTracker
import com.example.evol.entity.MaxThresholdValue
import com.example.evol.entity.ThresholdIncrement
import com.example.evol.service.ApiClient
import com.example.evol.service.UpdateTrackerRequestBody
import com.example.evol.utils.calculateConsistencyData
import com.example.evol.utils.getCurrentDate
import com.example.evol.utils.getNextNDate
import com.example.evol.utils.getPreviousNDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.internal.toImmutableList
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FoodTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "evolve_database"
    ).fallbackToDestructiveMigration().build()

    private val trackerDAO = db.foodTrackerDAO()
    val foodTrackerData = mutableStateListOf<FoodTracker>()
    private val configData = mutableStateOf<Map<String, Map<String, String>>?>(null)
    var updateAPICallIsLoading = mutableStateOf(false)
    val consistentData = mutableMapOf<String, Consistency>()
    var selectedDate = mutableStateOf(getCurrentDate())
    private var initialAPICallMade = mutableStateOf(false)
    private var apiData = mutableStateOf<FoodTrackerAPIGetResponse?>(null)
    val dataFetchIsLoading = mutableStateOf(false)

    init {
        loadTrackersFromApi()
    }
    

    private fun processAPIResponse() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentDatesData =  apiData.value!!.track[selectedDate.value]
                configData.value = apiData.value!!.configurations

                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val sortedDates = apiData.value!!.track.keys.sortedWith(compareBy {
                    LocalDate.parse(
                        it,
                        formatter
                    )
                })
                val selectedDateIndex = sortedDates.indexOf(selectedDate.value)
                val filteredTrackData =
                    if (currentDatesData == null) apiData.value!!.track else sortedDates
                        .take(selectedDateIndex + 1)
                        .associateWith { date -> apiData.value!!.track[date] ?: emptyMap() }

                if (currentDatesData != null) {
                    getConsistentData(filteredTrackData, true)
                    foodTrackerData.clear()
                    //TODO: get all specific dates data and update the same, deleting whole db and inserting again is not optimal and scalable
                    trackerDAO.deleteAll()
                    val convertedData =
                        convertTrackerDataMapToList(currentDatesData).toMutableList()
                    if (convertedData.size != apiData.value!!.configurations.keys.size) {
                        apiData.value!!.configurations.keys.forEach { item ->
                            if (currentDatesData[item] == null) {
                                convertedData.add(FoodTracker(id = null, item = item, value = 0.0))
                            }
                        }
                    }
                    trackerDAO.insertAll(convertedData)
                    foodTrackerData.addAll(convertedData)
                }
                if (currentDatesData == null) {
                    getConsistentData(filteredTrackData, false)
                    foodTrackerData.clear()
                    //TODO: get all specific dates data and update the same, deleting whole db and inserting again is not optimal and scalable
                    trackerDAO.deleteAll()
                    val convertedData: MutableList<FoodTracker> = mutableListOf()
                    apiData.value!!.configurations.keys.forEach { item ->
                        convertedData.add(FoodTracker(id = null, item = item, value = 0.0))
                    }
                    trackerDAO.insertAll(convertedData)
                    foodTrackerData.addAll(convertedData)
                }
            } catch (e: Exception) {
                println("error----")
                e.printStackTrace()
            }

        }
    }

    fun forceLoadDataOnPullToRefresh() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataFetchIsLoading.value = true
                apiData.value = ApiClient.getFoodTrackerApiService.fetchTrackers()
                dataFetchIsLoading.value = false
                processAPIResponse()
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "Network error. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                println("error----")
                e.printStackTrace()
            }
        }
    }

    private fun loadTrackersFromApi() {
        if (!initialAPICallMade.value) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    apiData.value = ApiClient.getFoodTrackerApiService.fetchTrackers()
                    processAPIResponse()
                    initialAPICallMade.value = true
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            getApplication(),
                            "Network error. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            getApplication(),
                            "Network error. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    e.printStackTrace()
                }
            }
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                processAPIResponse()
            }
        }
    }

    private fun getConsistentData(
        trackData: Map<String, Map<String, String>>,
        currentDaysData: Boolean
    ) {
        val itemGoals = configData.value?.mapValues { (_, config) ->
            config[MaxThresholdValue]?.toIntOrNull()
        } ?: emptyMap()

        consistentData.clear()
        consistentData.putAll(
            calculateConsistencyData(
                trackData = trackData,
                skipLatestDate = currentDaysData,
                itemGoals = itemGoals
            )
        )
    }


    private fun convertTrackerDataMapToList(map: Map<String, String>): List<FoodTracker> {
        return map.map { entry ->
            FoodTracker(
                id = null,
                item = entry.key,
                value = (if (entry.value.isNotEmpty()) {
                    entry.value.toDouble()
                } else {
                    0.0
                })
            )
        }
    }

    fun updateDate(incrementOrDecrement: String) {
        viewModelScope.launch {

            if (incrementOrDecrement === "increment") {
                selectedDate.value = getNextNDate(selectedDate.value, 1)
            }
            if (incrementOrDecrement === "decrement") {
                selectedDate.value = getPreviousNDate(selectedDate.value, 1)

            }
            consistentData.clear()
            loadTrackersFromApi()
        }
    }

    fun updateTrackerDataAPI() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                updateAPICallIsLoading.value = true
                val requestData = mutableListOf(selectedDate.value)
                foodTrackerData.forEach { data ->
                    requestData.add(data.value.toString())
                }
                val response = ApiClient.updateFoodTrackerApiService.updateTrackers(
                    UpdateTrackerRequestBody(values = requestData.toImmutableList())
                )
                updateAPICallIsLoading.value = false
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), response.message, Toast.LENGTH_SHORT).show()
                }

            } catch (e: java.lang.Exception) {
                updateAPICallIsLoading.value = false
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "Something happened with API call, please retry",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                e.printStackTrace()
            }

        }
    }

    private fun updateApiData(currentFoodTrackerDataList: List<FoodTracker>){
        apiData.value?.let { currentApiData ->
            val newTrackForSelectedDate = currentFoodTrackerDataList.associate { tracker ->
                tracker.item to tracker.value.toString()
            }

            val mutableTrack = currentApiData.track.toMutableMap()
            mutableTrack[selectedDate.value] = newTrackForSelectedDate
            apiData.value = currentApiData.copy(track = mutableTrack.toMap())

            println("Updated apiData for date ${selectedDate.value} using the current trackerData.")
        } ?: println("Warning: apiData.value is null. Cannot update.")
    }

    fun incrementValue(index: Int, item: String) {
        val incrementValue = configData.value?.get(item)?.get(ThresholdIncrement)?.toInt() ?: 1
        val data = foodTrackerData[index]
        val updatedData = data.copy(value = data.value.plus(incrementValue))
        viewModelScope.launch {
            trackerDAO.update(updatedData)
            foodTrackerData[index] = updatedData
            updateApiData(foodTrackerData)
        }
    }

    fun decrementValue(index: Int) {
        val data = foodTrackerData[index]
        val updatedData = data.copy(value = data.value.minus(1))
        viewModelScope.launch {
            trackerDAO.update(updatedData)
            foodTrackerData[index] = updatedData
            updateApiData(foodTrackerData)
        }
    }

    fun decrementValueOnLongPress(index: Int, item:String){
        val decrementBy = configData.value?.get(item)?.get(ThresholdIncrement)?.toInt() ?: 1
        val data = foodTrackerData[index]
        var decrementValue = data.value.minus(decrementBy)
        if(decrementValue < 0){
            decrementValue = 0.0
        }
        val updatedData = data.copy(value = decrementValue)
        viewModelScope.launch {
            trackerDAO.update(updatedData)
            foodTrackerData[index] = updatedData
            updateApiData(foodTrackerData)
        }
    }

    fun incrementValueOnLongPress(index: Int) {
        val data = foodTrackerData[index]
        val updatedData = data.copy(value = data.value.plus(1))
        viewModelScope.launch {
            trackerDAO.update(updatedData)
            foodTrackerData[index] = updatedData
            updateApiData(foodTrackerData)
        }
    }
}


