package com.thunderbolt.methods.bodhisattva.ui

import android.app.Application
import android.content.Context
import com.even.zining.inherit.sound.start.FnnStartFun

class App:Application() {
    companion object{
        lateinit var instance: Context
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
        FnnStartFun.appInt(this)
    }
}