package com.even.zining.inherit.sound.pangle


import com.even.zining.inherit.sound.start.FnnStartFun

class AdLim {
    fun canShowAd(isPostInt:Boolean): Boolean {
        val jsonBean = FnnStartFun.getAdminData() ?: return true
        val maxHourlyShows = jsonBean.config.adLimits.getOrNull(0)?:0
        val maxDailyShows = jsonBean.config.adLimits.getOrNull(1)?:0
        val maxClicks = jsonBean.config.adLimits.getOrNull(2)?:0
        XianUtils.init(maxHourlyShows, maxDailyShows,maxClicks)
        return XianUtils.canShowAd(isPostInt)
    }

    // 记录广告展示
     fun recordAdShown() {
        XianUtils.recordAdShown()
    }
    fun recordAdClick() {
        XianUtils.recordAdClick()
    }
}
