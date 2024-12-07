package com.example.evol.utils

import android.content.Context
import android.os.PowerManager


fun checkBatteryLifePermission(context: Context): Boolean {
    try {
        val pm: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)

    } catch (e: Throwable) {
        println(e)
    }
    return false
}