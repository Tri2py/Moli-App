package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // e.g., "Work", "Personal", "Design", "Focus"
    val priority: String, // e.g., "High", "Medium", "Low"
    val dueDate: Long,
    val isCompleted: Boolean = false,
    val isAlarmEnabled: Boolean = false,
    val alarmRung: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
