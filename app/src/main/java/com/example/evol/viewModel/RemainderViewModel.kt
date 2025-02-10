package com.example.evol.viewModel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.evol.database.AppDatabase
import com.example.evol.entity.Remainder
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class RemainderViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "evolve_database"
    ).fallbackToDestructiveMigration().build()

    private val remainderDAO = db.remainderDAO()
    val remainderData = mutableStateListOf<Remainder>()

    init {
        loadRemainderDataFromDB()
    }

    private fun loadRemainderDataFromDB(){
        viewModelScope.launch {
            remainderData.addAll(remainderDAO.getAll())
        }
    }

}