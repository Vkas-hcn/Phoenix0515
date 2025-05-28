package com.even.zining.inherit.sound.start.newfun


import androidx.work.PeriodicWorkRequestBuilder
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.even.zining.inherit.sound.job.sjob.FnnJobService
import com.even.zining.inherit.sound.job.workjob.JustWorker
import com.even.zining.inherit.sound.job.workjob.QuanWorker
import com.even.zining.inherit.sound.start.FnnStartFun
import java.util.concurrent.TimeUnit
object JobSchedulerManager {
    fun schedulePeriodicJobs() {
        schedulePeriodicChain()
        scheduleSelfLoop()
        schedulePeriodicJob()
    }

    private fun schedulePeriodicChain() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<JustWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(FnnStartFun.mainStart).enqueueUniquePeriodicWork(
            "just_work",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWork
        )
    }

     fun scheduleSelfLoop() {
        val work = OneTimeWorkRequestBuilder<QuanWorker>()
            .setInitialDelay(2, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(FnnStartFun.mainStart).enqueue(work)
    }

    private fun schedulePeriodicJob() {
        val jobScheduler = FnnStartFun.mainStart.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(FnnStartFun.mainStart, FnnJobService::class.java)
        val jobInfo = JobInfo.Builder(44778, componentName)
            .setPeriodic(15 * 60 * 1000)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
            .setPersisted(true)
            .build()

        jobScheduler.schedule(jobInfo)
    }
}
