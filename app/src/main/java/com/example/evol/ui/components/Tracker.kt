package com.example.evol.ui.components

import android.app.Application
import com.example.evol.database.AppDatabase
import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room.databaseBuilder
import com.example.evol.viewModel.TrackerViewModel
import com.example.evol.viewModelFactory.TrackerViewModelFactory


data class SelectedDatesData(
    val item: String,
    var value: Int
)

@Composable
fun Tracker(context: Context){
    val trackerViewModal: TrackerViewModel = viewModel(factory = TrackerViewModelFactory(context.applicationContext as Application))


//    val selectedDatesData = remember {
//        listOf(
//            SelectedDatesData(item = "Meditation", value = 0),
//            SelectedDatesData(item = "FC", value = 0),
//            SelectedDatesData(item = "Carrot", value = 0)
//        )
//    }

    Column(modifier = Modifier.padding(16.dp)) {
        trackerViewModal.trackerData.forEachIndexed { index,data ->
            Row (  modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically){
                Text(
                    text = "+",
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                        .wrapContentSize(Alignment.Center)
                        .clickable {
                            trackerViewModal.incrementValue(index)
                        }
                )
                data.item?.let {
                    Text(
                        text = it,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = data.value.toString()
                )
                Text(
                    text = "-",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                        .wrapContentSize(Alignment.Center)
                        .clickable(enabled = data.value!! > 0) {
                                trackerViewModal.decrementValue(index)
                        }
                )
            }
        }
    }
}
