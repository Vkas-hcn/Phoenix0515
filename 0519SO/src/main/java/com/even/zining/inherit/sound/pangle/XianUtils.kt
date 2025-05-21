package com.even.zining.inherit.sound.pangle


import com.even.zining.inherit.sound.start.FnnStartFun
import com.even.zining.inherit.sound.tool.TbaPostTool
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.tool.data.MMKVUtils
import com.even.zining.inherit.sound.tool.NetPostTool
import java.text.SimpleDateFormat
import java.util.*

object XianUtils {
    private var MAX_HOURLY_SHOWS = 0
    private var MAX_DAILY_SHOWS = 0
    private var MAX_CLICKS = 0

    fun init(maxHourlyShows: Int, maxDailyShows: Int, maxClicks: Int) {
        MAX_HOURLY_SHOWS = maxHourlyShows
        MAX_DAILY_SHOWS = maxDailyShows
        MAX_CLICKS = maxClicks
    }

    // 检查是否可以展示广告
    fun canShowAd(isPostInt: Boolean): Boolean {
        // 检查每日展示限制
        if (!checkDailyShowLimit()) {
            if (isPostInt) {
                NetPostTool.postPointData(false, "ispass", "string", "dayShowLimit")
                TbaPostTool.getLiMitData()
            }
            return false
        }
        // 检查点击限制
        if (!checkClickLimit()) {
            if (isPostInt) {
                NetPostTool.postPointData(false, "ispass", "string", "dayClickLimit")
                TbaPostTool.getLiMitData()
            }

            return false
        }
        // 检查小时限制
        if (!checkHourLimit()) {
            if (isPostInt) {
                NetPostTool.postPointData(false, "ispass", "string", "hourShowLimit")
            }
            return false
        }

        return true
    }

    // 记录广告展示
    fun recordAdShown() {
        FnnStartFun.showLog("记录插屏广告展示")
        // 更新小时计数
        updateHourCount()
        // 更新每日展示计数
        updateDailyShowCount()
    }

    fun recordAdClick() {
        FnnStartFun.showLog("记录插屏广告点击")
        // 更新点击计数
        updateClickCount()
    }

    private fun checkHourLimit(): Boolean {
        val currentHour = getCurrentHourString()
        val lastHour = MMKVUtils.getString(FnnLoadData.adHourShowDate)
        val hourCount = MMKVUtils.getInt(FnnLoadData.adHourShowNum)
        // 如果进入新小时段则重置计数
        if (currentHour != lastHour) {
            MMKVUtils.put(FnnLoadData.adHourShowDate, currentHour)
            MMKVUtils.put(FnnLoadData.adHourShowNum, 0)

            FnnStartFun.showLog("插屏-小时展示数重置")
            return true
        }
        FnnStartFun.showLog("插屏-小时展示数=$hourCount ----小时最大展示数=${MAX_HOURLY_SHOWS}")
        return hourCount < MAX_HOURLY_SHOWS
    }

    private fun checkDailyShowLimit(): Boolean {
        val currentDate = getCurrentDateString()
        val lastDate = MMKVUtils.getString(FnnLoadData.adDayShowDate)
        val dailyCount = MMKVUtils.getInt(FnnLoadData.adDayShowNum)

        // 如果进入新日期则重置计数
        if (currentDate != lastDate) {
            MMKVUtils.put(FnnLoadData.adDayShowDate, currentDate)
            MMKVUtils.put(FnnLoadData.adDayShowNum, 0)
            MMKVUtils.put(FnnLoadData.adClickNum, 0)
            MMKVUtils.put(FnnLoadData.getlimit, false)
            FnnStartFun.showLog("插屏-天展示数重置")
            return true
        }
        FnnStartFun.showLog("插屏-天展示数=$dailyCount ----天最大展示数=${MAX_DAILY_SHOWS}")
        return dailyCount < MAX_DAILY_SHOWS
    }

    private fun checkClickLimit(): Boolean {
        val clickCount = MMKVUtils.getInt(FnnLoadData.adClickNum)
        FnnStartFun.showLog("插屏-点击数=$clickCount ----点击最大展示数=${MAX_CLICKS}")
        return clickCount < MAX_CLICKS
    }

    private fun updateHourCount() {
        val currentHour = getCurrentHourString()
        val lastHour = MMKVUtils.getString(FnnLoadData.adHourShowDate)
        if (currentHour == lastHour) {
            val num = MMKVUtils.getInt(FnnLoadData.adHourShowNum) + 1
            MMKVUtils.put(FnnLoadData.adHourShowNum, num)
        } else {
            MMKVUtils.put(FnnLoadData.adHourShowDate, currentHour)
            MMKVUtils.put(FnnLoadData.adHourShowNum, 1)
        }
    }

    private fun updateDailyShowCount() {
        val currentDate = getCurrentDateString()
        val lastDate = MMKVUtils.getString(FnnLoadData.adDayShowDate)
        if (currentDate == lastDate) {
            val newCount = MMKVUtils.getInt(FnnLoadData.adDayShowNum) + 1
            MMKVUtils.put(FnnLoadData.adDayShowNum, newCount)

        } else {
            MMKVUtils.put(FnnLoadData.adDayShowDate, currentDate)
            MMKVUtils.put(FnnLoadData.adDayShowNum, 1)
        }
    }

    private fun updateClickCount() {
        val newCount = MMKVUtils.getInt(FnnLoadData.adClickNum) + 1
        MMKVUtils.put(FnnLoadData.adClickNum, newCount)

    }

    private fun getCurrentHourString() =
        SimpleDateFormat("yyyyMMddHH", Locale.getDefault()).format(Date())

    private fun getCurrentDateString() =
        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
}
