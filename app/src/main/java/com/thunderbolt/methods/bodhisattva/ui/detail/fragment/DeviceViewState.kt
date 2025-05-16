package com.thunderbolt.methods.bodhisattva.ui.detail.fragment





sealed class DeviceViewState {
    object Loading : DeviceViewState()
    data class Success(val data: Data) : DeviceViewState()
    data class Error(val exception: Exception) : DeviceViewState()

    data class Data(
        val deviceState: String = "",
        val model: String = "",
        val manufacturer: String = "",
        val brand: String = "",
        val device: String = "",
        val board: String = "",
        val hardware: String = "",
        val fingerprint: String = "",
        val host: String = "",
        val timezone: String = "",
        val totalFeatures: String = "",
        val androidDeviceId: String = "",
        val deviceType: String = "",
        val macAddress: String = "",
        val advertisingId: String = ""
    )
}
