package com.even.zining.inherit.sound.start.newfun

import android.os.Build
import com.appsflyer.AFLogger
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.even.zining.inherit.sound.pangle.AdXian
import com.even.zining.inherit.sound.start.DataGetUtils
import com.even.zining.inherit.sound.start.FnnStartFun.mainStart
import com.even.zining.inherit.sound.start.FnnStartFun.mustXS
import com.even.zining.inherit.sound.start.newfun.Logger.showLog
import com.even.zining.inherit.sound.tool.TbaPostTool
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.tool.data.MMKVUtils
import com.even.zining.inherit.sound.tool.NetPostTool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AdDataProcessor {
    fun canIntNextFun() {
        val adScheduler = AdXian()
        adScheduler.startRomFun()
    }


    fun startOneTimeAdminData() {
        val adminData = MMKVUtils.getString(FnnLoadData.admindata)
        showLog("startOneTimeAdminData: $adminData")
        if (adminData.isEmpty()) {
            NetPostTool.onePostAdmin()
        } else {
            NetPostTool.twoPostAdmin()
        }
        //1hours
        scheduleHourlyAdminRequest()
        DataGetUtils.initFaceBook()
    }

    private fun scheduleHourlyAdminRequest() {
        // 协程
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(1000 * 60 * 60)
                showLog("延迟1小时循环请求")
                DataGetUtils.executeAdminRequest(object : DataGetUtils.ResultCallback {
                    override fun onComplete(result: String) {
                        showLog("Admin request successful: $result")
                    }

                    override fun onError(message: String) {
                        showLog("Admin request failed: ${message}")
                    }
                })
            }
        }
    }

    fun initAppsFlyer() {
        val appsFlyer = AppsFlyerLib.getInstance()
        val appId = MMKVUtils.getString(FnnLoadData.appiddata)
        showLog("AppsFlyer-id: $${FnnLoadData.getConfig().appsId}")
        appsFlyer.init(FnnLoadData.getConfig().appsId, object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionDataMap: MutableMap<String, Any>?) {
                val status = conversionDataMap?.get("af_status") as? String ?: "null_status"
                showLog("AppsFlyer: $status")
                TbaPostTool.pointInstallAf(status)

                conversionDataMap?.forEach { (key, value) ->
                    try {
                        showLog("AppsFlyer-all: key=$key: value=$value")
                    } catch (e: Exception) {
                        showLog("AppsFlyer-logError: ${e.localizedMessage}")
                    }
                }
            }

            override fun onConversionDataFail(p0: String?) {
                showLog("AppsFlyer: onConversionDataFail $p0")
            }

            override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                showLog("AppsFlyer: onAppOpenAttribution $p0")
            }

            override fun onAttributionFailure(p0: String?) {
                showLog("AppsFlyer: onAttributionFailure $p0")
            }
        }, mainStart)
        AppsFlyerLib.getInstance().setLogLevel(AFLogger.LogLevel.ERROR)
        appsFlyer.setCustomerUserId(appId)
        appsFlyer.start(mainStart)

        appsFlyer.logEvent(
            mainStart,
            "systemsentry_install",
            buildMap {
                put("customer_user_id", appId)
                put("app_version", TbaPostTool.showAppVersion())
                put("os_version", Build.VERSION.RELEASE)
                put("bundle_id", mainStart.packageName)
                put("language", "asc_wds")
                put("platform", "raincoat")
                put("android_id", appId)
            }
        )
    }
}
