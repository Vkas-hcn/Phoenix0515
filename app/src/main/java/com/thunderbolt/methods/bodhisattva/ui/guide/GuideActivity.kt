package com.thunderbolt.methods.bodhisattva.ui.guide

import android.content.Intent
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import com.thunderbolt.methods.bodhisattva.base.BaseActivity
import com.thunderbolt.methods.bodhisattva.base.BaseViewModel
import com.thunderbolt.methods.bodhisattva.R
import com.thunderbolt.methods.bodhisattva.databinding.ActivityGuideBinding
import com.thunderbolt.methods.bodhisattva.ui.MainActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GuideActivity : BaseActivity<ActivityGuideBinding, BaseViewModel>() {
    override val layoutId: Int
        get() = R.layout.activity_guide
    override val viewModelClass: Class<BaseViewModel>
        get() = BaseViewModel::class.java


    private var job: Job? = null
    override fun setupViews() {
        onBackPressedDispatcher.addCallback {
        }
        var num = 0

        job = lifecycleScope.launch {
            while (true) {
                binding.loiGuide.progress = num
                num++
                if (num >= 100) {
                    job?.cancel()
                    binding.loiGuide.progress = 100
                    startActivity(Intent(this@GuideActivity, MainActivity::class.java))
                    finish()
                }
                delay(20)
            }

        }
    }

    override fun observeViewModel() {
    }

}