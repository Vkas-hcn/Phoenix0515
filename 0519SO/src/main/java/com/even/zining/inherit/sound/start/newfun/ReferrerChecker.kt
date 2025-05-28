package com.even.zining.inherit.sound.start.newfun
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.even.zining.inherit.sound.start.FnnStartFun.mainStart
import com.even.zining.inherit.sound.start.newfun.AdDataProcessor.startOneTimeAdminData
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.tool.data.MMKVUtils
import com.even.zining.inherit.sound.tool.NetPostTool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ReferrerChecker {
    fun launchRefData() {
        val refData = MMKVUtils.getString(FnnLoadData.refdata)
        val intJson = MMKVUtils.getString(FnnLoadData.IS_INT_JSON)
        if (refData.isNotEmpty()) {
            startOneTimeAdminData()
            intJson.takeIf { it.isNotEmpty() }?.let {
                NetPostTool.postInstallDataSimplified()
            }
            return
        }
        Logger.showLog("launchRefData=$refData")
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
            Logger.showLog("Failed to fetch referrer: ${e.message}")
        }
    }

    private fun handleReferrerSetup(responseCode: Int, referrerClient: InstallReferrerClient) {
        when (responseCode) {
            InstallReferrerClient.InstallReferrerResponse.OK -> {
                val installReferrer = referrerClient.installReferrer.installReferrer
                if (installReferrer.isNotEmpty()) {
                    MMKVUtils.put(FnnLoadData.refdata, installReferrer)
                    NetPostTool.postInstallDataSimplified()
                    startOneTimeAdminData()
                }
                Logger.showLog("Referrer  data: ${installReferrer}")
            }

            else -> {
                Logger.showLog("Failed to setup referrer: $responseCode")
            }
        }

        kotlin.runCatching {
            referrerClient.endConnection()
        }
    }
}
