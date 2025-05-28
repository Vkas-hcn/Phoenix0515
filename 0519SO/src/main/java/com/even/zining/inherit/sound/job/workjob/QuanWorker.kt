package com.even.zining.inherit.sound.job.workjob

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.even.zining.inherit.sound.start.newfun.JobSchedulerManager.scheduleSelfLoop
import com.even.zining.inherit.sound.start.newfun.Logger.showLog

class QuanWorker(context: Context, workerParams: WorkerParameters)
    : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        showLog( "LoopWorker 执行")
        scheduleSelfLoop()
        return Result.success()
    }
}
