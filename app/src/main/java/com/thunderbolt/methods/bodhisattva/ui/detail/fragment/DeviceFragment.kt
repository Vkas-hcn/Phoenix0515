package com.thunderbolt.methods.bodhisattva.ui.detail.fragment

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.TimeZone


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.thunderbolt.methods.bodhisattva.databinding.FragmentDeviceBinding

class DeviceFragment : Fragment() {
    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DeviceViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[DeviceViewModel::class.java]

        // 绑定数据
        bindViewModel()

        return root
    }

    private fun bindViewModel() {
        // 观察 ViewState 变化
        viewModel.viewState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DeviceViewState.Loading -> {
                    // 显示加载状态（可选）
                }
                is DeviceViewState.Success -> {
                    updateBasicDeviceInfo(state.data)
                    updatePermissionRequiredInfo(state.data)
                }
                is DeviceViewState.Error -> {
                    // 显示错误信息（可选）
                }
            }
        }

        // 触发数据加载
        lifecycleScope.launch {
            viewModel.loadDeviceData(requireContext())
        }
    }

    private fun updateBasicDeviceInfo(data: DeviceViewState.Data) {
        with(binding) {
            tvDeviceState.text = data.deviceState
            tvModel.text = data.model
            tvManufacturer.text = data.manufacturer
            tvBrand.text = data.brand
            tvDevice.text = data.device
            tvBoard.text = data.board
            tvHardware.text = data.hardware
            tvBuildFingerprint.text = data.fingerprint
            tvHost.text = data.host
            tvTimezone.text = data.timezone
            tvTotalDeviceFeatures.text = data.totalFeatures
        }
    }

    private fun updatePermissionRequiredInfo(data: DeviceViewState.Data) {
        with(binding) {
            tvAndroidDeviceId.text = data.androidDeviceId
            tvDeviceType.text = data.deviceType
            tvAddress.text = data.macAddress
            tvAdvertisingId.text = data.advertisingId
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
