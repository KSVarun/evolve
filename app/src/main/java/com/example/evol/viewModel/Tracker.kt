package com.example.evol.viewModel

import com.example.evol.database.AppDatabase
import android.app.Application
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import com.example.evol.entity.Tracker
import com.example.evol.service.ApiClient
import com.example.evol.service.UpdateTrackerRequestBody
import com.example.evol.utils.getCurrentDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.internal.toImmutableList
import java.io.IOException


@RequiresApi(Build.VERSION_CODES.O)
class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "evolve_database"
    ).fallbackToDestructiveMigration().build()

    private val dao = db.trackerDao()
    val trackerData = mutableStateListOf<Tracker>()

    init {
        loadTrackersFromApi()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadTrackersFromApi() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val apiResponse = ApiClient.getTrackerApiService.fetchTrackers()
                val currentDate = getCurrentDate()
                val currentDatesData = apiResponse.track[currentDate]
                if(currentDatesData != null) {
                    trackerData.clear()
                    dao.deleteAll()
                    var convertedData = convertMapToList(currentDatesData)
                    convertedData = convertedData.toMutableList()
                    val trackerItems = apiResponse.configurations.keys
                    trackerItems.forEach{item->
                            if(currentDatesData[item]==null){
                                convertedData.add(Tracker(item=item, value = 0))
                            }
                    }
                    dao.insertAll(convertedData)
                    trackerData.addAll(convertedData)
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                println("error----")
                e.printStackTrace()
            }
        }
    }

    fun updateTrackerDataAPI() {
        viewModelScope.launch {
            try {
                val requestData = mutableListOf(getCurrentDate())
                trackerData.forEach { data ->
                    requestData.add(data.value.toString())
                }
                val response = ApiClient.updateTrackerApiService.updateTrackers(
                    UpdateTrackerRequestBody(values = requestData.toImmutableList()))
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), response.message, Toast.LENGTH_SHORT).show()
                }

            } catch (e: java.lang.Exception) {
                println(e)
            }

        }
    }
    private fun convertMapToList(map: Map<String, String>): List<Tracker> {
        return map.map { entry ->
            Tracker(item = entry.key, value = entry.value.toInt())
        }
    }

    fun incrementValue(index: Int) {
        val data = trackerData[index]
        val updatedData = data.copy(value = data.value?.plus(1))
        viewModelScope.launch {
            dao.update(updatedData)
            trackerData[index] = updatedData
        }
    }

    fun decrementValue(index: Int) {
        val data = trackerData[index]
        val updatedData = data.copy(value = data.value?.minus(1))
        viewModelScope.launch {
            dao.update(updatedData)
            trackerData[index] = updatedData
        }
    }
}
