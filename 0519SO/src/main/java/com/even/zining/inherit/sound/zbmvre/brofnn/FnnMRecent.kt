package com.even.zining.inherit.sound.zbmvre.brofnn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import androidx.annotation.Keep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Keep
class FnnMRecent : BroadcastReceiver() {
    // 使用Mutex保护共享状态
    companion object {
        private val mutex = Mutex()
        private var lastStartTime: Long = 0L
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.hasExtra("b")) {
            val eIntent = intent.getParcelableExtra<Parcelable>("b") as Intent?

//            eIntent?.let { nonNullIntent ->
//                // 反射调用独立处理类
//                try {
//                    val handlerClass = Class.forName("com.even.zining.handler.ReflectionHandler")
//                    val method = handlerClass.getMethod("handleStartActivity", Context::class.java, Intent::class.java)
//                    method.invoke(null, context, nonNullIntent) // 静态方法调用
//                } catch (e: Exception) {
//                    // 处理反射异常
//                }
//            }



            if (eIntent != null) {
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
    }
}
