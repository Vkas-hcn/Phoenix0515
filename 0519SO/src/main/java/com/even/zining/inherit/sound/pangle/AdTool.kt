package com.even.zining.inherit.sound.pangle

import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAd
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdLoadCallback
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialRequest
import com.bytedance.sdk.openadsdk.api.model.PAGErrorModel
import com.even.zining.inherit.sound.start.FnnStartFun
import com.even.zining.inherit.sound.start.FnnStartFun.adLimiter
import com.even.zining.inherit.sound.start.FnnStartFun.mainStart
import com.even.zining.inherit.sound.tool.TbaPostTool
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.tool.data.MMKVUtils
import com.even.zining.inherit.sound.tool.PngCanGo
import com.even.zining.inherit.sound.tool.NetPostTool
import com.thinkup.core.api.AdError
import com.thinkup.core.api.TUAdInfo
import com.thinkup.interstitial.api.TUInterstitial
import com.thinkup.interstitial.api.TUInterstitialListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


object AdTool {
    private var adTimeoutJob: Job? = null

    // 广告缓存时间（单位：毫秒）
    private val AD_CACHE_DURATION = 50 * 60 * 1000L // 50分钟

    // 上次广告加载时间
    private var lastAdLoadPangLeTime: Long = 0
    private var lastAdLoadTopOnTime: Long = 0

    // 是否正在加载广告
    private var isLoading = false
    var interstitialAd: PAGInterstitialAd? = null
    lateinit var intTonOnAd: TUInterstitial
    var isPangle = false
    private fun initAd() {
        val idBean = FnnStartFun.getAdminData() ?: return
        val idTopOn = idBean.config.identifiers[1].tag
        FnnStartFun.showLog("体外广告idTopOn id=: ${idTopOn}")
        // 创建插屏广告对象
        intTonOnAd = TUInterstitial(mainStart, idTopOn)
        // 设置插屏广告监听器
        intTonOnAd.setAdListener(object : TUInterstitialListener {
            override fun onInterstitialAdLoaded() {
                FnnStartFun.showLog("体外广告onAdLoaded: 广告加载成功")
                setAdLoadingTime(System.currentTimeMillis())
                isLoading = false
                NetPostTool.postPointData(false, "getadvertise")
            }

            override fun onInterstitialAdLoadFail(p0: AdError?) {
                FnnStartFun.showLog("体外广告onAdFailed: 广告加载失败=${p0?.fullErrorInfo}")
                resetAdStatus()
                NetPostTool.postPointData(
                    false,
                    "getfail",
                    "string1",
                    p0?.code +  "-" + p0?.desc
                )
            }

            override fun onInterstitialAdClicked(p0: TUAdInfo?) {
                FnnStartFun.showLog("体外广告onAdClicked: 广告${p0?.getPlacementId()}被点击")
                adLimiter.recordAdClick()
            }

            override fun onInterstitialAdShow(p0: TUAdInfo?) {
                FnnStartFun.adShowTime = System.currentTimeMillis()
                FnnStartFun.showLog("体外广告onAdImpression: 广告${p0?.getPlacementId()}展示")
                adLimiter.recordAdShown()
                p0?.let { NetPostTool.postTopOnAdData(it) }
                TbaPostTool.showsuccessPoint()
                resetAdStatus()
                loadAllAd()
            }

            override fun onInterstitialAdClose(p0: TUAdInfo?) {
                FnnStartFun.showLog("体外广告onAdClosed: 广告${p0?.getPlacementId()}被关闭")
                FnnStartFun.showLog("closeAllActivities-topon")
                PngCanGo.closeAllActivities()
            }

            override fun onInterstitialAdVideoStart(p0: TUAdInfo?) {
            }

            override fun onInterstitialAdVideoEnd(p0: TUAdInfo?) {
            }

            override fun onInterstitialAdVideoError(p0: AdError?) {
                FnnStartFun.adShowTime = 0
                resetAdStatus()
                FnnStartFun.showLog("体外广告onAdClosed: 广告展示失败")
                NetPostTool.postPointData(
                    false,
                    "showfailer",
                    "string3",
                    p0?.fullErrorInfo
                )
            }
        })
    }

    fun startLoadAd() {
        val idBean = FnnStartFun.getAdminData() ?: return
        val idPangLe = idBean.config.identifiers[0].tag
        val idTopOn = idBean.config.identifiers[1].tag
        FnnStartFun.showLog("体外广告id=: idPangLe=${idPangLe}-TopOn=${idTopOn}")
        initAd()
        if (idPangLe.isNotBlank()) {
            isPangle = true
            loadAllAd()
            return
        }
        if (idTopOn.isNotBlank()) {
            isPangle = false
            loadAllAd()
        }
    }

    fun loadAllAd() {
        if (!adLimiter.canShowAd(false)) {
            FnnStartFun.showLog("体外广告展示限制,不加载广告")
            return
        }
        val currentTime = System.currentTimeMillis()
        if (isHaveAdData() && ((currentTime - getAdLoadingTime()) < AD_CACHE_DURATION)) {
            FnnStartFun.showLog("不加载,有缓存的广告")
        } else {
            if (((currentTime - getAdLoadingTime()) >= AD_CACHE_DURATION) && !isLoading) {
                FnnStartFun.showLog("无广告加载，或者缓存过期重新加载")
                resetAdStatus()
            }
            if (isLoading) {
                FnnStartFun.showLog("正在加载广告，等待加载完成")
                return
            }
            isLoading = true
            FnnStartFun.showLog("发起新的广告请求")
            if (isPangle) {
                loadPAGAd()
            } else {
                intTonOnAd.load()
            }
            NetPostTool.postPointData(false, "reqadvertise")
            startAdTimeout()
        }
    }

    private fun loadPAGAd() {
        val idBean = FnnStartFun.getAdminData() ?: return
        val id = idBean.config.identifiers[0].tag
        FnnStartFun.showLog("体外广告id=: ${id}")
        PAGInterstitialAd.loadAd(
            id,
            PAGInterstitialRequest(),
            object : PAGInterstitialAdLoadCallback {
                override fun onError(pagErrorModel: PAGErrorModel) {
                    FnnStartFun.showLog("体外广告onAdFailed: 广告加载失败=code=${pagErrorModel.errorCode}--${pagErrorModel.errorMessage}")
                    resetAdStatus()
                    NetPostTool.postPointData(
                        false,
                        "getfail",
                        "string1",
                        "${pagErrorModel.errorCode}--${pagErrorModel.errorMessage}"
                    )
                }

                override fun onAdLoaded(pagInterstitialAd: PAGInterstitialAd) {
                    FnnStartFun.showLog("体外广告onAdLoaded: 广告加载成功")
                    setAdLoadingTime(System.currentTimeMillis())
                    isLoading = false
                    NetPostTool.postPointData(false, "getadvertise")
                    interstitialAd = pagInterstitialAd
                }
            })
    }

    fun isHaveAdData(): Boolean {
        return if (isPangle) {
            interstitialAd?.isReady == true
        } else {
            intTonOnAd.isAdReady
        }
    }

    fun getAdLoadingTime(): Long {
        if (isPangle) {
            return lastAdLoadPangLeTime
        } else {
            return lastAdLoadTopOnTime
        }
    }

    fun setAdLoadingTime(time: Long) {
        if (isPangle) {
            lastAdLoadPangLeTime = time
        } else {
            lastAdLoadTopOnTime = time
        }
    }

    private fun startAdTimeout() {
        cancelTimeout()
        adTimeoutJob = CoroutineScope(Dispatchers.IO).launch {
            delay(60_000)
            if (isLoading && !isHaveAdData()) {
                FnnStartFun.showLog("广告加载超时，重新请求广告")
                resetAdStatus()
                loadAllAd()
            }
        }
    }

    private fun cancelTimeout() {
        adTimeoutJob?.cancel()
        adTimeoutJob = null
    }

    //广告状态重置
    fun resetAdStatus() {
        isLoading = false
        setAdLoadingTime(0)
    }


    fun addFa() {
        var adNum = MMKVUtils.getInt(FnnLoadData.isAdFailCount)
        adNum++
        MMKVUtils.put(FnnLoadData.isAdFailCount, adNum)
    }

    fun isHaveAdNextFun() {
        if (canShowLocked()) {
            FnnStartFun.showLog("锁屏或者息屏状态，广告不展示")
            return
        }
        NetPostTool.postPointData(false, "isunlock")
        val jsonBean = FnnStartFun.getAdminData() ?: return
        val instalTime = PngCanGo.getInstallTimeDataFun()
        val ins = jsonBean.config.scheduler.initialDelay ?: 0
        val wait = jsonBean.config.scheduler.displayGap ?: 0
        if (AdUtils.isBeforeInstallTime(instalTime, ins)) return
        if (AdUtils.isAdDisplayIntervalTooShort(wait)) return

        if (!adLimiter.canShowAd(true)) {
            FnnStartFun.showLog("体外广告展示限制")
            return
        }

        val activities = PngCanGo.activityList.isEmpty()
        val state = isAllActivitiesInWhitelist()
        if (isHaveAdData() || activities || state) {
            FnnStartFun.showLog("体外流程")
            AdUtils.showAdAndTrack()
        }
    }

    private fun canShowLocked(): Boolean {
        val powerManager = mainStart.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val keyguardManager =
            mainStart.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (powerManager == null || keyguardManager == null) {
            return false
        }
        val isScreenOn = powerManager.isInteractive
        val isInKeyguardRestrictedInputMode = keyguardManager.inKeyguardRestrictedInputMode()

        return !isScreenOn || isInKeyguardRestrictedInputMode
    }
    private fun isAllActivitiesInWhitelist(): Boolean {
        // 定义白名单集合
        val whitelist = setOf(
            "com.even.zining.inherit.sound.view.ac.SoShowAc",
            "com.thunderbolt.methods.bodhisattva.ui.guide.Guide2Activity",
            "com.thunderbolt.methods.bodhisattva.ui.MainActivity",
            "com.thunderbolt.methods.bodhisattva.ui.detail.DetailActivity"
        )
        // 遍历检查所有 Activity
        for (activity in PngCanGo.activityList) {
            val className = activity.javaClass.name
            if (className.isEmpty()) continue // 跳过空类名
            if (className !in whitelist) {
                FnnStartFun.showLog("当前广告正在显示：$className")
                return false // 存在不在白名单中的类
            }
        }
        return true // 所有类均合法
    }
}
