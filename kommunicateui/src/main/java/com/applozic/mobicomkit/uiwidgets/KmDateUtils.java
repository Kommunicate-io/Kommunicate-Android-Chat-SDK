package com.applozic.mobicomkit.uiwidgets;

import android.content.Context;
import android.os.SystemClock;

import com.applozic.mobicommons.commons.core.utils.SntpClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class KmDateUtils {

    public static boolean isSameDay(Long timestamp) {
        Calendar calendarForCurrent = Calendar.getInstance();
        Calendar calendarForScheduled = Calendar.getInstance();
        Date currentDate = new Date();
        Date date = new Date(timestamp);
        calendarForCurrent.setTime(currentDate);
        calendarForScheduled.setTime(date);
        return calendarForCurrent.get(Calendar.YEAR) == calendarForScheduled.get(Calendar.YEAR) &&
                calendarForCurrent.get(Calendar.DAY_OF_YEAR) == calendarForScheduled.get(Calendar.DAY_OF_YEAR);
    }

    public static String getFormattedDate(Long timestamp, Context context) {
        Date date = new Date(timestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(context.getString(is24hourFormat(context) ? R.string.TIME_24H_FORMAT
                : R.string.TIME_FORMAT), Locale.getDefault());
        SimpleDateFormat fullDateFormat = new SimpleDateFormat(context.getString(R.string.DATE_SHORT_FORMAT), Locale.getDefault());
        return simpleDateFormat.format(date);
    }

    private static boolean is24hourFormat(Context context) {
        return android.text.format.DateFormat.is24HourFormat(context.getApplicationContext());
    }

    public static String getDate(Context context, Long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat fullDateFormat = new SimpleDateFormat(context.getString(R.string.DATE_LONG_FORMAT), Locale.getDefault());
        return fullDateFormat.format(date);
    }

    public static long getTimeDiffFromUtc() {
        SntpClient sntpClient = new SntpClient();
        long diff = 0;
        if (sntpClient.requestTime("0.africa.pool.ntp.org", 30000)) {
            long utcTime = sntpClient.getNtpTime() + SystemClock.elapsedRealtime() - sntpClient.getNtpTimeReference();
            diff = utcTime - System.currentTimeMillis();
        }
        return diff;
    }

    public static String getFormattedDateAndTime(Long timestamp, Context context) {
        boolean sameDay = isSameDay(timestamp);
        Date date = new Date(timestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(context.getString(is24hourFormat(context) ? R.string.TIME_24H_FORMAT
                : R.string.TIME_FORMAT), Locale.getDefault());
        SimpleDateFormat fullDateFormat = new SimpleDateFormat(context.getString(R.string.DATE_SHORT_FORMAT), Locale.getDefault());
        Date newDate = new Date();
        try {
            if (sameDay) {
                long currentTime = newDate.getTime() - date.getTime();
                long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(currentTime);
                long diffHours = TimeUnit.MILLISECONDS.toHours(currentTime);
                if (diffMinutes < 1 && diffHours == 0) {
                    return context.getString(R.string.JUST_NOW);
                }
                if (diffMinutes == 1 && diffHours == 0) {
                    return context.getString(R.string.MINUTE, String.valueOf(diffMinutes));
                }
                if (diffMinutes <= 59 && diffHours == 0) {
                    return context.getString(R.string.MINUTES, String.valueOf(diffMinutes));
                }
                if (diffHours == 1) {
                    return context.getString(R.string.H, String.valueOf(diffHours));
                }
                if (diffHours >= 2) {
                    return context.getString(R.string.HOURS, String.valueOf(diffHours));
                }
                return simpleDateFormat.format(date);
            }
            return fullDateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getDateAndTimeForLastSeen(Long timestamp, Context context) {
        boolean sameDay = isSameDay(timestamp);
        Date date = new Date(timestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(context.getString(R.string.DATE_FULL_FORMAT), Locale.getDefault());
        try {
            if (sameDay) {
                Date newDate = new Date();
                long currentTime = newDate.getTime() - date.getTime();
                long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(currentTime);
                long diffHours = TimeUnit.MILLISECONDS.toHours(currentTime);
                if (diffMinutes <= 1 && diffHours == 0) {
                    return context.getString(R.string.JUST_NOW);
                }
                if (diffMinutes <= 59 && diffHours == 0) {
                    return context.getString(R.string.MINUTES_AGO, String.valueOf(diffMinutes));
                }
                if (diffHours < 24) {
                    return context.getString(R.string.HOURS_AGO, String.valueOf(diffHours));
                }
            }
            if (isYesterday(timestamp)) {
                return context.getString(R.string.YESTERDAY);
            }
            return simpleDateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isYesterday(Long timestamp) {
        Calendar c1 = Calendar.getInstance();
        c1.add(Calendar.DAY_OF_YEAR, -1);
        Date date = new Date(timestamp);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(date);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public static Calendar getDatePart(Date date) {
        Calendar cal = Calendar.getInstance();       // get calendar instance
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
        cal.set(Calendar.MINUTE, 0);                 // set minute in hour
        cal.set(Calendar.SECOND, 0);                 // set second in minute
        cal.set(Calendar.MILLISECOND, 0);            // set millisecond in second
        return cal;                                  // return the date part
    }

    /**
     * This method also assumes endDate >= startDate
     **/
    public static long daysBetween(Date startDate, Date endDate) {
        Calendar sDate = getDatePart(startDate);
        Calendar eDate = getDatePart(endDate);

        long daysBetween = 0;
        while (sDate.before(eDate)) {
            sDate.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return daysBetween;
    }

    public static String getDateAndTimeInDefaultFormat(Context context, long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(context.getString(is24hourFormat(context) ? R.string.DATE_TIME_24H_FULL_FORMAT
                : R.string.DATE_TIME_FULL_FORMAT), Locale.getDefault());
        return simpleDateFormat.format(date);
    }
}
