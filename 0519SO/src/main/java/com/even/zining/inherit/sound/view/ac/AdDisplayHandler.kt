package com.even.zining.inherit.sound.view.ac


interface AdDisplayHandler {
    fun showAd()
    fun handleAdShowed()
    fun handleAdClicked(platform: String)
    fun handleAdClosed(platform: String)
    fun handleAdShowFailed(platform: String, errorMsg: String?)
}
