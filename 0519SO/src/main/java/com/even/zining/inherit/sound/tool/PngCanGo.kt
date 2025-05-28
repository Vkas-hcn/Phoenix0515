package com.even.zining.inherit.sound.tool

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import androidx.core.content.ContextCompat
import com.even.zining.inherit.sound.start.FnnStartFun
import com.even.zining.inherit.sound.zbmvre.fnnserv.FnnFService
import com.even.zining.inherit.sound.start.FnnStartFun.mainStart
import com.even.zining.inherit.sound.start.newfun.Logger
import java.util.ArrayList
import kotlinx.coroutines.*

object PngCanGo {
    var KEY_IS_SERVICE = false
    var activityList = ArrayList<Activity>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var serviceJob: Job? = null
    fun closeAllActivities() {
        for (activity in activityList) {
            activity.finishAndRemoveTask()
        }
        activityList.clear()
    }

    fun addActivity(activity: Activity) {
        activityList.add(activity)
    }

    fun removeActivity(activity: Activity) {
        activityList.remove(activity)
    }
    fun getActivity(): ArrayList<Activity> {
        return activityList
    }

    fun getInstallTimeDataFun(): Long {
        try {
            val packageManager: PackageManager = mainStart.packageManager
            val packageInfo: PackageInfo = packageManager.getPackageInfo(mainStart.packageName, 0)
            val firstInstallTime: Long = packageInfo.firstInstallTime
            return (System.currentTimeMillis() - firstInstallTime) / 1000
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return 0L
        }
    }

    fun getInstallFast(): Long {
        try {
            val packageManager: PackageManager = mainStart.packageManager
            val packageInfo: PackageInfo = packageManager.getPackageInfo(mainStart.packageName, 0)
            return packageInfo.firstInstallTime
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return 0L
        }
    }


    fun startService() {
        stopService()
        serviceJob = coroutineScope.launch {
            while (isActive) {
                Logger.showLog("startService---1-----$KEY_IS_SERVICE")
                if (!KEY_IS_SERVICE && Build.VERSION.SDK_INT < 31) {
                    Logger.showLog("startService---2-----$KEY_IS_SERVICE")
                   try {
                       ContextCompat.startForegroundService(
                           mainStart,
                           Intent(mainStart, FnnFService::class.java)
                       )
                   }catch (e: Exception){
                   }

                } else {
                    Logger.showLog("startService---3-----$KEY_IS_SERVICE")
                    stopService()
                    break
                }

                delay(1020)
            }
        }
    }

    private fun stopService() {
        serviceJob?.cancel()
        serviceJob = null
    }


    fun isMainProcess(context: Context): Boolean {
        val currentProcessName = getCurrentProcessName(context)
        return currentProcessName == context.packageName
    }

    private fun getCurrentProcessName(context: Context): String? {
        val pid = Process.myPid()
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        for (processInfo in activityManager.runningAppProcesses) {
            if (processInfo.pid == pid) {
                return processInfo.processName
            }
        }
        return null
    }

}