package com.thunderbolt.methods.bodhisattva.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.os.StatFs
import android.text.format.Formatter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.thunderbolt.methods.bodhisattva.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel : BaseViewModel() {
    // RAM 数据
    private val _ramUsagePercent = MutableLiveData<Int>()
    val ramUsagePercent: LiveData<Int> = _ramUsagePercent

    private val _usedRam = MutableLiveData<String>()
    val usedRam: LiveData<String> = _usedRam

    private val _availableRam = MutableLiveData<String>()
    val availableRam: LiveData<String> = _availableRam

    // 存储数据
    private val _systemStoragePercent = MutableLiveData<Int>()
    val systemStoragePercent: LiveData<Int> = _systemStoragePercent

    private val _internalStoragePercent = MutableLiveData<Int>()
    val internalStoragePercent: LiveData<Int> = _internalStoragePercent

    // 电池数据
    private val _batteryPercent = MutableLiveData<Int>()
    val batteryPercent: LiveData<Int> = _batteryPercent

    private val _batteryVoltage = MutableLiveData<String>()
    val batteryVoltage: LiveData<String> = _batteryVoltage

    private val _batteryTemp = MutableLiveData<String>()
    val batteryTemp: LiveData<String> = _batteryTemp

    // 初始化系统信息
    init {
        refreshDeviceInfo()
    }

    private fun refreshDeviceInfo() {
        viewModelScope.launch {
            try {
                // 获取内存信息
                getMemoryInfo()

                // 获取存储信息
                getStorageInfo("/system", _systemStoragePercent)
                getStorageInfo("/data", _internalStoragePercent)
            } catch (e: Exception) {
                Log.e("DeviceViewModel", "Error refreshing device info", e)
            }
        }
    }

    // 获取内存信息
    private suspend fun getMemoryInfo() = withContext(Dispatchers.IO) {
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        (App.instance.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager).getMemoryInfo(
            memoryInfo
        )
        val totalRam = memoryInfo.totalMem
        val availableRam = memoryInfo.availMem
        val usedRam = totalRam - availableRam
        val ramUsagePercent = (usedRam.toDouble() / totalRam.toDouble() * 100).toInt()
        _ramUsagePercent.postValue(ramUsagePercent)
        _usedRam.postValue(Formatter.formatFileSize(App.instance, usedRam))
        _availableRam.postValue(Formatter.formatFileSize(App.instance, availableRam))
    }

    // 获取存储信息
    private suspend fun getStorageInfo(path: String, liveData: MutableLiveData<Int>) = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists()) return@withContext

            val stat = StatFs(file.absolutePath)
            val total = stat.blockCountLong * stat.blockSizeLong
            val free = stat.availableBlocksLong * stat.blockSizeLong
            val used = total - free
            val percent = (used.toDouble() / total.toDouble() * 100).toInt()

            liveData.postValue(percent)
        } catch (e: Exception) {
            Log.e("DeviceViewModel", "Error getting storage info for $path", e)
        }
    }

    // 更新电池信息
    fun updateBatteryInfo(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val tempCelsius = temperature / 10f
        val voltageV = voltage / 1000f
        val batteryPct = level * 100 / scale.toFloat()

        _batteryPercent.postValue(batteryPct.toInt())
        _batteryVoltage.postValue("%.2fV".format(voltageV))
        _batteryTemp.postValue("%.1f°C".format(tempCelsius))
    }
}
