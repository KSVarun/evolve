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
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evol.ui.reusable.LongPressElement
import com.example.evol.utils.hashCodeToColor
import com.example.evol.utils.isSelectedDateLessThanCurrentData
import com.example.evol.viewModel.FoodTrackerViewModel
import com.example.evol.viewModelFactory.FoodTrackerViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Food(context: Context) {
    val foodTrackerViewModal: FoodTrackerViewModel =
        viewModel(factory = FoodTrackerViewModelFactory(context.applicationContext as Application))
    val scrollState = rememberScrollState()
    var lastButtonClicked by remember { mutableStateOf(false) }
    var shouldCallLastAction by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    fun debouncedSaveAPICall() {
        if (!foodTrackerViewModal.updateAPICallIsLoading.value) {
            foodTrackerViewModal.updateTrackerDataAPI()
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

    LaunchedEffect(foodTrackerViewModal.updateAPICallIsLoading.value) {
        if (!foodTrackerViewModal.updateAPICallIsLoading.value && shouldCallLastAction) {
            foodTrackerViewModal.updateTrackerDataAPI()
            shouldCallLastAction = false
        }
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
                            foodTrackerViewModal.updateDate("decrement")
                        } else if (dragAmount < -swipeThreshold && isSelectedDateLessThanCurrentData(
                                foodTrackerViewModal.selectedDate.value
                            )
                        ) {
                            foodTrackerViewModal.updateDate("increment")
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
                    foodTrackerViewModal.updateDate("decrement")
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
                    text = foodTrackerViewModal.selectedDate.value, color = Color.White
                )
            }
            Button(
                onClick = {
                    foodTrackerViewModal.updateDate("increment")
                },
                enabled = isSelectedDateLessThanCurrentData(foodTrackerViewModal.selectedDate.value),
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
                    foodTrackerViewModal.forceLoadDataOnPullToRefresh()
                }
            },
            isRefreshing = foodTrackerViewModal.dataFetchIsLoading.value,
            modifier = Modifier
                .fillMaxSize(),
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = foodTrackerViewModal.dataFetchIsLoading.value,
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

                foodTrackerViewModal.foodTrackerData.forEachIndexed { index, data ->
                    val isLastItem = index == foodTrackerViewModal.foodTrackerData.lastIndex
                    val consistencyData = foodTrackerViewModal.consistentData[data.item]
                    val negativeStreak = consistencyData?.brokenSince ?: 0
                    val positiveStreak = consistencyData?.consistentSince ?: 0
                    val longestPositiveStreak = consistencyData?.longestConsistentSince ?: 0
                    val longestNegativeStreak = consistencyData?.longestBrokenSince ?: 0
                    val backgroundColor = if (data.value > 0) {
                        hashCodeToColor("#4999e9".toColorInt())
                    } else {
                        hashCodeToColor("#9AA6B2".toColorInt())
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (isLastItem) 75.dp else 5.dp)
                            .clip(RoundedCornerShape(42.dp))
                            .background(color = hashCodeToColor("#474747".toColorInt())),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LongPressElement(modifier = Modifier
                            .padding(start = 10.dp)
                            .size(50.dp)
                            .wrapContentSize(Alignment.Center)
                            .clip(RoundedCornerShape(20.dp))
                            .background(backgroundColor)
                            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 8.dp), onClick = {
                            if (data.value > 0) {
                                foodTrackerViewModal.decrementValue(index)
                                lastButtonClicked = true
                            }
                        }, onLongClick = { if (data.value > 0) {
                            foodTrackerViewModal.decrementValueOnLongPress(index, data.item)
                            lastButtonClicked = true} }, text = "-")

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .padding(start = 10.dp),
                            contentAlignment = Alignment.Center

                        ) {
                            Text(
                                text = negativeStreak.toString(),
                                color = if (negativeStreak > 0) {
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
                            Text(
                                text = data.item,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                            Text(
                                text = data.value.toString()
                            )
                            Text(
                                text = "Best +$longestPositiveStreak / -$longestNegativeStreak",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .padding(end = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = positiveStreak.toString(),
                                color = if (positiveStreak > 0) {
                                    Color.Green
                                } else {
                                    Color.Transparent
                                }
                            )
                        }

                        LongPressElement(modifier = Modifier.padding(end = 10.dp)
                            .size(50.dp)
                            .wrapContentSize(Alignment.Center)
                            .clip(RoundedCornerShape(20.dp))
                            .background(hashCodeToColor("#4999e9".toColorInt()))
                            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 8.dp), onClick = {
                            foodTrackerViewModal.incrementValue(index, data.item)
                            lastButtonClicked = true
                        }, onLongClick = {
                            foodTrackerViewModal.incrementValueOnLongPress(index)
                            lastButtonClicked = true} , text = "+")
                    }
                }
            }
        }
        Button(
            onClick = {
                if (!foodTrackerViewModal.updateAPICallIsLoading.value) {
                    foodTrackerViewModal.updateTrackerDataAPI()
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
                if (foodTrackerViewModal.updateAPICallIsLoading.value) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(34.dp)
                            .padding(top = 6.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}
