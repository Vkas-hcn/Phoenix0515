package com.even.zining.inherit.sound.zbmvre.fnnserv

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.widget.RemoteViews
import com.even.zining.inherit.sound.R
import com.even.zining.inherit.sound.start.FnnStartFun
import com.even.zining.inherit.sound.start.newfun.Logger
import com.even.zining.inherit.sound.tool.PngCanGo.KEY_IS_SERVICE

class FnnFService : Service() {
    @SuppressLint("ForegroundServiceType", "RemoteViewLayout")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.showLog("FebFiveFffService onStartCommand-1=${KEY_IS_SERVICE}")
        if (!KEY_IS_SERVICE) {
            KEY_IS_SERVICE = true
            val channel =
                NotificationChannel("bros", "browser", NotificationManager.IMPORTANCE_MIN)
            channel.setSound(null, null)
            channel.enableLights(false)
            channel.enableVibration(false)
            (application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).run {
                if (getNotificationChannel(channel.toString()) == null) createNotificationChannel(
                    channel
                )
            }
            runCatching {
                startForeground(
                    5600,
                    NotificationCompat.Builder(this, "bros").setSmallIcon(R.drawable.ces_show)
                        .setContentText("")
                        .setContentTitle("")
                        .setOngoing(true)
                        .setCustomContentView(RemoteViews(packageName, R.layout.layout_no))
                        .build()
                )
            }
            Logger.showLog("FebFiveFffService onStartCommand-2=${KEY_IS_SERVICE}")
        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


}
