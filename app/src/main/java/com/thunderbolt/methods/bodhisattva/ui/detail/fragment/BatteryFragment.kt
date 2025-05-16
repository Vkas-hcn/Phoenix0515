package com.thunderbolt.methods.bodhisattva.ui.detail.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.thunderbolt.methods.bodhisattva.databinding.FragmentBatteryBinding
import com.thunderbolt.methods.bodhisattva.databinding.FragmentDeviceBinding

class BatteryFragment : Fragment() {
    private var _binding: FragmentBatteryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBatteryBinding.inflate(inflater, container, false)
        registerBatteryReceiver()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 释放视图绑定资源
    }

    private fun registerBatteryReceiver() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        requireActivity().registerReceiver(batteryInfoReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(batteryInfoReceiver) // 解注册广播接收器
    }

    private val batteryInfoReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            // 空安全处理 + 视图存活检测
            _binding?.let {
                // Health状态映射
                it.tvHealth.text = getBatteryHealthText(intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1))

                // 电池状态
                it.tvStatus.text = getBatteryStatusText(intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1))

                // 电量百分比
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                it.tvLevel.text = if (level != -1 && scale != 0) {
                    "${(level * 100f / scale).toInt()}%"
                } else "N/A"

                // 电压显示
                val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
                it.tvVoltage.text = if (voltage != -1) {
                    "${"%.2f".format(voltage / 1000f)} V"
                } else "N/A"

                // 充电方式
                it.tvPowerSource.text = when(intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
                    BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                    else -> "Not Charging"
                }

                // 电池技术
                it.tvTechnology.text = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "N/A"

                // 温度显示
                val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                it.tvTemperature.text = if(temp != -1) {
                    "${"%.1f".format(temp / 10f)}°C"
                } else "N/A"

                // 电池容量（API 21+）
                it.tvCapacity.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        val bm = requireActivity().getSystemService(BATTERY_SERVICE) as BatteryManager
                        "${bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000} mAh"
                    } catch (e: Exception) {
                        "Access Denied"
                    }
                } else {
                    "API 21+ Required"
                }
            }
        }

        // 提取健康状态映射方法
        private fun getBatteryHealthText(health: Int): String = when(health) {
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            else -> "Unknown"
        }

        // 提取电池状态映射方法
        private fun getBatteryStatusText(status: Int): String = when(status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Fully Charged"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
    }
}
