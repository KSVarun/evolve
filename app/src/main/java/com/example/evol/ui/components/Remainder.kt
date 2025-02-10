package com.example.evol.ui.components

import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evol.entity.Remainder
import com.example.evol.utils.convertDateTimeToMillis
import com.example.evol.utils.convertMillisToDateTime
import com.example.evol.utils.getCurrentDate
import com.example.evol.utils.getDefaultHourToShow
import com.example.evol.utils.getValueInTwoDigits
import com.example.evol.utils.hashCodeToColor
import com.example.evol.viewModel.RemainderViewModel
import com.example.evol.viewModelFactory.RemainderViewModelFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Remainder(context: Context) {
    val remainderViewModal: RemainderViewModel =
        viewModel(factory = RemainderViewModelFactory(context.applicationContext as Application))
    val scrollState = rememberScrollState()
    val showDialog = remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val date = remember {
        mutableStateOf(getCurrentDate())
    }
    var hourValue by remember {
        mutableStateOf(getDefaultHourToShow())
    }
    var minuteValue by remember {
        mutableStateOf("00")
    }
    val calendar = Calendar.getInstance()



    if (showDialog.value) {
        AlertDialog(
            modifier = Modifier.height(400.dp),
            onDismissRequest = { showDialog.value = false },
            title = { Text("Add remainder") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { value ->
                            if (value.length <= 20) title = value
                        },
                        label = { Text("Title (max 20 characters)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier
                        .fillMaxWidth()) {
                        Text(text = date.value, modifier =  Modifier.clickable {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    calendar.set(year, month, dayOfMonth)
                                    val currentMonth = getValueInTwoDigits(month+1)
                                    date.value = "$dayOfMonth/$currentMonth/$year"
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
                    val dateTimeInMilli = convertDateTimeToMillis(date.value,
                        "$hourValue:$minuteValue"
                    )
                    val remainder = Remainder(id = null,title=title, description=description, time=dateTimeInMilli, workerId = "")
                    remainderViewModal.remainderData.add(remainder)
                    remainderViewModal.insertRemainderToDB(remainder = remainder)
                    showDialog.value = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
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
            if(remainderViewModal.remainderData.size==0) {
                Text(text = "No remainders!", fontSize = 25.sp, color = Color.White)
            }else{
                remainderViewModal.remainderData.forEachIndexed { index, remainder ->
                    Row {
                        Column {
                            Text(text = remainder.title)
                            Text(text = remainder.description)
                            Text(text = convertMillisToDateTime(remainder.time))
                        }
                    }
                }
            }

        }
        Button(
            onClick = {
                showDialog.value = true
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
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
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