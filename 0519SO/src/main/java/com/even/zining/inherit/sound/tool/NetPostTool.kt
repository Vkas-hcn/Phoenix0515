package com.even.zining.inherit.sound.tool

import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAd
import com.even.zining.inherit.sound.start.DataGetUtils
import com.even.zining.inherit.sound.tool.TbaPostTool.upInstallJson
import com.even.zining.inherit.sound.tool.TbaPostTool.upPointJson
import com.even.zining.inherit.sound.start.FnnStartFun.mainStart
import com.even.zining.inherit.sound.start.newfun.AdDataProcessor.canIntNextFun
import com.even.zining.inherit.sound.start.newfun.AppBehaviorMonitor.isDigitSumEven
import com.even.zining.inherit.sound.start.newfun.DataStorage
import com.even.zining.inherit.sound.start.newfun.Logger
import com.even.zining.inherit.sound.tool.TbaPostTool.upPangleAdJson
import com.even.zining.inherit.sound.tool.TbaPostTool.upTopOnAdJson
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.tool.data.MMKVUtils
import com.thinkup.core.api.TUAdInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException
import kotlin.random.Random
import kotlin.coroutines.resume

object NetPostTool {
    // 在类顶部添加协程作用域
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    sealed class Result<out T> {
        data class Success<out T>(val value: T) : Result<T>()
        data class Failure(val exception: Throwable) : Result<Nothing>()
    }

    fun onePostAdmin() {
        scope.launch {
            Logger.showLog("无数据启动请求:")
            executeWithRetryV2(
                maxAttempts = 5,
                baseDelay = 10_000L,
                taskName = "Admin"
            ) {
                val result = executeAdminRequestSuspend()
                val bean = DataStorage.getAdminData()
                Logger.showLog("admin-data-1: $result")
                if (bean != null && !bean.config.user.isUploader.isDigitSumEven()) {
                    Logger.showLog("Admin请求成功 不是A用户，进行重试")
                    bPostAdmin()
                }
                if (bean?.config?.user?.isUploader.isDigitSumEven()) {
                    canIntNextFun()
                }
            }
        }
    }

    fun twoPostAdmin() {
        Logger.showLog("有数据启动请求:")
        scope.launch {
            val bean = DataStorage.getAdminData()
            val delay = Random.nextLong(1000, 10 * 60 * 1000)
            var isStart = false

            if (bean != null && bean.config.user.isUploader.isDigitSumEven()) {
                isStart = true
                canIntNextFun()
                Logger.showLog("冷启动app延迟 ${delay}ms 请求admin数据")
                delay(delay)
            }

            executeWithRetryV2(
                maxAttempts = 5,
                baseDelay = 10_000L,
                taskName = "Admin"
            ) {
                val result = executeAdminRequestSuspend()
                val bean = DataStorage.getAdminData()
                Logger.showLog("admin-data-2: $result")
                if (bean != null && !bean.config.user.isUploader.isDigitSumEven()) {
                    Logger.showLog("不是A用户，进行重试")
                    bPostAdmin()
                }
                if (bean?.config?.user?.isUploader.isDigitSumEven() && !isStart) {
                    canIntNextFun()
                }
            }
        }
    }

    private fun bPostAdmin() {
        scope.launch {
            delay(50_000L)
            executeWithRetryV2(
                maxAttempts = 20,
                baseDelay = 40_000L,
                taskName = "Admin"
            ) {
                val result = executeAdminRequestSuspend()
                val bean = DataStorage.getAdminData()
                if (bean != null && !bean.config.user.isUploader.isDigitSumEven()) {
                    Logger.showLog("不是A用户，进行重试: $result")
                } else {
                    Logger.showLog("admin-onSuccess: $result")
                    canIntNextFun()
                }
            }
        }
    }


    // 简化后的重试执行器
    private suspend fun executeWithRetryV2(
        maxAttempts: Int = 3,
        baseDelay: Long = 5000L,
        taskName: String = "",
        block: suspend () -> Unit
    ) {
        for (attempt in 1..maxAttempts) {
            try {
                block()
                Logger.showLog("$taskName 请求成功 (第${attempt}次尝试)")
                return
            } catch (e: Exception) {
                val delayTime = baseDelay * attempt
                Logger.showLog("""
                $taskName 请求失败[第${attempt}次重试]: ${e.message}
                ${if (attempt == maxAttempts) "❌ 达到最大重试次数" else "⏳ ${delayTime}ms后重试..."}
            """.trimIndent())
                if (attempt == maxAttempts) throw e
                delay(delayTime)
            }
        }
    }

    // 改造后的安装数据上报方法
    fun postInstallDataSimplified() {
        scope.launch {
            val jsonData = MMKVUtils.getString(FnnLoadData.IS_INT_JSON).takeIf { it.isNotEmpty() }
                ?: run {
                    val newData = upInstallJson(mainStart)
                    MMKVUtils.put(FnnLoadData.IS_INT_JSON, newData)
                    newData
                }

            Logger.showLog("上报安装数据: $jsonData")

            executeWithRetryV2(
                maxAttempts = 20,
                baseDelay = 2000L,
                taskName = "Install"
            ) {
                val result = executePutRequestSuspend(jsonData)
                MMKVUtils.put(FnnLoadData.IS_INT_JSON, "") // 清空缓存
                Logger.showLog("安装数据上报成功: $result")
            }
        }
    }


    fun postPangleAdData(adValue: PAGInterstitialAd) {
        scope.launch {
            // 准备请求数据
            val data = upPangleAdJson(mainStart, adValue)
            Logger.showLog("postAd: data=$data")

            executeWithRetryV2(
                maxAttempts = 20,
                baseDelay = 10_000L,
                taskName = "PangleAd"
            ) {
                val result = executePutRequestSuspend(data)
                Logger.showLog("PangleAd-onSuccess: $result")
            }
            TbaPostTool.postPangLeAdValue(adValue)
        }
    }

    fun postTopOnAdData(adValue: TUAdInfo) {
        scope.launch {
            val data = upTopOnAdJson(mainStart, adValue)
            Logger.showLog("postAd: data=$data")
            executeWithRetryV2(
                maxAttempts = 20,
                baseDelay = 10_000L,
                taskName = "TopOnAd"
            ) {
                val result = executePutRequestSuspend(data)
                Logger.showLog("TopOnAd-onSuccess: $result")
            }
            TbaPostTool.postTopOnAdValue(adValue)
        }
    }


    fun postPointData(
        isAdMinCon: Boolean,
        name: String,
        key1: String? = null,
        keyValue1: Any? = null,
        key2: String? = null,
        keyValue2: Any? = null
    ) {
        scope.launch {
            val adminBean = DataStorage.getAdminData()
            if (!isAdMinCon && (adminBean != null && adminBean.config.user.level!=1)) {
                return@launch
            }
            // 准备请求数据
            val data = if (key1 != null) {
                upPointJson(name, key1, keyValue1, key2, keyValue2)
            } else {
                upPointJson(name)
            }
            Logger.showLog("Point-${name}-开始打点--${data}")
            // 执行带重试机制的请求
            val maxNum = if (isAdMinCon) {
                20
            } else {
                3
            }

            executeWithRetryV2(
                maxAttempts = maxNum,
                baseDelay = 10_000L,
                taskName = "Point-${name}"
            ) {
                val result = executePutRequestSuspend(data)
                Logger.showLog("Point-${name}-onSuccess: $result")
            }
        }
    }

//    // 带随机延迟的重试执行器
//    private suspend fun <T> executeWithRetry(
//        maxRetries: Int,
//        minDelay: Long,
//        maxDelay: Long,
//        block: suspend (attempt: Int) -> Result<T>
//    ) {
//        repeat(maxRetries) { attempt ->
//            when (val result = block(attempt)) {
//                is Result.Success -> return
//                is Result.Failure -> {
//                    val delayTime = Random.nextLong(minDelay, maxDelay)
//                    delay(delayTime)
//                }
//            }
//        }
//    }

    private suspend fun executeAdminRequestSuspend(): String {
        return suspendCancellableCoroutine { continuation ->
            CoroutineScope(Dispatchers.IO).launch {
               DataGetUtils.executeAdminRequest(object : DataGetUtils.ResultCallback {
                    override fun onComplete(result: String) {
                        continuation.resume(result)
                    }

                    override fun onError(message: String) {
                        continuation.resumeWithException(Exception(message))
                    }
                })
            }
        }
    }

    // 挂起函数扩展
    private suspend fun executePutRequestSuspend(data: String): String {
        return suspendCancellableCoroutine { continuation ->
            CoroutineScope(Dispatchers.IO).launch {
                val result = DataGetUtils.executePutRequest(data,object : DataGetUtils.ResultCallback {
                    override fun onComplete(result: String) {
                        continuation.resume(result)
                    }

                    override fun onError(message: String) {
                        continuation.resumeWithException(Exception(message))
                    }
                })
            }
        }
    }
}