package com.example.evol.ui.components

import android.app.Application
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evol.entity.Consistency
import com.example.evol.utils.isSelectedDateLessThanCurrentData
import com.example.evol.viewModel.HabitTrackerViewModel
import com.example.evol.viewModelFactory.HabitTrackerViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Tracker(context: Context) {
    val habitTrackerViewModal: HabitTrackerViewModel =
        viewModel(factory = HabitTrackerViewModelFactory(context.applicationContext as Application))
    val scrollState = rememberScrollState()
    var lastButtonClicked by remember { mutableStateOf(false) }
    var shouldCallLastAction by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    val backgroundColor = Color(0xFFF6F8FC)
    val cardColor = Color.White
    val accentBlue = Color(0xFF2D6BFF)
    val mutedText = Color(0xFF8D9AAE)
    val titleText = Color(0xFF1E2A3B)

    fun debouncedSaveAPICall() {
        if (!habitTrackerViewModal.updateAPICallIsLoading.value) {
            habitTrackerViewModal.updateTrackerDataAPI()
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

    LaunchedEffect(habitTrackerViewModal.updateAPICallIsLoading.value) {
        if (!habitTrackerViewModal.updateAPICallIsLoading.value && shouldCallLastAction) {
            habitTrackerViewModal.updateTrackerDataAPI()
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

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val currentDate = remember(habitTrackerViewModal.selectedDate.value) {
        runCatching {
            LocalDate.parse(habitTrackerViewModal.selectedDate.value, dateFormatter)
        }.getOrElse { LocalDate.now() }
    }
    val todayDate = remember { LocalDate.now() }
    val weekStart = remember(currentDate) {
        currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    val weekDates = remember(weekStart) {
        (0..6).map { weekStart.plusDays(it.toLong()) }
    }
    var isCalendarOpen by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val zone = java.time.ZoneId.systemDefault()
                val date = java.time.Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(zone)
                    .toLocalDate()
                return !date.isAfter(todayDate)
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year <= todayDate.year
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
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
                            habitTrackerViewModal.updateDate("decrement")
                        } else if (dragAmount < -swipeThreshold && isSelectedDateLessThanCurrentData(
                                habitTrackerViewModal.selectedDate.value
                            )
                        ) {
                            habitTrackerViewModal.updateDate("increment")
                        }
                    }
                ) { change, _ ->
                    change.consume()
                    dragEndX = change.position.x
                }
            }
    ) {
        PullToRefreshBox(
            state = pullToRefreshState,
            onRefresh = {
                coroutineScope.launch {
                    habitTrackerViewModal.forceLoadDataOnPullToRefresh()
                }
            },
            isRefreshing = habitTrackerViewModal.dataFetchIsLoading.value,
            modifier = Modifier.fillMaxSize(),
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = habitTrackerViewModal.dataFetchIsLoading.value,
                    containerColor = Color.White,
                    color = accentBlue,
                    state = pullToRefreshState
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 24.dp)
            ) {
                val headerText = remember(currentDate) {
                    val monthDay = currentDate.format(
                        DateTimeFormatter.ofPattern("MMM, d EEE", Locale.getDefault())
                    )
                    if (currentDate.year == todayDate.year) {
                        monthDay
                    } else {
                        "$monthDay - ${currentDate.year}"
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = headerText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = titleText
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "Calendar",
                                tint = mutedText,
                                modifier = Modifier
                                    .size(22.dp)
                                    .combinedClickable(
                                        onClick = { isCalendarOpen = true },
                                        onLongClick = {}
                                    )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                        .pointerInput(currentDate) {
                            var dragStartX = 0f
                            var dragEndX = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    dragStartX = offset.x
                                },
                                onDragEnd = {
                                    val dragAmount = dragEndX - dragStartX
                                    val swipeThreshold = 60
                                    val candidateDate = when {
                                        dragAmount > swipeThreshold -> currentDate.minusWeeks(1)
                                        dragAmount < -swipeThreshold -> currentDate.plusWeeks(1)
                                        else -> null
                                    }
                                    if (candidateDate != null && !candidateDate.isAfter(todayDate)) {
                                        habitTrackerViewModal.setSelectedDate(
                                            candidateDate.format(dateFormatter)
                                        )
                                    }
                                }
                            ) { change, _ ->
                                change.consume()
                                dragEndX = change.position.x
                            }
                        },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekDates.forEach { date ->
                        val isSelected = date == currentDate
                        WeekDayItem(
                            day = date.dayOfWeek.getDisplayName(
                                TextStyle.SHORT,
                                Locale.getDefault()
                            ).uppercase(Locale.getDefault()),
                            date = date.dayOfMonth,
                            isSelected = isSelected,
                            accentBlue = accentBlue,
                            mutedText = mutedText,
                            isEnabled = !date.isAfter(todayDate),
                            onClick = {
                                if (!date.isAfter(todayDate)) {
                                    habitTrackerViewModal.setSelectedDate(
                                        date.format(dateFormatter)
                                    )
                                }
                            }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    habitTrackerViewModal.habitTrackerData.forEachIndexed { index, data ->
                        val consistentData =
                            renderConsistentData(habitTrackerViewModal.consistentData[data.item])
                        val streakCount = consistentData[1].toIntOrNull() ?: 0
                        val goal = habitTrackerViewModal.getMaxThreshold(data.item)
                        val progress = if (goal != null && goal > 0) {
                            (data.value / goal.toDouble()).toFloat().coerceIn(0f, 1f)
                        } else {
                            null
                        }
                        val isComplete = progress != null && progress >= 1f
                        val isEmpty = data.value <= 0.0
                        val valueText = if (data.value % 1.0 == 0.0) {
                            data.value.toInt().toString()
                        } else {
                            String.format(Locale.getDefault(), "%.1f", data.value)
                        }
                        val goalText = goal?.toString() ?: "--"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .alpha(if (isEmpty && streakCount == 0) 0.55f else 1f)
                                .combinedClickable(
                                    onClick = {
                                        habitTrackerViewModal.incrementValue(index, data.item)
                                        lastButtonClicked = true
                                    },
                                    onLongClick = {
                                        if (data.value > 0) {
                                            habitTrackerViewModal.decrementValueOnLongPress(
                                                index,
                                                data.item
                                            )
                                            lastButtonClicked = true
                                        }
                                    }
                                ),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProgressBadge(
                                    progress = progress,
                                    isComplete = isComplete,
                                    isEmpty = isEmpty,
                                    accentBlue = accentBlue,
                                    mutedText = mutedText
                                )

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp)
                                ) {
                                    Text(
                                        text = data.item,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = titleText
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Whatshot,
                                            contentDescription = null,
                                            tint = Color(0xFFF4B64E),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = " ${streakCount} day streak",
                                            fontSize = 13.sp,
                                            color = Color(0xFFF4B64E)
                                        )
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "Goal: $goalText",
                                        fontSize = 13.sp,
                                        color = mutedText
                                    )
                                    Text(
                                        text = valueText,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = titleText
                                    )
                                }
                            }
                        }
                    }
                }

                ElevatedButton(
                    onClick = {
                        if (!habitTrackerViewModal.updateAPICallIsLoading.value) {
                            habitTrackerViewModal.updateTrackerDataAPI()
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color.White,
                        contentColor = accentBlue
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 2.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Routine",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        if (isCalendarOpen) {
            DatePickerDialog(
                onDismissRequest = { isCalendarOpen = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedMillis = datePickerState.selectedDateMillis
                            if (selectedMillis != null) {
                                val zone = java.time.ZoneId.systemDefault()
                                val pickedDate = java.time.Instant.ofEpochMilli(selectedMillis)
                                    .atZone(zone)
                                    .toLocalDate()
                                if (!pickedDate.isAfter(todayDate)) {
                                    habitTrackerViewModal.setSelectedDate(
                                        pickedDate.format(dateFormatter)
                                    )
                                }
                            }
                            isCalendarOpen = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isCalendarOpen = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeekDayItem(
    day: String,
    date: Int,
    isSelected: Boolean,
    accentBlue: Color,
    mutedText: Color,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .alpha(if (isEnabled) 1f else 0.35f)
            .combinedClickable(onClick = onClick, onLongClick = {})
    ) {
        Text(
            text = day,
            fontSize = 12.sp,
            color = if (isSelected) accentBlue else mutedText,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.toString(),
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Text(
                text = date.toString(),
                fontSize = 14.sp,
                color = Color(0xFF1E2A3B),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp)
            )
        }
    }
}

@Composable
private fun ProgressBadge(
    progress: Float?,
    isComplete: Boolean,
    isEmpty: Boolean,
    accentBlue: Color,
    mutedText: Color
) {
    Box(
        modifier = Modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isComplete -> {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = accentBlue
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            isEmpty -> {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color(0xFFF0F2F6)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = mutedText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            progress != null -> {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(48.dp),
                        color = accentBlue,
                        strokeWidth = 4.dp,
                        trackColor = Color(0xFFE7ECF4)
                    )
                    Text(
                        text = "${(progress * 100).roundToInt()}%",
                        fontSize = 12.sp,
                        color = accentBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            else -> {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color(0xFFF0F2F6)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "0%",
                            fontSize = 12.sp,
                            color = mutedText
                        )
                    }
                }
            }
        }
    }
}
