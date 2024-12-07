package com.example.evol.viewModel

import com.example.evol.database.AppDatabase
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import androidx.compose.runtime.mutableStateListOf
import androidx.room.Room
import com.example.evol.models.Tracker


class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "evolve_database"
    ).fallbackToDestructiveMigration().build()

    private val dao = db.trackerDao()

    val trackerData = mutableStateListOf<Tracker>()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val data = dao.getAll()
            println(data)
//            trackerData.clear()
//            trackerData.addAll(data)
            trackerData.addAll(listOf(
            Tracker(item = "Meditation", value = 0, id = 1),
                Tracker(item = "FC", value = 0, id = 2),
                Tracker(item = "Carrot", value = 0, id = 3)
        ))
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
