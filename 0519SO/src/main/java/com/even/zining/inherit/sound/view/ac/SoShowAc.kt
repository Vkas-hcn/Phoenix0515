package com.even.zining.inherit.sound.view.ac


import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdInteractionCallback
import com.bytedance.sdk.openadsdk.api.model.PAGErrorModel
import com.even.zining.inherit.sound.pangle.AdTool
import com.even.zining.inherit.sound.pangle.XianUtils
import com.even.zining.inherit.sound.tool.*
import com.even.zining.inherit.sound.start.FnnStartFun
import com.even.zining.inherit.sound.start.newfun.DataStorage
import com.even.zining.inherit.sound.start.newfun.Logger
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.tool.data.MMKVUtils
import com.even.zining.inherit.sound.znet.lo.FnnA
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class SoShowAc : AppCompatActivity(), AdDisplayHandler {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.showLog("BroKaActivity onCreate")

        MMKVUtils.put(FnnLoadData.isAdFailCount, 0)
        FnnA.aKig(this)

        initializeViewModel()
        onBackPressedDispatcher.addCallback {}
    }

    private fun initializeViewModel() {
        TbaPostTool.firstExternalBombPoint()

        val delayDuration = generateRandomDelay()
        NetPostTool.postPointData(false, "starup", "time", delayDuration / 1000)

        lifecycleScope.launch {
            delay(delayDuration)
            showAd()
        }
    }

    override fun showAd() {
        if (AdTool.isPangle) {
            showPangleAd()
        } else {
            showTopOnAd()
        }
    }

    private fun showPangleAd() {
        val ad = AdTool.interstitialAd ?: return finish()

        ad.setAdInteractionCallback(object : PAGInterstitialAdInteractionCallback() {
            override fun onAdShowed() {
                handleAdShowed()
                ad?.let { NetPostTool.postPangleAdData(it) }
            }

            override fun onAdClicked() {
                handleAdClicked("pangle")
            }

            override fun onAdDismissed() {
                handleAdClosed("pangle")
                PngCanGo.closeAllActivities()
            }

            override fun onAdShowFailed(@NonNull pagErrorModel: PAGErrorModel) {
                handleAdShowFailed("pangle", pagErrorModel.errorMessage)
            }
        })

        NetPostTool.postPointData(false, "isready")
        ad.show(this)
        startAdDurationCheck(delayDuration = 30_000)
    }

    private fun showTopOnAd() {
        if (!AdTool.intTonOnAd.isAdReady) return finish()

        AdTool.intTonOnAd.show(this)
        startAdDurationCheck(delayDuration = 30_000)
    }

    override fun handleAdShowed() {
        FnnStartFun.adShowTime = System.currentTimeMillis()
        Logger.showLog("体外广告 -pangle: 广告展示")
        XianUtils.recordAdShown()
        AdTool.resetAdStatus()
        NetPostTool.postPointData(false, "delaytime", "time", generateRandomDelay() / 1000)
        FnnStartFun.showAdTime = System.currentTimeMillis()
    }

    override fun handleAdClicked(platform: String) {
        Logger.showLog("体外广告onAdClicked-$platform: 广告被点击")
        XianUtils.recordAdClick()
    }

    override fun handleAdClosed(platform: String) {
        Logger.showLog("体外广告onAdClosed-$platform: 广告被关闭")
        Logger.showLog("closeAllActivities-$platform")
    }

    override fun handleAdShowFailed(platform: String, errorMsg: String?) {
        FnnStartFun.adShowTime = 0
        AdTool.resetAdStatus()
        Logger.showLog("体外广告onAdClosed-$platform: 广告展示失败")
        NetPostTool.postPointData(false, "showfailer", "string3", errorMsg ?: "unknown")
    }

    private fun startAdDurationCheck(delayDuration: Long) {
        lifecycleScope.launch {
            delay(delayDuration)
            if (FnnStartFun.showAdTime > 0) {
                NetPostTool.postPointData(false, "show", "t", "30")
                FnnStartFun.showAdTime = 0
            }
        }
    }

    private fun generateRandomDelay(): Long {
        val adminData = DataStorage.getAdminData() ?: return Random.nextLong(2000, 3000 + 1)

        return adminData.config.delayRange?.let { range ->
            try {
                val (min, max) = range.split("-").mapNotNull { it.toLongOrNull() }
                if (min <= max && max >= 0) {
                    Random.nextLong(min, max + 1)
                } else {
                    fallbackRandomDelay()
                }
            } catch (e: Exception) {
                fallbackRandomDelay().also {
                    Logger.showLog("generateRandomDelay error: ${e.message}")
                }
            }
        } ?: fallbackRandomDelay()
    }

    private fun fallbackRandomDelay() = Random.nextLong(2000, 3000 + 1)

    override fun onDestroy() {
        (window.decorView as? ViewGroup)?.removeAllViews()
        super.onDestroy()
    }
}

