package com.even.zining.inherit.sound.tool

import android.content.Context
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAd
import com.even.zining.inherit.sound.tool.TbaPostTool.upInstallJson
import com.even.zining.inherit.sound.tool.TbaPostTool.upPointJson
import com.even.zining.inherit.sound.start.FnnStartFun
import com.even.zining.inherit.sound.start.FnnStartFun.canIntNextFun
import com.even.zining.inherit.sound.start.FnnStartFun.mainStart
import com.even.zining.inherit.sound.start.DataGetUtils
import com.even.zining.inherit.sound.start.FnnStartFun.isDigitSumEven
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
            FnnStartFun.showLog("无数据启动请求:")
            executeWithRetry(
                maxRetries = 5,
                minDelay = 10_000L,
                maxDelay = 40_000L
            ) { attempt ->
                try {
                    val result = executeAdminRequestSuspend()
                    val bean = FnnStartFun.getAdminData()
                    FnnStartFun.showLog("admin-data-1: $result")
                    if (bean != null && !bean.config.user.isUploader.isDigitSumEven()) {
                        FnnStartFun.showLog("不是A用户，进行重试")
                        bPostAdmin()
                    }
                    if (bean?.config?.user?.isUploader.isDigitSumEven()) {
                        canIntNextFun()
                    }
                    Result.Success(Unit)
                } catch (e: Exception) {
                    handleError(5, "Admin", e.message ?: "", attempt)
                    Result.Failure(e)
                }
            }
        }
    }

    fun twoPostAdmin() {
        FnnStartFun.showLog("有数据启动请求:")
        scope.launch {
            val bean = FnnStartFun.getAdminData()
            val delay = Random.nextLong(1000, 10 * 60 * 1000)
            var isStart = false

            if (bean != null && bean.config.user.isUploader.isDigitSumEven()) {
                isStart = true
                canIntNextFun()
                FnnStartFun.showLog("冷启动app延迟 ${delay}ms 请求admin数据")
                delay(delay)
            }
            executeWithRetry(
                maxRetries = 4,
                minDelay = 10_000L,
                maxDelay = 40_000L
            ) { attempt ->
                try {
                    val result = executeAdminRequestSuspend()
                    val bean = FnnStartFun.getAdminData()
                    FnnStartFun.showLog("admin-data-2: $result")
                    if (bean != null && !bean.config.user.isUploader.isDigitSumEven()) {
                        FnnStartFun.showLog("不是A用户，进行重试")
                        bPostAdmin()
                    }
                    if (bean?.config?.user?.isUploader.isDigitSumEven() && !isStart) {
                        canIntNextFun()
                    }
                    Result.Success(Unit)
                } catch (e: Exception) {
                    handleError(3, "Admin", e.message ?: "", attempt)
                    Result.Failure(e)
                }
            }
        }
    }

    private fun bPostAdmin() {
        scope.launch {
            delay(50_000L)
            executeWithRetry(
                maxRetries = 20,
                minDelay = 59_000L,
                maxDelay = 60_000L
            ) { attempt ->
                try {
                    val result = executeAdminRequestSuspend()
                    val bean = FnnStartFun.getAdminData()
                    FnnStartFun.showLog("admin-onSuccess: $result")
                    if (bean != null && !bean.config.user.isUploader.isDigitSumEven()) {
                        handleError(20, "不是A用户，进行重试", "", attempt)
                        Result.Failure(Exception())
                    } else {
                        canIntNextFun()
                        Result.Success(Unit)
                    }
                } catch (e: Exception) {
                    handleError(20, "Admin", e.message ?: "", attempt)
                    Result.Failure(e)
                }
            }
        }
    }

    fun postInstallData(context: Context) {
        scope.launch {
            val data = withContext(Dispatchers.Default) {
                MMKVUtils.getString(FnnLoadData.IS_INT_JSON).ifEmpty {
                    val newData = upInstallJson(context)
                    MMKVUtils.put(FnnLoadData.IS_INT_JSON,newData)
                    newData
                }
            }

            FnnStartFun.showLog("Install: data=$data")

            // 执行带重试机制的请求
            executeWithRetry(
                maxRetries = 20,
                minDelay = 10_000L,
                maxDelay = 40_000L
            ) { attempt ->
                try {
                    val result = executePutRequestSuspend(data)
                    handleSuccess("Install", result)
                    MMKVUtils.put(FnnLoadData.IS_INT_JSON,"")
                    Result.Success(Unit)
                } catch (e: Exception) {
                    handleError(20, "Install", e.message ?: "", attempt)
                    Result.Failure(e)
                }
            }
        }
    }

    fun postPangleAdData(adValue: PAGInterstitialAd) {
        scope.launch {
            // 准备请求数据
            val data = upPangleAdJson(mainStart, adValue)
            FnnStartFun.showLog("postAd: data=$data")
            // 执行带重试机制的请求
            executeWithRetry(
                maxRetries = 20,
                minDelay = 10_000L,
                maxDelay = 40_000L
            ) { attempt ->
                try {
                    val result = executePutRequestSuspend(data)
                    handleSuccess("Ad", result)
                    Result.Success(Unit)
                } catch (e: Exception) {
                    handleError(3, "Ad", e.message ?: "", attempt)
                    Result.Failure(e)
                }
            }
            TbaPostTool.postPangLeAdValue(adValue)
        }
    }

    fun postTopOnAdData(adValue: TUAdInfo) {
        scope.launch {
            // 准备请求数据
            val data = upTopOnAdJson(mainStart, adValue)
            FnnStartFun.showLog("postAd: data=$data")
            // 执行带重试机制的请求
            executeWithRetry(
                maxRetries = 20,
                minDelay = 10_000L,
                maxDelay = 40_000L
            ) { attempt ->
                try {
                    val result = executePutRequestSuspend(data)
                    handleSuccess("Ad", result)
                    Result.Success(Unit)
                } catch (e: Exception) {
                    handleError(3, "Ad", e.message ?: "", attempt)
                    Result.Failure(e)
                }
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
            val adminBean = FnnStartFun.getAdminData()
            if (!isAdMinCon && (adminBean != null && adminBean.config.user.level!=1)) {
                return@launch
            }
            // 准备请求数据
            val data = if (key1 != null) {
                upPointJson(name, key1, keyValue1, key2, keyValue2)
            } else {
                upPointJson(name)
            }
            FnnStartFun.showLog("Point-${name}-开始打点--${data}")
            // 执行带重试机制的请求
            val maxNum = if (isAdMinCon) {
                20
            } else {
                3
            }
            executeWithRetry(
                maxRetries = maxNum,
                minDelay = 10_000L,
                maxDelay = 40_000L
            ) { attempt ->
                try {
                    val result = executePutRequestSuspend(data)
                    handleSuccess("Point-${name}", result)
                    Result.Success(Unit)
                } catch (e: Exception) {
                    handleError(maxNum, "Point-${name}", e.message ?: "", attempt)
                    Result.Failure(e)
                }
            }
        }
    }

    // 带随机延迟的重试执行器
    private suspend fun <T> executeWithRetry(
        maxRetries: Int,
        minDelay: Long,
        maxDelay: Long,
        block: suspend (attempt: Int) -> Result<T>
    ) {
        repeat(maxRetries) { attempt ->
            when (val result = block(attempt)) {
                is Result.Success -> return
                is Result.Failure -> {
                    val delayTime = Random.nextLong(minDelay, maxDelay)
                    delay(delayTime)
                }
            }
        }
    }

    private suspend fun executeAdminRequestSuspend(): String {
        return suspendCancellableCoroutine { continuation ->
            CoroutineScope(Dispatchers.IO).launch {
                val result = DataGetUtils.executeAdminRequest()
                if (result.isSuccess) {
                    continuation.resume(result.getOrNull() ?: "")

                } else {
                    continuation.resumeWithException(Exception(result.getOrNull() ?: ""))

                }
            }
        }
    }

    // 挂起函数扩展
    private suspend fun executePutRequestSuspend(data: String): String {
        return suspendCancellableCoroutine { continuation ->
            CoroutineScope(Dispatchers.IO).launch {
                val result = DataGetUtils.executePutRequest(data)
                if (result.isSuccess) {
                    continuation.resume(result.getOrNull() ?: "")

                } else {
                    continuation.resumeWithException(Exception(result.getOrNull() ?: ""))
                }
            }
        }
    }

    // 处理成功响应
    private fun handleSuccess(type: String, result: String) {
        FnnStartFun.showLog("${type}-请求成功: $result")
    }

    // 处理错误日志
    private fun handleError(maxNum: Int, type: String, e: String, attempt: Int) {
        FnnStartFun.showLog(
            """
        ${type}-请求失败[重试次数:${attempt + 1}]: ${e}
        ${if (attempt >= maxNum - 1) "达到最大重试次数" else ""}
    """.trimIndent()
        )
    }

}