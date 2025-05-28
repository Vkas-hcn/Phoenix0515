package com.even.zining.inherit.sound.start.newfun

import com.even.zining.inherit.sound.start.FnnStartFun
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.tool.data.MMKVUtils
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
object FirebaseManager {
    fun getFcmFun() {
        if (!FnnStartFun.mustXS || MMKVUtils.getBoolean(FnnLoadData.fcmState)) return

        runCatching {
            Firebase.messaging.subscribeToTopic(FnnLoadData.fffmmm)
                .addOnSuccessListener {
                    MMKVUtils.put(FnnLoadData.fcmState, true)
                    Logger.log("Firebase: subscribe success")
                }
                .addOnFailureListener {
                    Logger.log("Firebase: subscribe fail")
                }
        }
    }
}
