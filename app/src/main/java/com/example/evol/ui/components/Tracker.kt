package com.example.evol.ui.components

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
import androidx.compose.ui.unit.dp


data class SelectedDatesData(
    val item: String,
    var value: Int
)

@Composable
fun Tracker(){
    val selectedDatesData = remember {
        mutableStateListOf(
            SelectedDatesData(item = "Meditation", value = 0),
            SelectedDatesData(item = "FC", value = 0),
            SelectedDatesData(item = "Carrot", value = 0)
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        selectedDatesData.forEachIndexed { index,data ->
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
                            selectedDatesData[index] = data.copy(value = data.value + 1)
                        }
                )
                Text(
                    text = data.item,
                    modifier = Modifier.weight(1f)
                )
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
                        .clickable(enabled = data.value > 0) {
                                selectedDatesData[index] = data.copy(value = data.value - 1)
                        }
                )
            }
        }
    }
}
