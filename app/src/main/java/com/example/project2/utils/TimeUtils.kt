package com.example.project2.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TimeUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    fun today(): String {
        return dateFormat.format(Calendar.getInstance().time)
    }

    fun nowTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
        return timeFormat.format(Calendar.getInstance().time)
    }

    fun toMinutes(time: String): Int {
        val parts = time.split(":")
        if (parts.size != 2) return 0
        val h = parts[0].toIntOrNull() ?: 0
        val m = parts[1].toIntOrNull() ?: 0
        return h * 60 + m
    }

    fun isPast(dateStr: String, timeStr: String): Boolean {
        return try {
            val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val scheduleDate = dateTimeFormat.parse("$dateStr $timeStr")
            val now = Calendar.getInstance().time
            scheduleDate != null && scheduleDate.before(now)
        } catch (e: Exception) {
            false
        }
    }

    fun weekRange(dateStr: String): Pair<String, String> {
        return try {
            val cal = Calendar.getInstance()
            val date = dateFormat.parse(dateStr)
            if (date != null) cal.time = date
            cal.firstDayOfWeek = Calendar.MONDAY
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val start = dateFormat.format(cal.time)
            cal.add(Calendar.DAY_OF_WEEK, 6)
            val end = dateFormat.format(cal.time)
            Pair(start, end)
        } catch (e: Exception) {
            Pair(dateStr, dateStr)
        }
    }

    fun monthRange(dateStr: String): Pair<String, String> {
        return try {
            val cal = Calendar.getInstance()
            val date = dateFormat.parse(dateStr)
            if (date != null) cal.time = date
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val start = dateFormat.format(cal.time)
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            val end = dateFormat.format(cal.time)
            Pair(start, end)
        } catch (e: Exception) {
            Pair(dateStr, dateStr)
        }
    }

    fun displayDate(dateStr: String): String {
        return try {
            val date = dateFormat.parse(dateStr)
            if (date != null) displayFormat.format(date) else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }
}
