package com.example.evol.viewModel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.evol.database.AppDatabase
import com.example.evol.entity.Remainder
import kotlinx.coroutines.launch
import java.util.UUID

class RemainderViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "evolve_database"
    ).fallbackToDestructiveMigration().build()

    private val remainderDAO = db.remainderDAO()
    val remainderData: LiveData<List<Remainder>> = remainderDAO.getAll()

    fun insertRemainderToDB(remainder: Remainder){
        viewModelScope.launch {
            remainderDAO.insert(remainder = remainder)
        }
    }

    fun deleteRemainderFromDB(id: UUID){
        viewModelScope.launch {
            remainderDAO.deleteById(id)
        }
    }

    fun updateRemainderByIDFromDB(id:UUID, title:String, description:String, time:Long, workerId:UUID){
        viewModelScope.launch {
            remainderDAO.updateRemainderById(id, title, description,time, workerId)
        }
    }

}