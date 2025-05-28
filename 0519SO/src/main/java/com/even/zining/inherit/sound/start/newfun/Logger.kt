package com.even.zining.inherit.sound.start.newfun

import android.util.Log
import com.even.zining.inherit.sound.start.FnnStartFun

object Logger {
    fun log(msg: String) {
        if (FnnStartFun.mustXS) return
        Log.e("Photo", msg)
    }

    fun showLog(msg: String) = log(msg)
}
