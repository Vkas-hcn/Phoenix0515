package com.even.zining.inherit.sound.start

import android.annotation.SuppressLint
import android.util.Base64
import android.util.Log
import com.even.zining.inherit.sound.start.FnnStartFun.isDigitSumEven
import com.even.zining.inherit.sound.start.FnnStartFun.mainStart
import com.even.zining.inherit.sound.tool.data.FnnBean
import com.even.zining.inherit.sound.tool.TbaPostTool
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.tool.data.MMKVUtils
import com.even.zining.inherit.sound.tool.NetPostTool
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

object DataGetUtils {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun executeAdminRequest(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val requestData = prepareRequestData()
                FnnStartFun.showLog("executeAdminRequest=$requestData")
                NetPostTool.postPointData(false, "reqadmin")

                val (processedData, dt) = processRequestData(requestData)
                val targetUrl = FnnLoadData.getConfig().adminUrl
                Log.e(
                    "TAG",
                    "SanZong.ADMIN_URL=${FnnStartFun.mustXS}=: $targetUrl",
                )

                val request = Request.Builder()
                    .url(targetUrl)
                    .post(RequestBody.create("application/json".toMediaTypeOrNull(), processedData))
                    .addHeader("dt", dt)
                    .build()

                val response = client.newCall(request).execute()
               handleAdminResponse(response)
            } catch (e: SocketTimeoutException) {
                Result.failure(Exception("Request timed out: ${e.message}"))
            } catch (e: Exception) {
                Result.failure(Exception("Operation failed: ${e.message}"))
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun prepareRequestData(): String {
        return JSONObject().apply {
            put("BiTEfhQ", "com.pubilsph.informationchek")
            put("FxUZibbMd", MMKVUtils.getString(FnnLoadData.appiddata))
            put("fJSH", MMKVUtils.getString(FnnLoadData.refdata))
//            put("fJSH", "fb4a")
            put("iMtdGa", TbaPostTool.showAppVersion())
        }.toString()
    }

    private fun processRequestData(rawData: String): Pair<String, String> {
        val dt = System.currentTimeMillis().toString()
        val encrypted = xorEncrypt(rawData, dt)
        return Base64.encodeToString(encrypted.toByteArray(), Base64.NO_WRAP) to dt
    }


    suspend fun executePutRequest(body: Any): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // 准备请求数据
                val jsonBodyString = JSONObject(body.toString()).toString()
                val targetUrl = FnnLoadData.getConfig().upUrl

                // 配置连接
                val request = Request.Builder()
                    .url(targetUrl)
                    .post(RequestBody.create("application/json".toMediaTypeOrNull(), jsonBodyString))
                    .build()

                // 发送请求
                val response = client.newCall(request).execute()

                // 处理响应
                handlePutResponse(response)
            } catch (e: Exception) {
                Result.failure(Exception("Put request failed: ${e.message}"))
            }
        }
    }

    private fun handlePutResponse(response: Response): Result<String> {
        return try {
            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP error: ${response.code}"))
            }

            val responseString = response.body?.string() ?: throw IllegalArgumentException("Response body is null")
            Result.success(responseString)
        } catch (e: Exception) {
            Result.failure(Exception("Response processing failed: ${e.message}"))
        } finally {
            response.close()
        }
    }

    fun handleAdminResponse(response: Response): Result<String> {
        return try {
            if (response.code != 200) {
                TbaPostTool.getadmin(11, response.code.toString())
                return Result.failure(Exception("HTTP error: ${response.code}"))
            }
            val responseString =
                response.body?.string() ?: throw IllegalArgumentException("Response body is null")
            val dt = response.header("dt")
                ?: throw IllegalArgumentException("Missing dt header")

            // 解密处理
            val decodedBytes = Base64.decode(responseString, Base64.DEFAULT)
            val decodedStr = String(decodedBytes, StandardCharsets.UTF_8)
            val finalData = xorEncrypt(decodedStr, dt)

            // 解析数据
            val jsonResponse = JSONObject(finalData)
            val jsonData = parseAdminRefData(jsonResponse.toString())
            val adminBean = runCatching {
                Gson().fromJson(jsonData, FnnBean::class.java)
            }.getOrNull()

            when {
                adminBean == null -> {
                    TbaPostTool.getadmin(7, null)
                    Result.failure(Exception("Invalid response format"))
                }

                FnnStartFun.getAdminData() == null -> {
                    FnnStartFun.putAdminData(jsonData)
                    val code = when {
                        adminBean.config.user.isUploader.isDigitSumEven() -> 1
                        else -> 2
                    }
                    TbaPostTool.getadmin(code, response.code.toString())
                    Result.success(jsonData)
                }

                adminBean.config.user.isUploader.isDigitSumEven() -> {
                    FnnStartFun.putAdminData(jsonData)
                    TbaPostTool.getadmin(1, response.code.toString())
                    Result.success(jsonData)
                }

                else -> {
                    TbaPostTool.getadmin(2, response.code.toString())
                    Result.success(jsonData)
                }
            }

        } catch (e: Exception) {
            TbaPostTool.getadmin(3, "parse_error")
            Result.failure(e)
        } finally {
            response.close()
        }
    }

    private fun xorEncrypt(text: String, dt: String): String {
        val cycleKey = dt.toCharArray()
        val keyLength = cycleKey.size
        return text.mapIndexed { index, char ->
            char.toInt().xor(cycleKey[index % keyLength].toInt()).toChar()
        }.joinToString("")
    }

    private fun parseAdminRefData(jsonString: String): String {
        return try {
            JSONObject(jsonString).getJSONObject("wcmA").getString("conf")
        } catch (e: Exception) {
            ""
        }
    }

    fun initFaceBook() {
        runCatching {
            if(FacebookSdk.isInitialized()){
                return
            }
            val jsonBean = FnnStartFun.getAdminData()?:return
            val data = jsonBean.config.identifiers.getOrNull(2)?.tag ?: ""
            if (data.isNullOrBlank()) {
                return
            }
            FnnStartFun.showLog("initFaceBook: ${data}")
            FacebookSdk.setApplicationId(data)
            FacebookSdk.sdkInitialize(mainStart)
            AppEventsLogger.activateApp(mainStart)
        }

    }
}
