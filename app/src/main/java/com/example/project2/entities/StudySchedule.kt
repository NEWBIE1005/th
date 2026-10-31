package com.example.project2.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "StudySchedule",
    indices = [Index(value = ["studentId", "studyDate"])]
)
data class StudySchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val subjectId: Long,
    val studyDate: String, // "yyyy-MM-dd"
    val startTime: String, // "HH:mm"
    val endTime: String,   // "HH:mm"
    val location: String,
    val status: String     // "PENDING" | "CONFIRMED" | "CANCELLED"
)
