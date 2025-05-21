package com.even.zining.inherit.sound.start


import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.even.zining.inherit.sound.start.FnnStartFun.isDigitSumEven
import com.even.zining.inherit.sound.view.ac.SoShowAc
import com.even.zining.inherit.sound.tool.PngCanGo.KEY_IS_SERVICE
import com.even.zining.inherit.sound.tool.NetPostTool
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.tool.PngCanGo
import com.even.zining.inherit.sound.zbmvre.fnnserv.FnnFService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Keep
class LifeServiceShow : Application.ActivityLifecycleCallbacks {
    private var foregroundActivityCount = 0

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        PngCanGo.addActivity(activity)
        if (!KEY_IS_SERVICE) {
            try {
                ContextCompat.startForegroundService(
                    activity,
                    Intent(activity, FnnFService::class.java)
                )
            } catch (e: Exception) {
            }
        }
    }

    override fun onActivityStarted(activity: Activity) {
        foregroundActivityCount++
        if (activity is SoShowAc) {
            return
        }
        if (activity.javaClass.name.contains(FnnLoadData.reladRu)) {
            FnnStartFun.showLog("onActivityStarted=${activity.javaClass.name}")
            val anTime = PngCanGo.getInstallTimeDataFun()
            NetPostTool.postPointData(false, "session_front", "time", anTime)
        }
    }

    override fun onActivityResumed(activity: Activity) {


    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        foregroundActivityCount--
        if (foregroundActivityCount <= 0) {
            closeAllActivities()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        PngCanGo.removeActivity(activity)
    }

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
    }
    private fun closeAllActivities() {
        val isaData = FnnStartFun.getAdminData() ?: return
        if (isaData != null && isaData.config.user.isUploader.isDigitSumEven()) {
            //协程
            Log.e("TAG", "closeAllActivities: 进入APP后台")
            ArrayList(PngCanGo.activityList).forEach { it.finishAndRemoveTask() }
        }
    }
}
