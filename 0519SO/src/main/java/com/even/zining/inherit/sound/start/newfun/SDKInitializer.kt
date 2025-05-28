package com.even.zining.inherit.sound.start.newfun

import android.app.Application
import android.util.Log
import com.bytedance.sdk.openadsdk.api.init.PAGConfig
import com.bytedance.sdk.openadsdk.api.init.PAGSdk
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import java.io.File
import com.thinkup.core.api.TUSDK
import com.even.zining.inherit.sound.znet.lo.FnnA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SDKInitializer {
    suspend fun init(application: Application) {
        withContext(Dispatchers.Main){
            initPAGSdk(application)
            initTUSDK(application)
            initFnnA(application)
        }
    }

    private fun initPAGSdk(application: Application) {
        val path = "${application.dataDir.path}/fnnfl"
        File(path).mkdirs()
        val appId = FnnLoadData.getConfig().appidPangle
        val pAGInitConfig = PAGConfig.Builder().appId(appId).build()
        PAGSdk.init(application, pAGInitConfig, object : PAGSdk.PAGInitCallback {
            override fun success() {
                Log.e("TAG", "PAGInitCallback new api init success")
            }
            override fun fail(code: Int, msg: String) {
                Log.e("TAG", "PAGInitCallback new api init fail: $code")
            }
        })
    }

    private fun initTUSDK(application: Application) {
        TUSDK.init(
            application,
            FnnLoadData.getConfig().appidTopon,
            FnnLoadData.getConfig().appkeyTopon
        )
        Log.e("TAG", "open initSDK: ${FnnLoadData.getConfig().appidTopon}")
    }

    private fun initFnnA(application: Application) {
        FnnA.IntIn(application)
    }
}
