package com.example.evol.ui.components

import android.app.Application
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evol.viewModel.TrackerViewModel
import com.example.evol.viewModelFactory.TrackerViewModelFactory


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Tracker(context: Context) {
    val trackerViewModal: TrackerViewModel =
        viewModel(factory = TrackerViewModelFactory(context.applicationContext as Application))
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            trackerViewModal.trackerData.forEachIndexed { index, data ->
                val isLastItem = index == trackerViewModal.trackerData.lastIndex

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (isLastItem) 70.dp else 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            trackerViewModal.decrementValue(index)
                        },
                        enabled = data.value!! > 0,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(50.dp)
                            .wrapContentSize(Alignment.Center),
                        contentPadding = PaddingValues(0.dp)
                    ){
                        Text(text = "-", fontSize = 25.sp, color = Color.White)
                    }
                    data.item?.let {
                        Text(
                            text = it,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = data.value.toString()
                    )

                    Button(
                        onClick = {
                            trackerViewModal.incrementValue(index)
                        },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(50.dp)
                            .wrapContentSize(Alignment.Center),
                        contentPadding = PaddingValues(0.dp)
                    ){
                        Text(text = "+", fontSize = 25.sp, color = Color.White)
                    }
                }
            }
        }

        Button(
            onClick = {
                 trackerViewModal.updateTrackerDataAPI()
            },
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.BottomEnd)
                .zIndex(1f)
                .padding(10.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                modifier = Modifier.size(30.dp)
            )
        }

    }


}
