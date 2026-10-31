package com.example.project2.utils

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    const val REMINDER_MINUTES = 15

    fun schedule(context: Context, scheduleId: Long, studyDate: String, startTime: String) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val scheduleTime = sdf.parse("$studyDate $startTime") ?: return
            val triggerTime = scheduleTime.time - (REMINDER_MINUTES * 60 * 1000)
            val delay = triggerTime - System.currentTimeMillis()

            if (delay <= 0) return

            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf("SCHEDULE_ID" to scheduleId))
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "reminder_$scheduleId",
                ExistingWorkPolicy.REPLACE,
                request
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancel(context: Context, scheduleId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder_$scheduleId")
    }
}
