package com.example.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

object AlarmScheduler {
    
    fun scheduleAlarm(context: Context, task: Task) {
        // If alarm is disabled, or task is complete/rung, do not schedule
        if (!task.isAlarmEnabled || task.alarmRung || task.isCompleted) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", task.title)
            putExtra("TASK_DESC", task.description)
            putExtra("TASK_CATEGORY", task.category)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = task.dueDate

        // Verify scheduled time is in the future
        if (triggerTime <= System.currentTimeMillis()) {
            Log.d("AlarmScheduler", "Target time is in the past, skipping exact alarm for task ID ${task.id}")
            return
        }

        Log.d("AlarmScheduler", "Scheduling alarm for Task ID ${task.id} (${task.title}) at $triggerTime")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error scheduling exact alarm for task ID ${task.id}", e)
            try {
                // Fallback to non-exact trigger
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } catch (ex: Exception) {
                Log.e("AlarmScheduler", "Error scheduling fallback alarm for task ID ${task.id}", ex)
            }
        }
    }

    fun cancelAlarm(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            Log.d("AlarmScheduler", "Canceling active alarm for Task ID ${task.id}")
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
