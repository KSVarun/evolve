package com.example.evol.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


data class SelectedDatesData(
    val item: String,
    var value: Int
)

@Composable
fun Tracker(){
    val selectedDatesData = listOf(SelectedDatesData(item = "Meditation", value = 0),
        SelectedDatesData(item = "FC", value = 0),
        SelectedDatesData(item = "Carrot", value = 0))

    Column {
        selectedDatesData.forEach { data ->
            Row (  modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically){
                Text(
                    text = data.item
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = data.value.toString()

                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
