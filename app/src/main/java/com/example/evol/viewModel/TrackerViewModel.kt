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
import com.example.evol.data.TrackerAPIGetResponse
import com.example.evol.entity.Consistency
import com.example.evol.entity.ThresholdIncrement
import com.example.evol.entity.Tracker
import com.example.evol.service.ApiClient
import com.example.evol.service.UpdateTrackerRequestBody
import com.example.evol.utils.getCurrentDate
import com.example.evol.utils.getNextNDate
import com.example.evol.utils.getPreviousNDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.internal.toImmutableList
import java.io.IOException


class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "evolve_database"
    ).fallbackToDestructiveMigration().build()

    private val trackerDAO = db.trackerDAO()
    val trackerData = mutableStateListOf<Tracker>()
    private val configData = mutableStateOf<Map<String, Map<String, String>>?>(null)
    var loading = mutableStateOf(false)
    val consistentData = mutableMapOf<String, Consistency>()
    var selectedDate = mutableStateOf(getCurrentDate())
    var initialAPICallMade = mutableStateOf(false)
    var apiData = mutableStateOf<TrackerAPIGetResponse?>(null)

    init {
        loadTrackersFromApi()
    }

    private fun loadTrackersFromApi() {
        if(!initialAPICallMade.value){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                apiData.value = ApiClient.getTrackerApiService.fetchTrackers()
                val currentDatesData = apiData.value!!.track[selectedDate.value]
                configData.value= apiData.value!!.configurations

                if (currentDatesData != null) {
                    getConsistentData(apiData.value!!, true)
                    trackerData.clear()
                    //TODO: get all specific dates data and update the same, deleting whole db and inserting again is not optimal and scalable
                    trackerDAO.deleteAll()
                    val convertedData = convertTrackerDataMapToList(currentDatesData).toMutableList()
                    if(convertedData.size != apiData.value!!.configurations.keys.size) {
                        apiData.value!!.configurations.keys.forEach { item ->
                            if (currentDatesData[item] == null) {
                                convertedData.add(Tracker(id = null, item = item, value = 0.0))
                            }
                        }
                    }
                    trackerDAO.insertAll(convertedData)
                    trackerData.addAll(convertedData)
                }
                if(currentDatesData==null){
                    getConsistentData(apiData.value!!, false)
                    trackerData.clear()
                    //TODO: get all specific dates data and update the same, deleting whole db and inserting again is not optimal and scalable
                    trackerDAO.deleteAll()
                    val convertedData:MutableList<Tracker> = mutableListOf()
                    apiData.value!!.configurations.keys.forEach { item ->
                            convertedData.add(Tracker(id = null, item = item, value = 0.0))
                    }
                    trackerDAO.insertAll(convertedData)
                    trackerData.addAll(convertedData)
                }
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
                println("error----")
                e.printStackTrace()
            }
        }
            }else{
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val currentDatesData = apiData.value!!.track[selectedDate.value]
                    configData.value = apiData.value!!.configurations

                    if (currentDatesData != null) {
                        getConsistentData(apiData.value!!, true)
                        trackerData.clear()
                        //TODO: get all specific dates data and update the same, deleting whole db and inserting again is not optimal and scalable
                        trackerDAO.deleteAll()
                        val convertedData =
                            convertTrackerDataMapToList(currentDatesData).toMutableList()
                        if (convertedData.size != apiData.value!!.configurations.keys.size) {
                            apiData.value!!.configurations.keys.forEach { item ->
                                if (currentDatesData[item] == null) {
                                    convertedData.add(Tracker(id = null, item = item, value = 0.0))
                                }
                            }
                        }
                        trackerDAO.insertAll(convertedData)
                        trackerData.addAll(convertedData)
                    }
                    if (currentDatesData == null) {
                        getConsistentData(apiData.value!!, false)
                        trackerData.clear()
                        //TODO: get all specific dates data and update the same, deleting whole db and inserting again is not optimal and scalable
                        trackerDAO.deleteAll()
                        val convertedData: MutableList<Tracker> = mutableListOf()
                        apiData.value!!.configurations.keys.forEach { item ->
                            convertedData.add(Tracker(id = null, item = item, value = 0.0))
                        }
                        trackerDAO.insertAll(convertedData)
                        trackerData.addAll(convertedData)
                    }
                }catch (e: Exception) {
                    println("error----")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getConsistentData(apiResponse: TrackerAPIGetResponse, currentDaysData:Boolean){
        var data = mutableListOf<Map<String,String>>()

        apiResponse.track.forEach { (t, u) ->
            data.add(u)

        }
        data = data.reversed().toMutableList()
        if(currentDaysData){data.removeAt(0)}


        data.forEach { u ->
            u.forEach label@{ (t, u) ->
                var currentData = consistentData[t]
                if(currentData==null){
                    currentData = Consistency(consistentSince = 0, brokenSince = 0)
                }
                if(u.isNotEmpty() && u.toDouble()>0){
                    if(currentData.brokenSince == 0) {
                        if (currentData.consistentSince == 0) {
                            consistentData[t] = Consistency(
                                consistentSince = 1,
                                brokenSince = 0
                            )
                        }
                        if (currentData.consistentSince > 0) {
                            consistentData[t] = Consistency(
                                consistentSince = currentData.consistentSince + 1,
                                brokenSince = 0
                            )
                        }
                    }else if(currentData.brokenSince == 1 && currentData.consistentSince > 0){
                        return@label
                    }
                    else{
                        consistentData[t] = Consistency(
                            consistentSince = 1,
                            brokenSince = currentData.brokenSince
                        )
                    }
                }else if(u.isNotEmpty() && u.toDouble()==0.0){
                    if(currentData.consistentSince==0) {
                        if (currentData.brokenSince == 0) {
                            consistentData[t] = Consistency(
                                consistentSince = 0,
                                brokenSince = 1
                            )
                        } else if (currentData.brokenSince > 0) {
                            consistentData[t] = Consistency(
                                consistentSince = 0,
                                brokenSince = currentData.brokenSince + 1
                            )
                        }
                    }else if(currentData.consistentSince == 1 && currentData.brokenSince > 0){
                        return@label
                    }else{
                        consistentData[t] = Consistency(
                            consistentSince = currentData.consistentSince,
                            brokenSince = 1
                        )
                    }
                }
            }
        }
    }


    private fun convertTrackerDataMapToList(map: Map<String, String>): List<Tracker> {
        return map.map { entry ->
            Tracker(
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

    fun updateDate(incrementOrDecrement: String){
        viewModelScope.launch {

            if(incrementOrDecrement=== "increment"){
                selectedDate.value=getNextNDate(selectedDate.value,1)
                            }
            if(incrementOrDecrement=== "decrement"){
                selectedDate.value=getPreviousNDate(selectedDate.value,1)

            }
            consistentData.clear()
            loadTrackersFromApi()
        }
    }

    fun updateTrackerDataAPI() {
        viewModelScope.launch {
            try {
                loading.value=true
                val requestData = mutableListOf(getCurrentDate())
                trackerData.forEach { data ->
                    requestData.add(data.value.toString())
                }
                val response = ApiClient.updateTrackerApiService.updateTrackers(
                    UpdateTrackerRequestBody(values = requestData.toImmutableList())
                )
                loading.value=false
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), response.message, Toast.LENGTH_SHORT).show()
                }

            } catch (e: java.lang.Exception) {
                loading.value=false
                Toast.makeText(getApplication(), "Something happened with API call, please retry", Toast.LENGTH_SHORT).show()
            }

        }
    }

    fun incrementValue(index: Int, item:String) {
        val incrementValue = configData.value?.get(item)?.get(ThresholdIncrement)?.toInt() ?: 1
        val data = trackerData[index]
        val updatedData = data.copy(value = data.value.plus(incrementValue))
        viewModelScope.launch {
            trackerDAO.update(updatedData)
            trackerData[index] = updatedData
        }
    }

    fun decrementValue(index: Int) {
        val data = trackerData[index]
        val updatedData = data.copy(value = data.value.minus(1))
        viewModelScope.launch {
            trackerDAO.update(updatedData)
            trackerData[index] = updatedData
        }
    }
}




