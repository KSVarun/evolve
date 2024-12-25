package com.example.evol.ui.components

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evol.viewModel.TrackerViewModel
import com.example.evol.viewModelFactory.TrackerViewModelFactory


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Tracker(context: Context) {
    val trackerViewModal: TrackerViewModel =
        viewModel(factory = TrackerViewModelFactory(context.applicationContext as Application))
    val scrollState = rememberScrollState()

    fun hashCodeToColor(hashCode: Int): Color {
        val red = (hashCode shr 16) and 0xFF
        val green = (hashCode shr 8) and 0xFF
        val blue = hashCode and 0xFF
        return Color(red, green, blue)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            trackerViewModal.trackerData.forEachIndexed { index, data ->
                val isLastItem = index == trackerViewModal.trackerData.lastIndex

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (isLastItem) 75.dp else 5.dp)
                        .clip(RoundedCornerShape(42.dp))
                        .background(color = hashCodeToColor("#474747".toColorInt())),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            trackerViewModal.decrementValue(index)
                        },
                        enabled = data.value!! > 0,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(50.dp)
                            .wrapContentSize(Alignment.Center),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = hashCodeToColor("#4999e9".toColorInt())
                        ),
                    ) {
                        Text(text = "-", fontSize = 25.sp, color = Color.White)
                    }
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        data.item?.let {
                            Text(
                                text = it,
                            )
                        }
                        Text(
                            text = data.value.toString()
                        )
                    }
                    Button(
                        onClick = {
                            trackerViewModal.incrementValue(index, data.item ?: "")
                        },
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(50.dp)
                            .wrapContentSize(Alignment.Center),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = hashCodeToColor("#4999e9".toColorInt())
                        ),
                    ) {
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
            colors = ButtonDefaults.buttonColors(
                containerColor = hashCodeToColor("#1976d2".toColorInt())
            ),
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
