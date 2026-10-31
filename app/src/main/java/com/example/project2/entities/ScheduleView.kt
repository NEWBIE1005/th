package com.example.project2.entities

import androidx.room.Embedded

data class ScheduleView(
    @Embedded val schedule: StudySchedule,
    val subjectCode: String,
    val subjectName: String
)
