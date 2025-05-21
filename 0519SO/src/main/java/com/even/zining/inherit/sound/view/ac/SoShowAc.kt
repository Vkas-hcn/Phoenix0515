package com.even.zining.inherit.sound.view.ac

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdInteractionCallback
import com.bytedance.sdk.openadsdk.api.model.PAGErrorModel
import com.even.zining.inherit.sound.pangle.AdTool
import com.even.zining.inherit.sound.pangle.AdTool.intTonOnAd
import com.even.zining.inherit.sound.pangle.AdTool.interstitialAd
import com.even.zining.inherit.sound.pangle.AdTool.isPangle
import com.even.zining.inherit.sound.tool.NetPostTool
import com.even.zining.inherit.sound.start.FnnStartFun
import com.even.zining.inherit.sound.start.FnnStartFun.adLimiter
import com.even.zining.inherit.sound.tool.TbaPostTool
import com.even.zining.inherit.sound.tool.PngCanGo
import com.even.zining.inherit.sound.znet.lo.FnnA
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoShowAc : AppCompatActivity() {

    private val viewModel: SoShowViewModel by viewModels()
    var isClickpangle = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FnnStartFun.showLog("BroKaActivity onCreate")
        FnnA.aKig(this)
        initializeViewModel()
        observeViewModel()
        onBackPressedDispatcher.addCallback {
        }
    }

    private fun initializeViewModel() {
        viewModel.determineContentType()
    }

    private fun observeViewModel() {

        viewModel.showAd.observe(this) { delayDuration ->
            lifecycleScope.launch {
                if (delayDuration != null) {
                    handleAdDisplay(delayDuration)
                }
            }

        }

        viewModel.adSuccessTrack.observe(this) {
            viewModel.trackAdSuccess()
        }

    }

    private suspend fun handleAdDisplay(delayDuration: Long) {
        delay(delayDuration)
        if (isPangle) {
            if (interstitialAd != null && interstitialAd?.isReady == true) {
                interstitialAd?.setAdInteractionCallback(object :
                    PAGInterstitialAdInteractionCallback() {
                    override fun onAdShowed() {
                        super.onAdShowed()
                        FnnStartFun.adShowTime = System.currentTimeMillis()
                        FnnStartFun.showLog("体外广告 -pangle: 广告展示")
                        adLimiter.recordAdShown()
                        AdTool.resetAdStatus()
                        interstitialAd?.let {
                            FnnStartFun.showLog("体外广告-pangle: 广告展示2")
                            NetPostTool.postPangleAdData(it)
                        }
                        TbaPostTool.showsuccessPoint()
                        AdTool.loadAllAd()
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        FnnStartFun.showLog("体外广告onAdClicked-pangle: 广告被点击")
                        adLimiter.recordAdClick()
                    }

                    override fun onAdDismissed() {
                        super.onAdDismissed()
                        FnnStartFun.showLog("体外广告onAdClosed-pangle: 广告被关闭")
                        FnnStartFun.showLog("closeAllActivities-pangle")
                        PngCanGo.closeAllActivities()
                    }

                    override fun onAdShowFailed(@NonNull pagErrorModel: PAGErrorModel) {
                        super.onAdShowFailed(pagErrorModel)
                        FnnStartFun.adShowTime = 0
                        AdTool.resetAdStatus()
                        FnnStartFun.showLog("体外广告onAdClosed-pangle: 广告展示失败")
                        NetPostTool.postPointData(
                            false,
                            "showfailer",
                            "string3",
                            pagErrorModel?.errorMessage
                        )
                    }
                })
                NetPostTool.postPointData(false, "isready")
                interstitialAd?.show(this)
                viewModel.handleAdShow(delayDuration)
            } else {
                finish()
            }
        } else {
            if (intTonOnAd.isAdReady) {
                NetPostTool.postPointData(false, "isready")
                intTonOnAd.show(this)
                viewModel.handleAdShow(delayDuration)
            } else {
                finish()
            }
        }

    }

    override fun onDestroy() {
        (window.decorView as? ViewGroup)?.removeAllViews()
        super.onDestroy()
    }
}
