package com.example.evol.viewModelFactory

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.evol.viewModel.RemainderViewModel
import com.example.evol.viewModel.TrackerViewModel

class RemainderViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return RemainderViewModel(application) as T
    }
}
