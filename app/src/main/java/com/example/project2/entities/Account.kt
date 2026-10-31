package com.example.project2.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Account",
    indices = [Index(value = ["username"], unique = true)]
)
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val password: String,
    val role: String // "ADMIN" | "STUDENT"
)
