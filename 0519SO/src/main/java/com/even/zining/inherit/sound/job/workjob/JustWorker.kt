package com.even.zining.inherit.sound.job.workjob

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class JustWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return Result.success()
    }
}