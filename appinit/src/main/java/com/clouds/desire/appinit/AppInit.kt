package com.clouds.desire.appinit

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import android.util.Log
import androidx.work.WorkManager

import androidx.work.Configuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

object AppInit {
    fun init(application: Application, mustXSData: Boolean) {
        getAndroidId(application)
    }
    @SuppressLint("HardwareIds")
    fun getAndroidId(application: Application) {
        val adminData = MMKVUtils.getString(FnnLoadData.appiddata)
        if (adminData.isEmpty()) {
            val androidId =
                Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
            if (!androidId.isNullOrBlank()) {
                MMKVUtils.put(FnnLoadData.appiddata, androidId)
            } else {
                MMKVUtils.put(FnnLoadData.appiddata, UUID.randomUUID().toString())
            }
        }
    }
}