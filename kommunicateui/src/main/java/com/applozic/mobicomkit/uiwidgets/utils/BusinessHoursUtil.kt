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

        val isWithinHours = currentTimeInMinutes in startTime..endTime

        return isWithinHours
    }

    /**
     * Parses business hours string in format "HHMM-HHMM"
     * @return Pair of start and end times in minutes since midnight
     */
    private fun parseBusinessHours(hoursString: String): Pair<Int, Int> {
        val startHourStr = hoursString.substring(0, 2)
        val startMinStr = hoursString.substring(2, 4)
        val endHourStr = hoursString.substring(5, 7)
        val endMinStr = hoursString.substring(7, 9)

        val startTime = startHourStr.toInt() * 60 + startMinStr.toInt()
        val endTime = endHourStr.toInt() * 60 + endMinStr.toInt()

        return Pair(startTime, endTime)
    }
}