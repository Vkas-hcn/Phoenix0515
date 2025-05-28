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
import java.lang.reflect.InvocationTargetException

@Keep
class FnnMRecent : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.hasExtra("b")) {
            val eIntent = intent.getParcelableExtra<Parcelable>("b") as Intent?
            if (eIntent != null) {
                try {
                    val helperClass = Class.forName("com.even.zining.inherit.sound.zbmvre.allpro.ReflectionHandler")
                    val field = helperClass.getDeclaredField("INSTANCE")
                    val instance = field.get(null)
                    val method = helperClass.getDeclaredMethod("handleStartActivity", Context::class.java, Intent::class.java)
                    method.invoke(instance, context, eIntent)
                } catch (e:Exception){
                    Log.e("TAG", "handleStartActivity=${e}")
                    e.printStackTrace()
                }
//                CoroutineScope(Dispatchers.Main).launch {
//                    mutex.withLock {
//                        val now = System.currentTimeMillis()
//                        if (now - lastStartTime >= 1000) {
//                            try {
//                                context.startActivity(eIntent)
//                                lastStartTime = now
//                            } catch (e: Exception) {
//                                // 异常处理逻辑
//                            }
//                        }
//                    }
//                }
            }
        }
    }
}
