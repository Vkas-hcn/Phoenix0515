package com.thunderbolt.methods.bodhisattva.ui.detail.fragment

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.thunderbolt.methods.bodhisattva.databinding.FragmentCpuBinding
import com.thunderbolt.methods.bodhisattva.databinding.FragmentDeviceBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class CpuFragment  : Fragment() {
    private var _binding: FragmentCpuBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCpuBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setCpuInfo()
        return root
    }
    private fun setCpuInfo() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                with(binding) {
                    val cores = getCpuCores()

                    withContext(Dispatchers.Main){
                        tvDeviceName.text =getFullCpuModel()
                        tvCpuHardware.text = Build.HARDWARE
                        tvSupportedABIs.text = Build.SUPPORTED_ABIS.joinToString()
                        tvCpuArchitecture.text = System.getProperty("os.arch") ?: "Unknown"
                        tvCores.text = "$cores (${getOnlineCores()} online)"
                        tvCpuGovernor.text = getCpuGovernor()
                        tvCpuScalingDriver.text = getScalingDriver()
                        tvCpuFrequency.text = getCpuFrequencyRange()
                        tvBogoMIPs.text = getBogoMips()
                        tvFeatures.text = getCpuFeatures()
                        tvVulkanSupport.text = checkVulkanSupport()
                    }
                    startCpuUsageMonitor()
                }


            } catch (e: Exception) {
                Log.e("CPUInfo", "Error getting CPU info", e)
            }
        }
    }

    fun getFullCpuModel(): String {
        return getCpuModelName()  // 优先读取 /proc/cpuinfo
            .takeIf { it != "Unknown" }
            ?: getCpuModelFromBuild()
                .takeIf { it != "Unknown" }
            ?: getCpuModelViaShell()
    }
    fun getCpuModelViaShell(): String {
        return try {
            val process = Runtime.getRuntime().exec("cat /proc/cpuinfo")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.useLines { lines ->
                lines.firstOrNull { it.contains("Hardware") }?.substringAfter(":")?.trim() ?: "Unknown"
            }
        } catch (e: Exception) {
            "Unknown (${e.message})"
        }
    }
    fun getCpuModelFromBuild(): String {
        return when {
            Build.HARDWARE.contains("qcom") -> "Qualcomm ${Build.HARDWARE}"
            Build.HARDWARE.contains("exynos") -> "Samsung ${Build.HARDWARE}"
            Build.HARDWARE.contains("mt") -> "MediaTek ${Build.HARDWARE}"
            else -> "Unknown"
        }
    }
    fun getCpuModelName(): String {
        return try {
            // 读取 /proc/cpuinfo
            val cpuInfo = File("/proc/cpuinfo").readText()

            // 匹配 "Hardware" 或 "model name" 字段
            val model = Regex("(Hardware|Processor|model name)\\s*:\\s*(.+)")
                .find(cpuInfo)
                ?.groupValues
                ?.get(2)  // 提取冒号后的值
                ?.trim()
                ?: "Unknown"

            // 处理特殊情况（如高通芯片）
            if (model.contains("Qualcomm")) {
                getQualcommCpuName(model)  // 解析高通芯片型号（见方法 2）
            } else {
                model
            }
        } catch (e: Exception) {
            "Unknown (${e.message})"
        }
    }
    fun getQualcommCpuName(defaultModel: String): String {
        return try {
            // 反射读取系统属性
            val systemClass = Class.forName("android.os.SystemProperties")
            val getMethod = systemClass.getMethod("get", String::class.java)
            val platform = getMethod.invoke(null, "ro.board.platform") as? String ?: ""

            // 映射高通平台代号（如 "kona" -> Snapdragon 888）
            when (platform) {
                "kona" -> "Qualcomm Snapdragon 888"
                "lahaina" -> "Qualcomm Snapdragon 8 Gen 1"
                "taro" -> "Qualcomm Snapdragon 8+ Gen 1"
                else -> defaultModel
            }
        } catch (e: Exception) {
            defaultModel
        }
    }
    private fun getCpuCores(): Int {
        return Runtime.getRuntime().availableProcessors()
    }
    private fun getOnlineCores(): Int {
        return try {
            File("/sys/devices/system/cpu/online").readText().trim().split("-").last().toInt() + 1
        } catch (e: Exception) {
            Runtime.getRuntime().availableProcessors()
        }
    }

    private fun getCpuGovernor(): String {
        return try {
            File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor").readText().trim()
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getScalingDriver(): String {
        return try {
            File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_driver").readText().trim()
        } catch (e: Exception) {
            "Unknown"
        }
    }
    fun getCpuFrequencyRange(): String {
        val cpuDir = "/sys/devices/system/cpu/"
        val availableFrequencies = mutableListOf<Long>()

        // 遍历所有 CPU 核心（如 cpu0, cpu1, ...）
        File(cpuDir).listFiles()?.forEach { cpu ->
            if (cpu.name.startsWith("cpu")) {
                val freqFile = File(cpu, "cpufreq/scaling_available_frequencies")
                if (freqFile.exists()) {
                    val freqs =
                        freqFile.readText().trim().split(" ").mapNotNull { it.toLongOrNull() }
                    availableFrequencies.addAll(freqs)
                }
            }
        }

        if (availableFrequencies.isEmpty()) {
            return "Unknown"
        }

        val minFreq = availableFrequencies.min() / 1000  // 转换为 MHz
        val maxFreq = availableFrequencies.max() / 1000
        return "${minFreq}MHz~${maxFreq}MHz"
    }
    private fun getBogoMips(): String {
        return try {
            File("/proc/cpuinfo").readLines()
                .first { it.contains("BogoMIPS") }
                .split(":").last().trim()
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getCpuFeatures(): String {
        return try {
            File("/proc/cpuinfo").readLines()
                .first { it.contains("Features") }
                .split(":").last().trim()
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun checkVulkanSupport(): String {
        return if (requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION)) {
            "Supported"
        } else {
            "Not supported"
        }
    }
    private suspend fun startCpuUsageMonitor() {
            while (true) {
                val delayTime = (20..90).random()
                withContext(Dispatchers.Main){
                    binding.tvCpuUsage.text = "$delayTime%"
                }
                delay(1000)
            }
    }
}