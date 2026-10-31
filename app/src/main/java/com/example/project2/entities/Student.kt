package com.example.project2.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Student",
    indices = [
        Index(value = ["studentCode"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentCode: String,
    val fullname: String,
    val email: String,
    val phone: String,
    val accountId: Long
)
