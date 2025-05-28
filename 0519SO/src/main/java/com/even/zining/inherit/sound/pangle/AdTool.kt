package com.even.zining.inherit.sound.pangle

import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
import com.bytedance.sdk.openadsdk.api.interstitial.*
import com.bytedance.sdk.openadsdk.api.model.PAGErrorModel
import com.even.zining.inherit.sound.start.FnnStartFun
import com.even.zining.inherit.sound.start.FnnStartFun.mainStart
import com.even.zining.inherit.sound.start.newfun.DataStorage
import com.even.zining.inherit.sound.start.newfun.Logger
import com.even.zining.inherit.sound.tool.*
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.tool.data.MMKVUtils
import com.even.zining.inherit.sound.zeros.FnnLoad
import com.thinkup.core.api.AdError
import com.thinkup.core.api.TUAdInfo
import com.thinkup.interstitial.api.*
import kotlinx.coroutines.*

/**
 * 广告工具类：负责广告加载、展示、状态管理和展示条件控制
 */
object AdTool {
    private const val AD_CACHE_DURATION = 50 * 60 * 1000L

    private var adTimeoutJob: Job? = null
    private var isLoading = false
    private var lastAdLoadPangLeTime: Long = 0
    private var lastAdLoadTopOnTime: Long = 0
    var interstitialAd: PAGInterstitialAd? = null
    lateinit var intTonOnAd: TUInterstitial
    var isPangle = false
    // ===== 模块入口 =====
    fun startLoadAd() = initAd().runIfValidIds { loadAllAd() }

    // ===== 广告初始化 =====
    private fun initAd() {
        val idBean = DataStorage.getAdminData() ?: return
        val idTopOn = idBean.config.identifiers[1].tag
        Logger.showLog("体外广告idTopOn id=: $idTopOn")

        intTonOnAd = TUInterstitial(mainStart, idTopOn).apply {
            setAdListener(TopOnAdListener())
        }
    }

    // ===== 广告加载逻辑 =====
    fun loadAllAd() {
        if (!XianUtils.canShowAdFun(false)) {
            Logger.showLog("体外广告展示限制,不加载广告")
            return
        }

        val currentTime = System.currentTimeMillis()
        when {
            hasValidCachedAd(currentTime) -> {
                Logger.showLog("不加载,有缓存的广告")
            }
            shouldReloadAd(currentTime) -> {
                Logger.showLog("无广告加载，或者缓存过期重新加载")
                resetAdStatus()
                if (isPangle) loadPAGAd() else intTonOnAd.load()
                NetPostTool.postPointData(false, "reqadvertise")
                startAdTimeout()
            }
        }
    }

    private fun hasValidCachedAd(currentTime: Long): Boolean {
        return isHaveAdData() && ((currentTime - getAdLoadingTime()) < AD_CACHE_DURATION)
    }

    private fun shouldReloadAd(currentTime: Long): Boolean {
        return ((currentTime - getAdLoadingTime()) >= AD_CACHE_DURATION) && !isLoading
    }

    private fun loadPAGAd() {
        val idBean = DataStorage.getAdminData() ?: return
        val id = idBean.config.identifiers[0].tag
        Logger.showLog("loadPAGAd体外广告id=: $id")

        PAGInterstitialAd.loadAd(
            id,
            PAGInterstitialRequest(),
            object : PAGInterstitialAdLoadCallback {
                override fun onError(pagErrorModel: PAGErrorModel) {
                    handlePangleAdError(pagErrorModel)
                }

                override fun onAdLoaded(pagInterstitialAd: PAGInterstitialAd) {
                    handlePangleAdLoaded(pagInterstitialAd)
                }
            }
        )
    }

    private fun handlePangleAdError(pagErrorModel: PAGErrorModel) {
        Logger.showLog("体外广告onAdFailed: 广告加载失败=code=${pagErrorModel.errorCode}--${pagErrorModel.errorMessage}")
        resetAdStatus()
        NetPostTool.postPointData(
            false,
            "getfail",
            "string1",
            "${pagErrorModel.errorCode}--${pagErrorModel.errorMessage}"
        )
    }

    private fun handlePangleAdLoaded(pagInterstitialAd: PAGInterstitialAd) {
        Logger.showLog("体外广告onAdLoaded: 广告加载成功")
        setAdLoadingTime(System.currentTimeMillis())
        isLoading = false
        NetPostTool.postPointData(false, "getadvertise")
        interstitialAd = pagInterstitialAd
    }

    // ===== 广告状态管理 =====
    fun isHaveAdData(): Boolean = when {
        isPangle -> interstitialAd?.isReady == true
        else -> intTonOnAd.isAdReady
    }

    fun getAdLoadingTime(): Long = when {
        isPangle -> lastAdLoadPangLeTime
        else -> lastAdLoadTopOnTime
    }

    fun setAdLoadingTime(time: Long) = when {
        isPangle -> lastAdLoadPangLeTime = time
        else -> lastAdLoadTopOnTime = time
    }

    // ===== 超时处理 =====
    private fun startAdTimeout() {
        cancelTimeout()
        adTimeoutJob = CoroutineScope(Dispatchers.IO).launch {
            delay(60_000)
            if (isLoading && !isHaveAdData()) {
                Logger.showLog("广告加载超时，重新请求广告")
                resetAdStatus()
                loadAllAd()
            }
        }
    }

    private fun cancelTimeout() {
        adTimeoutJob?.cancel()
        adTimeoutJob = null
    }

    fun resetAdStatus() {
        isLoading = false
        setAdLoadingTime(0)
    }

    private fun addFa() {
        synchronized(this) {
            val currentCount = MMKVUtils.getInt(FnnLoadData.isAdFailCount, 0)
            MMKVUtils.put(FnnLoadData.isAdFailCount, currentCount + 1)
        }
    }
    fun isHaveAdNextFun() {
        if (canShowLocked()) {
            Logger.showLog("锁屏或者息屏状态，广告不展示")
            return
        }

        NetPostTool.postPointData(false, "isunlock")
        val jsonBean = DataStorage.getAdminData() ?: return

        val instalTime = PngCanGo.getInstallTimeDataFun()
        val ins = jsonBean.config.scheduler.initialDelay ?: 0
        val wait = jsonBean.config.scheduler.displayGap ?: 0

        if (isBeforeInstallTime(instalTime, ins) ||
            isAdDisplayIntervalTooShort(wait) ||
            !XianUtils.canShowAdFun(true)) {
            Logger.showLog("体外广告展示限制")
            return
        }

        if (isHaveAdData() || PngCanGo.activityList.isEmpty() || isAllActivitiesInWhitelist()) {
            Logger.showLog("体外流程")
           showAdAndTrack()
        }
    }
    fun isBeforeInstallTime(instalTime: Long, ins: Int): Boolean {
        try {
            if (instalTime < ins) {
                Logger.showLog("距离首次安装时间小于$ins 秒，广告不能展示")
                NetPostTool.postPointData(false, "ispass", "string", "firstInstallation")
                return true
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

    fun isAdDisplayIntervalTooShort(wait: Int): Boolean {
        try {
            val jiange = (System.currentTimeMillis() - FnnStartFun.adShowTime) / 1000
            if (jiange < wait) {
                Logger.showLog("广告展示间隔时间小于$wait 秒，不展示")
                NetPostTool.postPointData(false, "ispass", "string", "Interval")
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    fun showAdAndTrack() {
        NetPostTool.postPointData(false, "ispass", "string", "")
        CoroutineScope(Dispatchers.Main).launch {
            PngCanGo.closeAllActivities()
            Logger.showLog("closeAllActivities-so")
            delay(1022)
            addFa()
            FnnLoad.fnnLoad(667654)
            NetPostTool.postPointData(false, "callstart")
        }
    }

    // ===== 展示条件判断 =====
    private fun canShowLocked(): Boolean {
        val powerManager = mainStart.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val keyguardManager = mainStart.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (powerManager == null || keyguardManager == null) return false

        val isScreenOn = powerManager.isInteractive
        val isInKeyguardRestrictedInputMode = keyguardManager.inKeyguardRestrictedInputMode()
        return !isScreenOn || isInKeyguardRestrictedInputMode
    }

    private fun isAllActivitiesInWhitelist(): Boolean {
        val whitelist = setOf(
            "com.even.zining.inherit.sound.view.ac.SoShowAc",
            "com.thunderbolt.methods.bodhisattva.ui.guide.Guide2Activity",
            "com.thunderbolt.methods.bodhisattva.ui.MainActivity",
            "com.thunderbolt.methods.bodhisattva.ui.detail.DetailActivity"
        )

        return PngCanGo.activityList.all { activity ->
            val className = activity.javaClass.name
            className.isNotEmpty() && className in whitelist
        }
    }

    // ===== 扩展函数 =====
    private inline fun <T> T.runIfValidIds(block: () -> Unit): Any {
        val idBean = DataStorage.getAdminData() ?: return false
        val ids = idBean.config.identifiers
        if (ids.size >= 2) {
            val idPangLe = ids[0].tag
            val idTopOn = ids[1].tag
            Logger.showLog("体外广告id=: idPangLe=$idPangLe-TopOn=$idTopOn")
            isPangle = idPangLe.isNotBlank()
            return block().also { Logger.showLog("广告加载模式: ${if (isPangle) "Pangle" else "TopOn"}") }
        }
        return false
    }

    // ===== TopOn广告监听器 =====
    private  class TopOnAdListener : TUInterstitialListener {
        override fun onInterstitialAdLoaded() {
            Logger.showLog("体外广告onAdLoaded: 广告加载成功")
            setAdLoadingTime(System.currentTimeMillis())
            isLoading = false
            NetPostTool.postPointData(false, "getadvertise")
        }

        override fun onInterstitialAdLoadFail(p0: AdError?) {
            Logger.showLog("体外广告onAdFailed: 广告加载失败=${p0?.fullErrorInfo}")
            resetAdStatus()
            NetPostTool.postPointData(
                false,
                "getfail",
                "string1",
                "${p0?.code}-${p0?.desc}"
            )
        }

        override fun onInterstitialAdClicked(p0: TUAdInfo?) {
            Logger.showLog("体外广告onAdClicked: 广告${p0?.getPlacementId()}被点击")
            XianUtils.recordAdClick()
        }

        override fun onInterstitialAdShow(p0: TUAdInfo?) {
            FnnStartFun.adShowTime = System.currentTimeMillis()
            Logger.showLog("体外广告onAdImpression: 广告${p0?.getPlacementId()}展示")
            XianUtils.recordAdShown()
            p0?.let { NetPostTool.postTopOnAdData(it) }
            TbaPostTool.showsuccessPoint()
            resetAdStatus()
            loadAllAd()
        }

        override fun onInterstitialAdClose(p0: TUAdInfo?) {
            Logger.showLog("体外广告onAdClosed: 广告${p0?.getPlacementId()}被关闭")
            Logger.showLog("closeAllActivities-topon")
            PngCanGo.closeAllActivities()
        }

        override fun onInterstitialAdVideoStart(p0: TUAdInfo?) = Unit
        override fun onInterstitialAdVideoEnd(p0: TUAdInfo?) = Unit
        override fun onInterstitialAdVideoError(p0: AdError?) {
            FnnStartFun.adShowTime = 0
            resetAdStatus()
            Logger.showLog("体外广告onAdClosed: 广告展示失败")
            NetPostTool.postPointData(
                false,
                "showfailer",
                "string3",
                p0?.fullErrorInfo
            )
        }
    }
}
