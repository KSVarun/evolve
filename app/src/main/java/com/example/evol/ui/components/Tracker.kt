package com.example.evol.ui.components

import android.app.Application
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evol.entity.Consistency
import com.example.evol.utils.hashCodeToColor
import com.example.evol.utils.isSelectedDateLessThanCurrentData
import com.example.evol.viewModel.TrackerViewModel
import com.example.evol.viewModelFactory.TrackerViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tracker(context: Context) {
    val trackerViewModal: TrackerViewModel =
        viewModel(factory = TrackerViewModelFactory(context.applicationContext as Application))
    val scrollState = rememberScrollState()
    var lastButtonClicked by remember { mutableStateOf(false) }
    var shouldCallLastAction by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    println(trackerViewModal.dataFetchIsLoading.value)

    fun debouncedSaveAPICall() {
        if (!trackerViewModal.updateAPICallIsLoading.value) {
            trackerViewModal.updateTrackerDataAPI()
        } else {
            shouldCallLastAction = true
        }
    }

    LaunchedEffect(lastButtonClicked) {
        if (lastButtonClicked) {
            delay(1500)
            if (isActive) {
                debouncedSaveAPICall()
            }
            lastButtonClicked = false
        }
    }

    LaunchedEffect(trackerViewModal.updateAPICallIsLoading.value) {
        if (!trackerViewModal.updateAPICallIsLoading.value && shouldCallLastAction) {
            trackerViewModal.updateTrackerDataAPI()
            shouldCallLastAction = false
        }
    }


    //streak data, how long a task is done consistently and how long it's not done consistently
    fun renderConsistentData(data: Consistency?): MutableList<String> {
        val returnData = mutableListOf<String>()
        if (data == null) {
            returnData.add(0, "0")
            returnData.add(1, "0")
            return returnData
        }
        if (data.consistentSince == 1 && data.brokenSince == 1) {
            returnData.add(0, "1")
            returnData.add(1, "1")
        } else if (data.consistentSince > 1 && data.brokenSince == 1) {
            returnData.add(0, "0")
            returnData.add(1, data.consistentSince.toString())
        } else if (data.brokenSince > 1 && data.consistentSince == 1) {
            returnData.add(0, data.brokenSince.toString())
            returnData.add(1, "0")
        } else if (data.brokenSince == 0 || data.consistentSince == 0) {
            returnData.add(0, data.brokenSince.toString())
            returnData.add(1, data.consistentSince.toString())
        }
        return returnData
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var dragStartX = 0f
                var dragEndX = 0f
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        dragStartX = offset.x
                    },
                    onDragEnd = {
                        val dragAmount = dragEndX - dragStartX
                        val swipeThreshold = 100

                        if (dragAmount > swipeThreshold) {
                            trackerViewModal.updateDate("decrement")
                        } else if (dragAmount < -swipeThreshold && isSelectedDateLessThanCurrentData(
                                trackerViewModal.selectedDate.value
                            )
                        ) {
                            trackerViewModal.updateDate("increment")
                        }
                    }
                ) { change, _ ->
                    change.consume()
                    dragEndX = change.position.x
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    trackerViewModal.updateDate("decrement")
                },
                modifier = Modifier
                    .padding(start = 10.dp)
                    .size(20.dp)
                    .wrapContentSize(Alignment.Center),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = hashCodeToColor("#4999e9".toColorInt())
                ),
            ) {
                Text(text = "<", fontSize = 10.sp, color = Color.White)
            }
            Column(
                modifier = Modifier
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = trackerViewModal.selectedDate.value, color = Color.White
                )
            }
            Button(
                onClick = {
                    trackerViewModal.updateDate("increment")
                },
                enabled = isSelectedDateLessThanCurrentData(trackerViewModal.selectedDate.value),
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(20.dp)
                    .wrapContentSize(Alignment.Center),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = hashCodeToColor("#4999e9".toColorInt())
                ),
            ) {
                Text(text = ">", fontSize = 10.sp, color = Color.White)
            }
        }

        PullToRefreshBox(
            state = pullToRefreshState,
            onRefresh = {
                coroutineScope.launch {
                    trackerViewModal.forceLoadDataOnPullToRefresh()
                }
            },
            isRefreshing = trackerViewModal.dataFetchIsLoading.value,
            modifier = Modifier
                .fillMaxSize(),
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = trackerViewModal.dataFetchIsLoading.value,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    state = pullToRefreshState
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, top = 28.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {

                trackerViewModal.trackerData.forEachIndexed { index, data ->
                    val isLastItem = index == trackerViewModal.trackerData.lastIndex
                    val consistentData =
                        renderConsistentData(trackerViewModal.consistentData[data.item])
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
                                lastButtonClicked = true
                            },
                            enabled = data.value > 0,
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

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .padding(start = 10.dp),
                            contentAlignment = Alignment.Center

                        ) {
                            Text(
                                text = consistentData[0],
                                color = if (consistentData[0].toInt() > 0) {
                                    Color.Red
                                } else {
                                    Color.Transparent
                                }
                            )
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
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .padding(end = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = consistentData[1],
                                color = if (consistentData[1].toInt() > 0) {
                                    Color.Green
                                } else {
                                    Color.Transparent
                                }
                            )
                        }

                        Button(
                            onClick = {
                                trackerViewModal.incrementValue(index, data.item ?: "")
                                lastButtonClicked = true
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
        }
        Button(
            onClick = {
                if (!trackerViewModal.updateAPICallIsLoading.value) {
                    trackerViewModal.updateTrackerDataAPI()
                }
            },
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.BottomEnd)
                .zIndex(1f) // Ensure it's on top
                .padding(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = hashCodeToColor("#1976d2".toColorInt())
            ),
            contentPadding = PaddingValues(0.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (trackerViewModal.updateAPICallIsLoading.value) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(34.dp)
                            .padding(top = 6.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}