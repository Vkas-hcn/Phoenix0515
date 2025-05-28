package com.even.zining.inherit.sound.start


import android.annotation.SuppressLint
import android.util.Base64
import android.util.Log
import com.even.zining.inherit.sound.start.FnnStartFun.mainStart
import com.even.zining.inherit.sound.start.newfun.AppBehaviorMonitor.isDigitSumEven
import com.even.zining.inherit.sound.start.newfun.DataStorage
import com.even.zining.inherit.sound.start.newfun.Logger
import com.even.zining.inherit.sound.tool.NetPostTool
import com.even.zining.inherit.sound.tool.TbaPostTool
import com.even.zining.inherit.sound.tool.data.FnnBean
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.tool.data.MMKVUtils
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.gson.Gson
import org.json.JSONObject

import java.nio.charset.StandardCharsets

import java.io.BufferedInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.Executors

object DataGetUtils {
    interface ResultCallback {
        fun onComplete(result: String)
        fun onError(message: String)
    }

    private val threadPool = Executors.newFixedThreadPool(4)

    fun executeAdminRequest(callback: ResultCallback) {
        val requestData = JSONObject().apply {
            put("BiTEfhQ", "com.pubilsph.informationchek")
            put("FxUZibbMd", MMKVUtils.getString(FnnLoadData.appiddata))
            put("fJSH", MMKVUtils.getString(FnnLoadData.refdata))
            put("iMtdGa", TbaPostTool.showAppVersion())
        }.toString()
        Logger.showLog("executeAdminRequest=$requestData")
        NetPostTool.postPointData(false, "reqadmin")
        threadPool.execute {
            var connection: HttpURLConnection? = null
            try {
                val (processedData, datetime) = processRequestData(requestData)
                val targetUrl = URL(FnnLoadData.getConfig().adminUrl)
                Log.e(
                    "TAG",
                    "ADMIN_URL=: ${FnnLoadData.getConfig().adminUrl}",
                )
                connection = targetUrl.openConnection() as HttpURLConnection
                configureConnection(connection).apply {
                    setRequestProperty("datetime", datetime)
                    doOutput = true
                }

                connection.outputStream.use { os ->
                    os.write(processedData.toByteArray(StandardCharsets.UTF_8))
                }

                handleAdminResponse(connection, callback)
            }  catch (e: SocketTimeoutException) {
                callback.onError("Request timed out: ${e.message}")
                TbaPostTool.getadmin(12, "timeout")
            } catch (e: Exception) {
                callback.onError("Operation failed: ${e.message}")
                TbaPostTool.getadmin(13, "timeout")
            }finally {
                connection?.disconnect()
            }
        }
    }

    private fun processRequestData(rawData: String): Pair<String, String> {
        val datetime = System.currentTimeMillis().toString()
        val encrypted = xorEncrypt(rawData, datetime)
        return Base64.encodeToString(encrypted.toByteArray(), Base64.NO_WRAP) to datetime
    }

    private fun configureConnection(conn: HttpURLConnection): HttpURLConnection {
        conn.apply {
            connectTimeout = 60000
            readTimeout = 60000
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
        }
        return conn
    }

    private fun handleAdminResponse(conn: HttpURLConnection, callback: ResultCallback) {
        try {
            val statusCode = conn.responseCode
            if (statusCode != 200) {
                callback.onError("HTTP error: $statusCode")
                TbaPostTool.getadmin(11, statusCode.toString())
                return
            }

            BufferedInputStream(conn.inputStream).use { bis ->
                val responseString = InputStreamReader(bis).readText()
                val datetime = conn.getHeaderField("datetime")
                    ?: throw IllegalArgumentException("Missing datetime header")

                // 解密处理
                val decodedBytes = Base64.decode(responseString, Base64.DEFAULT)
                val decodedStr = String(decodedBytes, StandardCharsets.UTF_8)
                val finalData = xorEncrypt(decodedStr, datetime)

                // 解析数据
                val jsonResponse = JSONObject(finalData)
                val jsonData = parseAdminRefData(jsonResponse.toString())

                val adminBean = runCatching {
                    Gson().fromJson(jsonData, FnnBean::class.java)
                }.getOrNull()

                when {
                    adminBean == null -> {
                        TbaPostTool.getadmin(7, null)
                        callback.onError("Invalid response format")
                    }

                    DataStorage.getAdminData() == null -> {
                        DataStorage.putAdminData(jsonData)
                        val code = when {
                            adminBean.config.user.isUploader.isDigitSumEven() -> 1
                            else -> 2
                        }
                        TbaPostTool.getadmin(code, statusCode.toString())
                        callback.onComplete(jsonData)
                    }

                    adminBean.config.user.isUploader.isDigitSumEven() -> {
                        DataStorage.putAdminData(jsonData)
                        TbaPostTool.getadmin(1, statusCode.toString())
                        callback.onComplete(jsonData)
                    }

                    else -> {

                        TbaPostTool.getadmin(2, statusCode.toString())
                        callback.onComplete(jsonData)
                    }
                }
            }
        } catch (e: Exception) {
            callback.onError("Response processing failed: ${e.message}")
            TbaPostTool.getadmin(3, "parse_error")
        }
    }

    // 添加加解密方法（需与原有实现保持一致）
    private fun xorEncrypt(text: String, datetime: String): String {
        val cycleKey = datetime.toCharArray()
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

    fun executePutRequest(body: Any, callback: ResultCallback) {
        threadPool.execute {
            var connection: HttpURLConnection? = null
            try {
                // 准备请求数据
                val jsonBodyString = JSONObject(body.toString()).toString()
                val targetUrl = URL(FnnLoadData.getConfig().upUrl)
                // 配置连接
                connection = targetUrl.openConnection() as HttpURLConnection
                configureConnection(connection).apply {
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }

                // 发送请求
                connection.outputStream.use { os ->
                    os.write(jsonBodyString.toByteArray(StandardCharsets.UTF_8))
                }

                // 处理响应
                try {
                    handlePutResponse(connection, callback)
                } catch (e: Exception) {
                    callback.onError("Put request failed: ${e.message}")
                }
            } catch (e: Exception) {
                callback.onError("Put request failed: ${e.message}")
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun handlePutResponse(conn: HttpURLConnection, callback: ResultCallback) {
        try {
            val statusCode = conn.responseCode
            if (statusCode !=200) {
                callback.onError("HTTP error: $statusCode")
                return
            }

            BufferedInputStream(conn.inputStream).use { bis ->
                val responseString = InputStreamReader(bis).readText()
                callback.onComplete(responseString)
            }
        } catch (e: Exception) {
            callback.onError("Response processing failed: ${e.message}")
        }
    }
    fun initFaceBook() {
        runCatching {
            if(FacebookSdk.isInitialized()){
                return
            }
            val jsonBean = DataStorage.getAdminData()?:return
            val data = jsonBean.config.identifiers.getOrNull(2)?.tag ?: ""
            if (data.isNullOrBlank()) {
                return
            }
            Logger.showLog("initFaceBook: ${data}")
            FacebookSdk.setApplicationId(data)
            FacebookSdk.sdkInitialize(mainStart)
            AppEventsLogger.activateApp(mainStart)
        }

    }
}



