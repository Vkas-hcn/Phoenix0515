package com.even.zining.inherit.sound.pangle


import com.even.zining.inherit.sound.tool.PngCanGo
import com.even.zining.inherit.sound.tool.NetPostTool
import com.even.zining.inherit.sound.start.FnnStartFun
import com.even.zining.inherit.sound.zeros.FnnLoad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AdUtils {

    fun isBeforeInstallTime(instalTime: Long, ins: Int): Boolean {
        try {
            if (instalTime < ins) {
                FnnStartFun.showLog("距离首次安装时间小于$ins 秒，广告不能展示")
                NetPostTool.postPointData(false, "ispass", "string", "firstInstallation")
                return true
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

    fun isAdDisplayIntervalTooShort(wait: Int): Boolean {
        try {
            val jiange = (System.currentTimeMillis() - FnnStartFun.adShowTime) / 1000
            if (jiange < wait) {
                FnnStartFun.showLog("广告展示间隔时间小于$wait 秒，不展示")
                NetPostTool.postPointData(false, "ispass", "string", "Interval")
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    fun showAdAndTrack() {
        NetPostTool.postPointData(false, "ispass", "string", "")
        CoroutineScope(Dispatchers.Main).launch {
            PngCanGo.closeAllActivities()
            FnnStartFun.showLog("closeAllActivities-so")
            delay(1022)
            AdTool.addFa()
            FnnLoad.fnnLoad(667654)
            NetPostTool.postPointData(false, "callstart")
        }
    }
}
