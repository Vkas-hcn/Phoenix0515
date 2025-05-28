package com.thunderbolt.methods.bodhisattva.ui.guide

import android.content.Intent
import android.util.Log
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.thinkup.core.api.AdError
import com.thinkup.core.api.TUAdInfo
import com.thinkup.splashad.api.TUSplashAd
import com.thinkup.splashad.api.TUSplashAdExtraInfo
import com.thinkup.splashad.api.TUSplashAdListener
import com.thunderbolt.methods.bodhisattva.R
import com.thunderbolt.methods.bodhisattva.base.BaseActivity
import com.thunderbolt.methods.bodhisattva.base.BaseViewModel
import com.thunderbolt.methods.bodhisattva.databinding.ActivityGuideBinding
import com.thunderbolt.methods.bodhisattva.ui.App.Companion.instance
import com.thunderbolt.methods.bodhisattva.ui.MainActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class Guide2Activity : BaseActivity<ActivityGuideBinding, BaseViewModel>() {
    override val layoutId: Int
        get() = R.layout.activity_guide
    override val viewModelClass: Class<BaseViewModel>
        get() = BaseViewModel::class.java
    private lateinit var splashAd: TUSplashAd
    var job: Job? = null
    override fun setupViews() {
        onBackPressedDispatcher.addCallback {
        }
        initSpAd()
    }

    fun jumpMain() {
        Log.e("TAG", "jumpMain: ", )
        startActivity(Intent(this@Guide2Activity, MainActivity::class.java))
        finish()
    }


    override fun observeViewModel() {
    }

    private fun initSpAd() {
        // 创建开屏广告对象
        Log.e("TAG", "open 创建开屏广告对象: ", )
        splashAd = TUSplashAd(
            instance,
            FnnLoadData.getConfig().openidTopon,
            object : TUSplashAdListener {

                override fun onAdLoaded(p0: Boolean) {
                    Log.e("TAG", "open ad load success")
                }

                override fun onAdLoadTimeout() {
                    Log.e("TAG", "open ad load onAdLoadTimeout")

                }

                override fun onNoAdError(p0: AdError?) {
                    Log.e("TAG", "open ad load onNoAdError")

                }

                override fun onAdShow(p0: TUAdInfo?) {
                    Log.e("TAG", "open ad load onAdShow")

                }

                override fun onAdClick(p0: TUAdInfo?) {
                }

                override fun onAdDismiss(p0: TUAdInfo?, p1: TUSplashAdExtraInfo?) {
                    Log.e("TAG", "open ad load onAdDismiss")
                    jumpMain()
                }
            })
        splashAd.loadAd()
        showOpenAd()
    }

    private fun showOpenAd() {
        job?.cancel()
        job = null
        job = lifecycleScope.launch {
            try {
                var num = 0

                withTimeout(12000L) {
                    while (isActive) {
                        binding.loiGuide.progress = num
                        num++
                        if (num >= 100) {
                            binding.loiGuide.progress = 100
                        }
                        if (splashAd.isAdReady) {
                            splashAd.show(this@Guide2Activity, binding.startAll)
                            break
                        }
                        delay(100L)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("TAG", "open: TimeoutCancellationException", )
                jumpMain()
            }
        }
    }
}