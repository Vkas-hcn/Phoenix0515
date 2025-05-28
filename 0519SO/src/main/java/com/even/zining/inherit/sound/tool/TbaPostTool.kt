package com.even.zining.inherit.sound.tool

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.appsflyer.AFAdRevenueData
import com.appsflyer.AdRevenueScheme
import com.appsflyer.AppsFlyerLib
import com.appsflyer.MediationNetwork
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAd
import com.even.zining.inherit.sound.start.DataGetUtils
import com.even.zining.inherit.sound.start.FnnStartFun.mainStart
import org.json.JSONObject
import java.util.UUID
import com.even.zining.inherit.sound.start.FnnStartFun
import com.even.zining.inherit.sound.start.newfun.DataStorage
import com.even.zining.inherit.sound.start.newfun.Logger
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.tool.data.MMKVUtils
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.thinkup.core.api.TUAdInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Currency

object TbaPostTool {
    fun showAppVersion(): String {
        val versionName =
            mainStart.packageManager.getPackageInfo(mainStart.packageName, 0).versionName
        return if (versionName.isNullOrEmpty()) "1.0.0" else versionName
    }


    private fun topJsonData(context: Context): JSONObject {
        val is_android = MMKVUtils.getString(FnnLoadData.appiddata)

        val yeomanry = JSONObject().apply {
            //os_version
            put("panicked", Build.VERSION.RELEASE)

        }


        val ass = JSONObject().apply {
            //system_language//假值
            put("railbird", "cevrasd_d")
            //android_id
            put("emacs", is_android)
            //manufacturer
            put("fest", Build.MANUFACTURER)
            //client_ts
            put("scowl", System.currentTimeMillis())
        }

        val seymour = JSONObject().apply {
            //gaid
            put("pie", "")
            //log_id
            put("library", UUID.randomUUID().toString())
            //distinct_id
            put("suffrage", is_android)
            //device_model-最新需要传真实值
            put("anheuser", Build.BRAND)
            //app_version
            put("lackey", showAppVersion())
            //bundle_id
            put("iceman", context.packageName)
        }

        val eire = JSONObject().apply {
            put("nobel", "xxxx")
            //operator 传假值字符串
            put("swain", "778")

            //os
            put("upwind", "pianist")
        }

        val json = JSONObject().apply {
            put("yeomanry", yeomanry)
            put("as", ass)
            put("seymour", seymour)
            put("eire", eire)
        }

        return json
    }

    fun upInstallJson(context: Context): String {
        val is_ref = MMKVUtils.getString(FnnLoadData.refdata)
        return topJsonData(context).apply {
            //build
            put("allemand", "build/${Build.ID}")

            //referrer_url
            put("spear", is_ref)

            //user_agent
            put("imitable", "")

            //lat
            put("diplomat", "mullen")

            //referrer_click_timestamp_seconds
            put("clobber", 0)

            //install_begin_timestamp_seconds
            put("phonetic", 0)

            //referrer_click_timestamp_server_seconds
            put("birgit", 0)

            //install_begin_timestamp_server_seconds
            put("rickety", 0)

            //install_first_seconds
            put("captive", getFirstInstallTime(context))

            //last_update_seconds
            put("sour", 0)
            put("jangle", "yawn")
        }.toString()
    }

    fun upPangleAdJson(context: Context, adValue: PAGInterstitialAd): String {
        val ecpm = try {
            (adValue.pagRevenueInfo?.showEcpm?.cpm?.toDouble() ?: 0.0) * 1000
        } catch (e: Exception) {
            0.0
        }
        return topJsonData(context).apply {
            //ad_pre_ecpm
            put("embolden", ecpm)//tranplus leishi
            //currency
            put("bagging", "USD")
            //ad_network
            put(
                "habeas",
                adValue.pagRevenueInfo?.showEcpm?.adnName
            )
            //ad_source
            put("paraguay", "PangLe")
            //ad_code_id
            put("spoonful", adValue.pagRevenueInfo?.showEcpm?.adUnit)
            //ad_pos_id
            put("tertiary", "int")

            //ad_rit_id
            put("greet", "")
            //ad_sense
            put("solemn", "")
            //ad_format
            put("melamine", adValue.pagRevenueInfo?.showEcpm?.adFormat)
            put("jangle", "earthmen")
        }.toString()
    }

    fun upTopOnAdJson(context: Context, adValue: TUAdInfo): String {
        return topJsonData(context).apply {
            //ad_pre_ecpm
            put("embolden", adValue.publisherRevenue * 1000000)
            //currency
            put("bagging", "USD")
            //ad_network
            put(
                "habeas",
                adValue.networkName
            )
            //ad_source
            put("paraguay", "TopOn")
            //ad_code_id
            put("spoonful", adValue.placementId)
            //ad_pos_id
            put("tertiary", "int")

            //ad_rit_id
            put("greet", "")
            //ad_sense
            put("solemn", "")
            //ad_format
            put("melamine", adValue.format)
            put("jangle", "earthmen")

        }.toString()
    }

    fun upPointJson(name: String): String {
        return topJsonData(mainStart).apply {
            put("jangle", name)
        }.toString()
    }

    fun upPointJson(
        name: String,
        key1: String? = null,
        keyValue1: Any? = null,
        key2: String? = null,
        keyValue2: Any? = null,
    ): String {
        return topJsonData(mainStart).apply {
            put("jangle", name)
            if (key1 != null) {
                put("rand~$key1", keyValue1)
            }
            if (key2 != null) {
                put("rand~$key2", keyValue2)
            }

        }.toString()
    }

    private fun getFirstInstallTime(context: Context): Long {
        try {
            val packageInfo =
                context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.firstInstallTime / 1000
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }


    fun postPangLeAdValue(adValue: PAGInterstitialAd) {
        val ecmVVVV = try {
            (adValue.pagRevenueInfo?.showEcpm?.cpm?.toDouble() ?: 0.0) / (1000.0)
        } catch (e: Exception) {
            0.0
        }
        val adRevenueData = AFAdRevenueData(
            adValue.pagRevenueInfo?.showEcpm?.adnName ?: "",
            MediationNetwork.CUSTOM_MEDIATION,
            "USD",
            ecmVVVV ?: 0.0
        )
        val additionalParameters: MutableMap<String, Any> = HashMap()
        additionalParameters[AdRevenueScheme.AD_UNIT] =
            adValue.pagRevenueInfo?.showEcpm?.adUnit ?: ""
        additionalParameters[AdRevenueScheme.AD_TYPE] = "Interstitial"
        AppsFlyerLib.getInstance().logAdRevenue(adRevenueData, additionalParameters)
        logAdImpressionRevenue(ecmVVVV.toString())
        val jsonBean = DataStorage.getAdminData()
        val fbId = jsonBean?.config?.identifiers?.getOrNull(2)?.tag ?: ""
        if (fbId.isBlank()) {
            return
        }
        if (fbId.isNotEmpty()) {
            if (!FacebookSdk.isInitialized()) {
                DataGetUtils.initFaceBook()
            }
            try {
                AppEventsLogger.newLogger(mainStart).logPurchase(
                    BigDecimal(ecmVVVV.toString()),
                    Currency.getInstance("USD")
                )
            } catch (e: Exception) {
                Logger.showLog("Invalid ecpmPrecision value: ${ecmVVVV}, skipping logPurchase")
            }
        }
    }

    fun postTopOnAdValue(adValue: TUAdInfo) {
        val ecmVVVV = try {
            adValue.publisherRevenue
        } catch (e: NumberFormatException) {
            Logger.showLog("Invalid ecpmPrecision value: ${adValue.ecpmPrecision}, using default value 0.0")
            0.0
        }
        val adRevenueData = AFAdRevenueData(
            adValue.adsourceId,
            MediationNetwork.TOPON,
            "USD",
            ecmVVVV
        )
        val additionalParameters: MutableMap<String, Any> = HashMap()
        additionalParameters[AdRevenueScheme.AD_UNIT] = adValue.placementId
        additionalParameters[AdRevenueScheme.AD_TYPE] = "Interstitial"
        AppsFlyerLib.getInstance().logAdRevenue(adRevenueData, additionalParameters)
        logAdImpressionRevenue(ecmVVVV.toString())
        val jsonBean = DataStorage.getAdminData()
        val fbId = jsonBean?.config?.identifiers?.getOrNull(2)?.tag ?: ""
        if (fbId.isBlank()) {
            return
        }
        if (fbId.isNotEmpty()) {
            if (!FacebookSdk.isInitialized()) {
                DataGetUtils.initFaceBook()
            }
            try {
                AppEventsLogger.newLogger(mainStart).logPurchase(
                    BigDecimal(ecmVVVV.toString()),
                    Currency.getInstance("USD")
                )
            } catch (e: Exception) {
                Logger.showLog("Invalid ecpmPrecision value: ${adValue.ecpmPrecision}, skipping logPurchase")
            }
        }
    }

    private fun logAdImpressionRevenue(adFormat: String) {
        try {
            val firebaseAnalytics = Firebase.analytics
            val params = Bundle().apply {
                putString(FirebaseAnalytics.Param.VALUE, adFormat)
                putString(FirebaseAnalytics.Param.CURRENCY, "USD")
            }
            firebaseAnalytics.logEvent("ad_impression_SystemSentry", params)
        } catch (e: Exception) {
            Log.e("TAG", "Firebase: ${e.message}")
        }

    }

    fun getadmin(userCategory: Int, codeInt: String?) {
        var isuserData: String? = null

        if (codeInt == null) {
            isuserData = null
        } else if (codeInt != "200") {
            isuserData = codeInt
        } else if (userCategory == 1) {
            isuserData = "a"
        } else {
            isuserData = "b"
        }

        NetPostTool.postPointData(true, "getadmin", "getstring", isuserData)
    }


    fun showsuccessPoint() {
        val time = (System.currentTimeMillis() - FnnStartFun.showAdTime) / 1000
        NetPostTool.postPointData(false, "show", "t", time)
        FnnStartFun.showAdTime = 0
    }

    fun firstExternalBombPoint() {
        if (MMKVUtils.getBoolean(FnnLoadData.firstPoint)) {
            return
        }
        val instalTime = PngCanGo.getInstallTimeDataFun()
        NetPostTool.postPointData(false, "first_start", "time", instalTime)
        MMKVUtils.put(FnnLoadData.firstPoint, true)
    }

    fun pointInstallAf(data: String) {
        val keyIsAdOrg = MMKVUtils.getBoolean(FnnLoadData.adOrgPoint)
        if (data.contains("non_organic", true) && !keyIsAdOrg) {
            NetPostTool.postPointData(false, "non_organic")
            MMKVUtils.put(FnnLoadData.adOrgPoint, true)
        }
    }

    fun getLiMitData() {
        val getlimitState = MMKVUtils.getBoolean(FnnLoadData.getlimit)
        if (!getlimitState) {
            NetPostTool.postPointData(true, "getlimit")
            MMKVUtils.put(FnnLoadData.getlimit, true)
        }
    }

    fun sessionUp() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                NetPostTool.postPointData(false, "session_up")
                delay(1000 * 60 * 15)
            }
        }
    }
}