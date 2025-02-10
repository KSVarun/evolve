package com.example.evol.utils

import androidx.compose.ui.graphics.Color

fun hashCodeToColor(hashCode: Int): Color {
    val red = (hashCode shr 16) and 0xFF
    val green = (hashCode shr 8) and 0xFF
    val blue = hashCode and 0xFF
    return Color(red, green, blue)
}