package com.thunderbolt.methods.bodhisattva.ui.detail

import android.widget.TextView
import androidx.fragment.app.Fragment
import com.thunderbolt.methods.bodhisattva.base.BaseActivity
import com.thunderbolt.methods.bodhisattva.base.BaseViewModel
import com.thunderbolt.methods.bodhisattva.R
import com.thunderbolt.methods.bodhisattva.databinding.ActivityDetailBinding
import com.thunderbolt.methods.bodhisattva.ui.detail.fragment.BatteryFragment
import com.thunderbolt.methods.bodhisattva.ui.detail.fragment.CpuFragment
import com.thunderbolt.methods.bodhisattva.ui.detail.fragment.DeviceFragment
import com.thunderbolt.methods.bodhisattva.ui.detail.fragment.SystemFragment
import androidx.core.graphics.toColorInt

class DetailActivity : BaseActivity<ActivityDetailBinding, BaseViewModel>() {
    override val layoutId: Int
        get() = R.layout.activity_detail
    override val viewModelClass: Class<BaseViewModel>
        get() = BaseViewModel::class.java
    private val deviceFragment = DeviceFragment()
    private val systemFragment = SystemFragment()
    private val cpuFragment = CpuFragment()
    private val batteryFragment = BatteryFragment()

    override fun setupViews() {
        //接收上个页面传递的参数
        val textView = intent.getStringExtra("intType")
        when (textView) {
            "Device" -> initFragment(binding.tvDevice)
            "System" -> initFragment(binding.tvSystem)
            "CPU" -> initFragment(binding.tvCpu)
            "Battery" -> initFragment(binding.tvBattery)
        }
        binding.aivBack.setOnClickListener { finish() }
    }

    fun initFragment(textView: TextView){
        val fragment = when (textView.text) {
            "Device" -> deviceFragment
            "System" -> systemFragment
            "CPU" -> cpuFragment
            "Battery" -> batteryFragment
            else -> deviceFragment
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
        with(binding) {
            tvDevice.setOnClickListener { switchFragment(deviceFragment, it as TextView) }
            tvSystem.setOnClickListener { switchFragment(systemFragment, it as TextView) }
            tvCpu.setOnClickListener { switchFragment(cpuFragment, it as TextView) }
            tvBattery.setOnClickListener { switchFragment(batteryFragment, it as TextView) }

            textView.setTextColor("#222222".toColorInt())
            textView.setTextSize(16f)
        }
    }

    private fun switchFragment(fragment: Fragment, textView: TextView) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
        val tabList = listOf(binding.tvDevice, binding.tvSystem, binding.tvCpu, binding.tvBattery)
        tabList.forEach {
            it.setTextColor("#8E9291".toColorInt())
            it.setTextSize(14f)
        }
        textView.setTextColor("#222222".toColorInt())
        textView.setTextSize(16f)
    }

    override fun observeViewModel() {
    }

}