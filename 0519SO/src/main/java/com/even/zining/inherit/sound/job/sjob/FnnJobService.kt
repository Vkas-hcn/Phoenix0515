package com.even.zining.inherit.sound.job.sjob

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log

@SuppressLint("SpecifyJobSchedulerIdRange")
class FnnJobService : JobService() {

    @SuppressLint("SpecifyJobSchedulerIdRange")
    override fun onStartJob(params: JobParameters): Boolean {
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return true
    }
}