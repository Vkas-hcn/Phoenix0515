package com.thunderbolt.methods.bodhisattva.ui.detail.fragment

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.thunderbolt.methods.bodhisattva.databinding.FragmentDeviceBinding
import com.thunderbolt.methods.bodhisattva.databinding.FragmentSystemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class SystemFragment : Fragment() {
    private var _binding: FragmentSystemBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSystemBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setSys()
        return root
    }

    private fun setSys() {
        val versionInfo = getAndroidVersionInfo()
        with(binding) {
            tvNum.text = versionInfo["RELEASE"]
            tvAndroid.text = "Android " + versionInfo["RELEASE"]
            tvJG.text = versionInfo["CODENAME"]
            tvjd.text = "Released :" + versionInfo["SECURITY_PATCH"]
            // 系统基本信息
            tvCodeName.text = "Android" + Build.VERSION.CODENAME
            tvApiLevel.text = Build.VERSION.SDK_INT.toString()
            tvSecurityPatchLevel.text = try {
                Build.VERSION.SECURITY_PATCH
            } catch (e: Exception) {
                "Unknown"
            }
            tvBuildNumber.text = Build.DISPLAY
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                tvBaseband.text = Build.getRadioVersion()
            } else {
                tvBaseband.text = getSystemProperty("gsm.version.baseband")
            }
            // 基带版本
            // Java虚拟机信息
            tvJavaVm.text = System.getProperty("java.vm.version") ?: "Unknown"

            // 内核版本
            tvKernel.text = System.getProperty("os.version") ?: "Unknown"

            // 系统语言
            tvLanguage.text = Locale.getDefault().displayLanguage

            // OpenGL ES版本
            try {
                val configInfo =
                    (requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager)
                        .deviceConfigurationInfo
                tvOpenGLEs.text = configInfo.glEsVersion
            } catch (e: Exception) {
                tvOpenGLEs.text = "Unavailable"
            }

            // Root权限检测
            tvRootAccess.text = isRooted()

            // SELinux状态
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val selinuxStatus = getSELinuxStatus()
                    withContext(Dispatchers.Main) {
                        tvSELinux.text = selinuxStatus
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        tvSELinux.text = "Unavailable"
                    }
                }
            }
            // Google Play服务
            tvGooglePlayServices.text = isGooglePlayServicesAvailable().toString()

            // 系统运行时间
            tvSystemUptime.text =
                DateUtils.formatElapsedTime(SystemClock.elapsedRealtime() / 1000)

            // Treble支持
            tvTreble.text = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Build.SUPPORTED_ABIS.joinToString()
                } else "Unavailable"
            } catch (e: SecurityException) {
                "Unavailable"
            }
        }
    }
    fun getAndroidVersionInfo(): Map<String, String> {
        return mapOf(
            "SDK_INT" to Build.VERSION.SDK_INT.toString(),  // API 级别（如 33）
            "RELEASE" to Build.VERSION.RELEASE,             // 版本号（如 "13.0"）
            "CODENAME" to Build.VERSION.CODENAME,           // 开发代号（如 "Tiramisu"）
            "BASE_OS" to Build.VERSION.BASE_OS,            // 底层系统版本（可能为空）
            "SECURITY_PATCH" to Build.VERSION.SECURITY_PATCH // 安全补丁日期
        )
    }
    private fun isGooglePlayServicesAvailable(): Boolean {
        return try {
            requireActivity().packageManager.getPackageInfo("com.google.android.gms", 0) != null
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    private fun getSELinuxStatus(): String {
        return try {
            val process = Runtime.getRuntime().exec("getenforce")
            val output = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            when {
                output.contains("Enforcing") -> "Enforcing"
                output.contains("Permissive") -> "Permissive"
                else -> "Disabled"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
    private fun isRooted(): String {
        return try {
            val paths = arrayOf(
                "/system/app/Superuser.apk",
                "/system/xbin/which su",
                "/system/bin/su",
                "/sbin/su",
                "/su/bin/su"
            )
            paths.any { File(it).exists() }.toString()
        } catch (e: SecurityException) {
            "Unknown"
        }
    }
    private fun getSystemProperty(key: String): String {
        return try {
            val c = Class.forName("android.os.SystemProperties")
            val get = c.getMethod("get", String::class.java)
            get.invoke(c, key) as String
        } catch (e: Exception) {
            "Unknown"
        }
    }
}