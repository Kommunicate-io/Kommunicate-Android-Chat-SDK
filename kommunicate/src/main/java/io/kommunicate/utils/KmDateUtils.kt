package io.kommunicate.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object KmDateUtils {
    private const val DEFAULT_DATE_FORMAT: String = "dd/MM/yyyy"
    private const val DEFAULT_TIME_FORMAT_24: String = "HH:mm"
    private const val DEFAULT_TIME_FORMAT_12: String = "hh:mm aa"
    private const val FORM_SERIALISED_DATE_FORMAT = "yyyy-MM-dd"
    private const val FORM_SERIALISED_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm"

    @JvmStatic
    fun getLocalisedDateFormat(): String {
        return DEFAULT_DATE_FORMAT
    }

    @JvmStatic
    fun getLocalisedDateTimeFormat(isAmPm: Boolean): String {
        return "$DEFAULT_DATE_FORMAT ${getTimeFormat(isAmPm)}"
    }

    @JvmStatic
    fun getFormattedDate(timeInMillis: Long = 0L): String {
        return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
            .format(Date(timeInMillis))
    }

    @JvmStatic
    fun getFormattedTime(timeInMillis: Long = 0L, isAmPm: Boolean): String {
        return SimpleDateFormat(
            if (isAmPm) DEFAULT_TIME_FORMAT_12 else DEFAULT_TIME_FORMAT_24,
            Locale.getDefault()
        ).format(Date(timeInMillis))
    }

    @JvmStatic
    fun getFormattedDateTime(timeInMillis: Long = 0L, isAmPm: Boolean): String {
        val date = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
            .format(Date(timeInMillis))
        return "$date ${getFormattedTime(timeInMillis, isAmPm)}"
    }

    @JvmStatic
    fun getTimeFormat(isAmPm: Boolean): String {
        return if (isAmPm) DEFAULT_TIME_FORMAT_12 else DEFAULT_TIME_FORMAT_24
    }

    @JvmStatic
    fun getFormSerialisedDateFormat(timeStamp: Long = 0L): String {
        return SimpleDateFormat(FORM_SERIALISED_DATE_FORMAT, Locale.getDefault())
            .format(Date(timeStamp)
        )
    }

    @JvmStatic
    fun getFormSerialisedTimeFormat(timeStamp: Long = 0L): String {
        return SimpleDateFormat(DEFAULT_TIME_FORMAT_24, Locale.getDefault())
            .format(Date(timeStamp))
    }

    @JvmStatic
    fun getFormSerialisedDateTimeFormat(timeStamp: Long = 0L): String {
        return SimpleDateFormat(FORM_SERIALISED_DATE_TIME_FORMAT, Locale.getDefault())
            .format(Date(timeStamp))
    }
}
