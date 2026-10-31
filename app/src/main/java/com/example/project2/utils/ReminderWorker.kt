package com.example.project2.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.project2.dal.AppDB

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val scheduleId = inputData.getLong("SCHEDULE_ID", -1L)
        if (scheduleId == -1L) return Result.success()

        val db = AppDB.getInstance(applicationContext)
        val scheduleView = db.dao().getScheduleViewById(scheduleId)

        if (scheduleView != null && scheduleView.schedule.status != "CANCELLED") {
            val subjectName = scheduleView.subjectName
            val startTime = scheduleView.schedule.startTime
            val location = scheduleView.schedule.location
            NotificationHelper.show(
                applicationContext,
                "Sắp đến giờ học",
                "$subjectName lúc $startTime tại $location"
            )
        }

        return Result.success()
    }
}
