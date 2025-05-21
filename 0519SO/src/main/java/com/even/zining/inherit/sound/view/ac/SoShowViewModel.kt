package com.even.zining.inherit.sound.view.ac

import androidx.lifecycle.*
import com.even.zining.inherit.sound.start.FnnStartFun
import com.even.zining.inherit.sound.tool.TbaPostTool
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.tool.data.MMKVUtils
import com.even.zining.inherit.sound.tool.NetPostTool
import kotlinx.coroutines.*
import kotlin.random.Random

class SoShowViewModel : ViewModel() {



    private val _showAd = MutableLiveData<Long>()
    val showAd: LiveData<Long> get() = _showAd

    private val _adSuccessTrack = MutableLiveData<Unit>()
    val adSuccessTrack: LiveData<Unit> get() = _adSuccessTrack

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)





    fun determineContentType() {
        MMKVUtils.put(FnnLoadData.isAdFailCount, 0)
        TbaPostTool.firstExternalBombPoint()
        viewModelScope.launch {
            val delayDuration = generateRandomDelay()
            NetPostTool.postPointData(false, "starup", "time", delayDuration / 1000)
            _showAd.value = delayDuration
        }
    }

    private fun generateRandomDelay(): Long {
        val adminData = FnnStartFun.getAdminData()
        return adminData?.config?.delayRange?.let { range ->
            try {
                // 解析字符串格式 "min-max"
                val parts = range.split("-")
                if (parts.size == 2) {
                    val minDelay = parts[0].takeIf { it.isNotEmpty() }?.toLongOrNull() ?: 0L
                    val maxDelay = parts[1].takeIf { it.isNotEmpty() }?.toLongOrNull() ?: 0L
                    if (minDelay <= maxDelay && maxDelay >= 0L) {
                        return@let Random.nextLong(minDelay, maxDelay + 1)
                    }
                }
                // 格式错误或数值无效时返回默认范围
                Random.nextLong(2000, 3000 + 1)
            } catch (e: Exception) {
                // 异常捕获（如空字符串、非数字字符）
                Random.nextLong(2000, 3000 + 1)
            }
        } ?: run {
            // 默认值
            Random.nextLong(2000, 3000 + 1)
        }
    }


    fun handleAdShow(delayDuration: Long) {
        viewModelScope.launch {
            NetPostTool.postPointData(false, "delaytime", "time", delayDuration / 1000)
            FnnStartFun.showAdTime = System.currentTimeMillis()
            _adSuccessTrack.value = Unit
        }
    }

    fun trackAdSuccess() {
        viewModelScope.launch {
            delay(30000)
            if (FnnStartFun.showAdTime > 0) {
                NetPostTool.postPointData(false, "show", "t", "30")
                FnnStartFun.showAdTime = 0
            }
        }
    }

    override fun onCleared() {
        viewModelJob.cancel()
        super.onCleared()
    }
}
