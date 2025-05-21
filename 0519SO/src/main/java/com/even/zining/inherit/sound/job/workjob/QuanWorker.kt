package com.even.zining.inherit.sound.job.workjob

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.even.zining.inherit.sound.start.FnnStartFun

class QuanWorker(context: Context, workerParams: WorkerParameters)
    : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        FnnStartFun.showLog( "LoopWorker 执行")
        FnnStartFun.enqueueSelfLoop()
        return Result.success()
    }
}
