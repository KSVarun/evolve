package com.example.evol.ui.components

import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkManager
import com.example.evol.constants.newFilterText
import com.example.evol.constants.oldFilterText
import com.example.evol.entity.Remainder
import com.example.evol.utils.checkForNotificationPermission
import com.example.evol.utils.convertDateTimeToMillis
import com.example.evol.utils.convertMillisToDateTime
import com.example.evol.utils.getCurrentDate
import com.example.evol.utils.getDefaultHourToShow
import com.example.evol.utils.getValueInTwoDigits
import com.example.evol.utils.hashCodeToColor
import com.example.evol.utils.scheduleNotification
import com.example.evol.viewModel.RemainderViewModel
import com.example.evol.viewModelFactory.RemainderViewModelFactory
import java.util.Calendar
import java.util.UUID


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Remainder(context: Context) {


    val remainderViewModal: RemainderViewModel =
        viewModel(factory = RemainderViewModelFactory(context.applicationContext as Application))
    val remainderData by remainderViewModal.remainderData.observeAsState(emptyList())
    fun getPastRemainders(): List<Remainder> {
        return remainderData.filter {
            it.time < System.currentTimeMillis()
        }.sortedBy { it.time }
    }

    fun getFutureRemainders(): List<Remainder> {
        return remainderData.filter {
            it.time > System.currentTimeMillis()
        }.sortedBy { it.time }
    }

    val scrollState = rememberScrollState()
    val showDialog = remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember {
        mutableStateOf(getCurrentDate())
    }
    var hourValue by remember {
        mutableStateOf(getDefaultHourToShow())
    }
    var minuteValue by remember {
        mutableStateOf("00")
    }
    var titleErrorMessage by remember {
        mutableStateOf("")
    }
    val filter = remember {
        mutableStateListOf(newFilterText)
    }
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, hourValue.toInt())
    calendar.set(Calendar.MINUTE, minuteValue.toInt())
    val editingRemainder: MutableState<Remainder?> = remember {
        mutableStateOf(null)
    }
    val focusManager = LocalFocusManager.current


    fun recomputeResultRemainders(data: List<Remainder>): List<Remainder> {
        if (search.isNotEmpty() && filter.contains(oldFilterText) && filter.contains(newFilterText)) {
            return data.filter {
                it.title.contains(
                    search
                )
            }.sortedBy { it.time }
        } else if (search.isEmpty() && filter.contains(oldFilterText) && filter.contains(
                newFilterText
            )
        ) {
            return data.sortedBy { it.time }
        } else if (search.isNotEmpty() && filter.contains(oldFilterText) && !filter.contains(
                newFilterText
            )
        ) {
            return data.filter {
                it.title.contains(
                    search
                ) && it.time < System.currentTimeMillis()
            }.sortedBy { it.time }
        } else if (search.isNotEmpty() && !filter.contains(oldFilterText) && filter.contains(
                newFilterText
            )
        ) {
            return data.filter {
                it.title.contains(
                    search
                ) && it.time > System.currentTimeMillis()
            }.sortedBy { it.time }
        } else if (search.isEmpty() && filter.contains(oldFilterText) && !filter.contains(
                newFilterText
            )
        ) {
            return getPastRemainders()
        } else if (search.isEmpty() && !filter.contains(oldFilterText) && filter.contains(
                newFilterText
            )
        ) {
            return getFutureRemainders()
        } else {
            return data.sortedBy { it.time }
        }
    }

    val resultRemainders = remember {
        derivedStateOf {
            recomputeResultRemainders(remainderData)
        }
    }

    fun resetValuesToDefault() {
        title = ""
        description = ""
        hourValue = getDefaultHourToShow()
        date = getCurrentDate()
        minuteValue = "00"
        editingRemainder.value = null
        titleErrorMessage = ""
    }

    if (showDialog.value) {
        AlertDialog(modifier = Modifier.height(430.dp),
            onDismissRequest = { showDialog.value = false },
            title = { Text("Add remainder") },
            text = {
                Column {
                    OutlinedTextField(value = title,
                        onValueChange = { value ->
                            if (value.isNotEmpty() && titleErrorMessage.isNotEmpty()) {
                                titleErrorMessage = ""
                            }
                            if (value.isEmpty()) {
                                titleErrorMessage = "Title is required!"
                            }
                            if (value.length <= 50) title = value
                        },
                        label = { Text("Title* (max 50 characters)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (titleErrorMessage.isNotEmpty()) {
                        Text(
                            text = titleErrorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 5.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)

                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = date, modifier = Modifier.clickable {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    calendar.set(year, month, dayOfMonth)
                                    val selectedDate = getValueInTwoDigits(dayOfMonth)
                                    val currentMonth = getValueInTwoDigits(month + 1)
                                    date = "$selectedDate/$currentMonth/$year"
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        })
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                                            calendar.set(Calendar.MINUTE, minute)
                                            calendar.set(Calendar.SECOND, 0)
                                            calendar.set(Calendar.MILLISECOND, 0)

                                            hourValue = getValueInTwoDigits(hour)
                                            minuteValue = getValueInTwoDigits(minute)

                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                },
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Text(text = hourValue)
                            Text(" : ")
                            Text(text = minuteValue)
                        }
                    }

                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isEmpty()) {
                        titleErrorMessage = "Title is required"
                        return@Button
                    }
                    val dateTimeInMilli = convertDateTimeToMillis(
                        date, "$hourValue:$minuteValue"
                    )
                    val remainder = Remainder(
                        id = editingRemainder.value?.id ?: UUID.randomUUID(),
                        title = title,
                        description = description,
                        time = dateTimeInMilli,
                        workerId = editingRemainder.value?.workerId
                    )
                    if (editingRemainder.value?.id !== null) {
                        if (editingRemainder.value?.workerId !== null) {
                            editingRemainder.value!!.workerId?.let {
                                WorkManager.getInstance(context).cancelWorkById(
                                    it
                                )
                            }
                        }
                        val workerId = scheduleNotification(
                            context,
                            remainder.time - System.currentTimeMillis(),
                            remainder.title,
                            remainder.description
                        )
                        remainder.workerId = workerId
                        remainderViewModal.updateRemainderByIDFromDB(
                            remainder.id,
                            remainder.title,
                            remainder.description,
                            remainder.time,
                            remainder.workerId!!
                        )
                    } else {
                        val workerId = scheduleNotification(
                            context,
                            remainder.time - System.currentTimeMillis(),
                            remainder.title,
                            remainder.description
                        )
                        remainder.workerId = workerId
                        remainderViewModal.insertRemainderToDB(remainder)
                    }
                    resetValuesToDefault()
                    showDialog.value = false
                    recomputeResultRemainders(remainderData)
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = {
                    resetValuesToDefault()
                    showDialog.value = false
                }) {
                    Text("Cancel")
                }
            })
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures { focusManager.clearFocus() }
        }

    ) {
        Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp)) {
            OutlinedTextField(value = search,
                onValueChange = { value ->
                    search = value
                    resultRemainders.apply { recomputeResultRemainders(remainderData) }
                },
                label = { Text("Search by title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Row(modifier = Modifier.padding(bottom = 4.dp)) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (filter.contains(oldFilterText) && filter.contains(newFilterText)) {
                            filter.remove(oldFilterText)
                            resultRemainders.apply { recomputeResultRemainders(remainderData) }
                        } else if (filter.contains(oldFilterText) && !filter.contains(newFilterText)) {
                            filter.add(newFilterText)
                            filter.remove(oldFilterText)
                            resultRemainders.apply { recomputeResultRemainders(remainderData) }
                            Toast.makeText(
                                context, "At least one filter will be selected", Toast.LENGTH_SHORT
                            ).show()
                        } else if (!filter.contains(oldFilterText) && filter.contains(newFilterText)) {
                            filter.add(oldFilterText)
                            resultRemainders.apply { recomputeResultRemainders(remainderData) }
                        } else {
                            filter.add(oldFilterText)
                            resultRemainders.apply { recomputeResultRemainders(remainderData) }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (filter.contains(
                                oldFilterText
                            )
                        ) {
                            hashCodeToColor("#4999e9".toColorInt())
                        } else {
                            Color.Gray
                        }
                    ),

                    ) {
                    Text("Past")
                }
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (filter.contains(newFilterText) && filter.contains(oldFilterText)) {
                            filter.remove(newFilterText)
                            resultRemainders.apply { recomputeResultRemainders(remainderData) }
                        } else if (filter.contains(newFilterText) && !filter.contains(oldFilterText)) {
                            filter.add(oldFilterText)
                            filter.remove(newFilterText)
                            resultRemainders.apply { recomputeResultRemainders(remainderData) }
                            Toast.makeText(
                                context, "At least one filter will be selected", Toast.LENGTH_SHORT
                            ).show()
                        } else if (filter.contains(oldFilterText) && !filter.contains(newFilterText)) {
                            filter.add(newFilterText)
                            resultRemainders.apply { recomputeResultRemainders(remainderData) }
                        } else {
                            filter.add(newFilterText)
                            resultRemainders.apply { recomputeResultRemainders(remainderData) }
                        }

                    },
                    modifier = Modifier.padding(start = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (filter.contains(newFilterText)) {
                            hashCodeToColor("#4999e9".toColorInt())
                        } else {
                            Color.Gray
                        }
                    )
                ) {
                    Text("Future")
                }
            }
             Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                when {
                    search.isNotEmpty() && resultRemainders.value.isEmpty() -> {
                        Text(
                            text = "No remainders, please refine your search or filter!",
                            fontSize = 25.sp
                        )

                    }

                    resultRemainders.value.isEmpty() -> {
                        Text(text = "No remainders!", fontSize = 25.sp)
                    }

                    else -> {
                        resultRemainders.value.forEachIndexed { _, remainder ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (System.currentTimeMillis() < remainder.time) {
                                            Color.Transparent
                                        } else {
                                            Color.Gray
                                        }
                                    )
                                    .border(
                                        1.dp, if (System.currentTimeMillis() < remainder.time) {
                                            hashCodeToColor("#4999e9".toColorInt())
                                        } else {
                                            Color.Gray
                                        }, shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp)

                            ) {
                                Column {
                                    Text(text = remainder.title, fontSize = 25.sp)
                                    Text(text = remainder.description)
                                    Text(
                                        text = convertMillisToDateTime(
                                            remainder.time, "dd/MM/yyyy HH:mm"
                                        )
                                    )
                                    Row {
                                        Button(onClick = {
                                            focusManager.clearFocus()
                                            if (remainder.workerId !== null) {
                                                WorkManager.getInstance(context)
                                                    .cancelWorkById(remainder.workerId!!)
                                            }
                                            remainderViewModal.deleteRemainderFromDB(remainder.id)
                                            recomputeResultRemainders(remainderData)
                                        }, modifier = Modifier.padding(end = 10.dp)) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Button(onClick = {
                                            focusManager.clearFocus()
                                            showDialog.value = true
                                            editingRemainder.value = remainder
                                            title = remainder.title
                                            description = remainder.description
                                            date = convertMillisToDateTime(
                                                remainder.time,
                                                "dd/MM/yyyy"
                                            )
                                            hourValue =
                                                convertMillisToDateTime(remainder.time, "HH")
                                            minuteValue =
                                                convertMillisToDateTime(remainder.time, "mm")
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

            }
        }
        Button(
            onClick = {
                if(checkForNotificationPermission(context)){
                    focusManager.clearFocus()
                    showDialog.value = true
                }else {
                    Toast.makeText(
                        context, "Please allow notification permission in app settings", Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.BottomEnd)
                .zIndex(1f)
                .padding(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = hashCodeToColor("#1976d2".toColorInt())
            ),
            contentPadding = PaddingValues(0.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(30.dp)
                )

            }
        }

    }
}