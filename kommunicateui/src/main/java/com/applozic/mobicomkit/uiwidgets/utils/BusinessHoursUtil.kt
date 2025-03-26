package com.applozic.mobicomkit.uiwidgets.utils

import java.util.Calendar
import java.util.Date
import java.util.TimeZone


object BusinessHoursUtil {
    /**
     * Checks if the current time is within business hours
     * @return Pair of Boolean (is within business hours) and optional message
     */
    @JvmStatic
    fun isWithinBusinessHours(businessHourMap: Map<Int, String>, serverTimezone: String): Boolean {
        val serverZone = TimeZone.getTimeZone(serverTimezone)

        val calendar = Calendar.getInstance(serverZone)

        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> 7
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> -1
        }

        val businessHours = businessHourMap[dayOfWeek]

        if (businessHours.isNullOrBlank()) {
            return false
        }

        val (startTime, endTime) = parseBusinessHours(businessHours)

        val currentDate = Date()
        val adjustedCalendar = Calendar.getInstance(serverZone).apply {
            time = currentDate
        }

        val currentHour = adjustedCalendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = adjustedCalendar.get(Calendar.MINUTE)

        val currentTimeInMinutes = currentHour * 60 + currentMinute

        val isWithinHours = if (startTime <= endTime) {
            currentTimeInMinutes in startTime..endTime
        } else {
            currentTimeInMinutes >= startTime || currentTimeInMinutes <= endTime
        }

        return isWithinHours
    }

    /**
     * Parses business hours string in format "HHMM-HHMM"
     * @return Pair of start and end times in minutes since midnight
     */
    private fun parseBusinessHours(hoursString: String): Pair<Int, Int> {
        // Validate format: should be "HHMM-HHMM"
        if (!hoursString.matches(Regex("\\d{4}-\\d{4}"))) {
            return Pair(0, 0)
        }

        val startHourStr = hoursString.substring(0, 2).toInt().coerceIn(0, 23)
        val startMinStr = hoursString.substring(2, 4).toInt().coerceIn(0, 59)
        val endHourStr = hoursString.substring(5, 7).toInt().coerceIn(0, 23)
        val endMinStr = hoursString.substring(7, 9).toInt().coerceIn(0, 59)

        val startTime = startHourStr * 60 + startMinStr
        val endTime = endHourStr * 60 + endMinStr

        return Pair(startTime, endTime)
    }
}