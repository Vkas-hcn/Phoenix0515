package com.thunderbolt.methods.bodhisattva.ui.detail.fragment


import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.thunderbolt.methods.bodhisattva.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.TimeZone

class DeviceViewModel() : BaseViewModel() {
    private val _viewState = MutableLiveData<DeviceViewState>()
    val viewState: LiveData<DeviceViewState> = _viewState

    fun loadDeviceData(context: Context) {
        viewModelScope.launch {
            try {
                _viewState.postValue(DeviceViewState.Loading)

                val basicInfo = getBasicDeviceInfo(context)
                val permissionInfo = getPermissionRequiredInfo(context)

                val combinedData = DeviceViewState.Data(
                    deviceState = basicInfo.deviceState,
                    model = basicInfo.model,
                    manufacturer = basicInfo.manufacturer,
                    brand = basicInfo.brand,
                    device = basicInfo.device,
                    board = basicInfo.board,
                    hardware = basicInfo.hardware,
                    fingerprint = basicInfo.fingerprint,
                    host = basicInfo.host,
                    timezone = basicInfo.timezone,
                    totalFeatures = basicInfo.totalFeatures,
                    androidDeviceId = permissionInfo.androidDeviceId,
                    deviceType = permissionInfo.deviceType,
                    macAddress = permissionInfo.macAddress,
                    advertisingId = permissionInfo.advertisingId
                )

                _viewState.postValue(DeviceViewState.Success(combinedData))
            } catch (e: Exception) {
                _viewState.postValue(DeviceViewState.Error(e))
            }
        }
    }

    private suspend fun getBasicDeviceInfo(context: Context): DeviceViewState.Data {
        return withContext(Dispatchers.Default) {
            val packageManager = context.packageManager

            DeviceViewState.Data(
                deviceState = "${Build.BRAND} ${Build.MODEL}",
                model = Build.MODEL,
                manufacturer = Build.MANUFACTURER,
                brand = Build.BRAND,
                device = Build.DEVICE,
                board = Build.BOARD,
                hardware = Build.HARDWARE,
                fingerprint = Build.FINGERPRINT,
                host = if (packageManager.hasSystemFeature("android.hardware.usb.host")) "Supported" else "Not supported",
                timezone = TimeZone.getDefault().id,
                totalFeatures = packageManager.systemAvailableFeatures.size.toString(),
                androidDeviceId = "-",
                deviceType = "-",
                macAddress = "-",
                advertisingId = "-"
            )
        }
    }

    private suspend fun getPermissionRequiredInfo(context: Context): DeviceViewState.Data {
        return withContext(Dispatchers.IO) {
            val packageManager = context.packageManager
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            DeviceViewState.Data(
                deviceState = "",
                model = "",
                manufacturer = "",
                brand = "",
                device = "",
                board = "",
                hardware = "",
                fingerprint = "",
                host = "",
                timezone = "",
                totalFeatures = "",
                androidDeviceId = getAndroidDeviceId(context),
                deviceType = getDeviceType(packageManager),
                macAddress = getMacAddress(connectivityManager),
                advertisingId = getAdvertisingId(context)
            )
        }
    }

    private fun getAndroidDeviceId(context: Context): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "Unavailable"
        } catch (e: SecurityException) {
            Log.e("DeviceViewModel", "Read phone state permission denied", e)
            "-"
        }
    }

    private fun getDeviceType(packageManager: android.content.pm.PackageManager): String {
        return try {
            when {
                packageManager.hasSystemFeature("android.hardware.type.watch") -> "Watch"
                packageManager.hasSystemFeature("android.hardware.type.television") -> "TV"
                packageManager.hasSystemFeature("android.hardware.type.automotive") -> "Car"
                else -> "Phone"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getMacAddress(connectivityManager: ConnectivityManager): String {
        return try {
            val network = connectivityManager.activeNetwork
            val linkProperties = connectivityManager.getLinkProperties(network)
            linkProperties?.linkAddresses?.firstOrNull()?.toString() ?: "Unavailable"
        } catch (e: SecurityException) {
            Log.e("DeviceViewModel", "Access fine location permission denied", e)
            "-"
        }
    }

    private suspend fun getAdvertisingId(context: Context): String {
        return withContext(Dispatchers.IO) {
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                adInfo.id ?: "-"
            } catch (e: IOException) {
                Log.e("DeviceViewModel", "IOException when fetching advertising ID", e)
                "-"
            } catch (e: GooglePlayServicesNotAvailableException) {
                Log.e("DeviceViewModel", "Google Play Services not available", e)
                "-"
            } catch (e: GooglePlayServicesRepairableException) {
                Log.e("DeviceViewModel", "Google Play Services repairable error", e)
                "-"
            }
        }
    }
}
