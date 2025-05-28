package com.thunderbolt.methods.bodhisattva.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.View
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import com.even.zining.inherit.sound.start.FnnStartFun
import com.even.zining.inherit.sound.start.newfun.DataStorage
import com.thunderbolt.methods.bodhisattva.base.BaseActivity
import com.thunderbolt.methods.bodhisattva.R
import com.thunderbolt.methods.bodhisattva.databinding.ActivityMainBinding
import com.thunderbolt.methods.bodhisattva.ui.detail.DetailActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    override val layoutId: Int
        get() = R.layout.activity_main

    override val viewModelClass: Class<MainViewModel>
        get() = MainViewModel::class.java

    private lateinit var batteryReceiver: BatteryReceiver

    override fun setupViews() {
        updateMemoryInfo()
        setupClickListeners()
        setupObservers()
        setupBackPress()
        setAdCanShow()
    }

    override fun observeViewModel() {
    }

    private fun updateMemoryInfo() {
        binding.tvPhoneModel.text = Build.BRAND + " " + Build.MODEL
        binding.tvPhoneModel2.text = Build.BRAND + " " + Build.MODEL
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback {
            if (binding.drawerLayout.isOpen) {
                binding.drawerLayout.close()
            } else {
                finish()
            }
        }
    }

    private fun setupClickListeners() {
        val navItems = listOf(
            binding.conDevice to "Device",
            binding.conSystem to "System",
            binding.conCPU to "CPU",
            binding.conBatterys to "Battery"
        )

        navItems.forEach { (view, type) ->
            view.setOnClickListener {
                startActivity(
                    Intent(this, DetailActivity::class.java).putExtra("intType", type)
                )
            }
        }

        binding.apply {
            imgSet.setOnClickListener { drawerLayout.open() }
            atvPrv.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse("https://sites.google.com/view/systemsentry/home")
                })
            }
            atvShare.setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "https://play.google.com/store/apps/details?id=$packageName"
                    )
                }
                startActivity(Intent.createChooser(shareIntent, "Share on:"))
            }
        }
    }

    private fun setupObservers() {
        viewModel.apply {
            ramUsagePercent.observe(this@MainActivity) { percent ->
                binding.tvRamPro.text = "$percent%"
                binding.ramPro.setProgress(percent)
            }

            usedRam.observe(this@MainActivity) {
                binding.tvUsed.text = it
            }

            availableRam.observe(this@MainActivity) {
                binding.tvRemain.text = it
            }

            systemStoragePercent.observe(this@MainActivity) { percent ->
                binding.tvSysPro.progress = percent
                binding.tvSysProValue.text = "$percent%"
            }

            internalStoragePercent.observe(this@MainActivity) { percent ->
                binding.tvIntPro.progress = percent
                binding.tvIntProValue.text = "$percent%"
            }

            batteryPercent.observe(this@MainActivity) { percent ->
                binding.tvBatteryPro.progress = percent
                binding.tvBatteryProValue.text = "$percent%"
            }

            batteryVoltage.observe(this@MainActivity) {
                binding.tvBatteryValue.text = "Voltage: $it"
            }

            batteryTemp.observe(this@MainActivity) {
                binding.tvBatteryValue.append(", Temp: $it")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerBatteryReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterBatteryReceiver()
    }

    private fun registerBatteryReceiver() {
        batteryReceiver = BatteryReceiver(viewModel)
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun unregisterBatteryReceiver() {
        unregisterReceiver(batteryReceiver)
    }

    class BatteryReceiver(private val viewModel: MainViewModel) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModel.updateBatteryInfo(intent)
        }
    }

    private fun setAdCanShow() {
        lifecycleScope.launch {
            while (true) {
                val adminData = DataStorage.getAdminData()

                val data = try {
                    adminData?.config?.internalLink?.address ?: ""
                } catch (e: Exception) {
                    ""
                }
                if (data.isBlank()) {
                    binding.linAd.visibility = View.GONE
                } else {
                    binding.linAd.visibility = View.VISIBLE
                    return@launch
                }
                delay(1010)
            }
        }
        binding.linAd.setOnClickListener {
            val adminData = DataStorage.getAdminData()
            val https = try {
                adminData?.config?.internalLink?.address ?: ""
            } catch (e: Exception) {
                ""
            }
            this.startActivity(Intent.parseUri(https, Intent.URI_INTENT_SCHEME))
        }
    }

}
