package com.even.zining.inherit.sound.zbmvre.allpro


import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Keep
object ReflectionHandler {
    private val mutex = Mutex()
    private var lastStartTime: Long = 0L

    @Suppress("unused") // 供反射调用
    @Keep
    fun handleStartActivity(context: Context, eIntent: Intent) {
        CoroutineScope(Dispatchers.Main).launch {
            mutex.withLock {
                val now = System.currentTimeMillis()
                if (now - lastStartTime >= 1000) {
                    try {
                        context.startActivity(eIntent)
                        lastStartTime = now
                    } catch (e: Exception) {
                        // 异常处理逻辑
                    }
                }
            }
        }
    }
}
