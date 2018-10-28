package com.applozic.mobicomkit.uiwidgets;

import android.content.Context;
import android.os.SystemClock;

import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.commons.core.utils.SntpClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class KmDateUtils extends DateUtils {

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

    public static String getFormattedDate(Context context, Long timestamp) {
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

    public static String getFormattedDateAndTime(Context context, Long timestamp, int justNow, int min, int hr) {
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
                if (diffMinutes <= 1 && diffHours == 0) {
                    return context.getString(justNow);
                }
                if (diffMinutes <= 59 && diffHours == 0) {
                    return context.getResources().getQuantityString(min, (int) diffMinutes, diffMinutes);
                }

                if (diffMinutes > 59 && diffHours <= 2) {
                    return context.getResources().getQuantityString(hr, (int) diffHours, diffHours);
                }
                return simpleDateFormat.format(date);
            }
            return fullDateFormat.format(date);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getDateAndTimeForLastSeen(Context context, Long timestamp, int justNow, int minAgo, int hrAgo, int yesterday) {
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
                    return context.getString(justNow);
                }
                if (diffMinutes <= 59 && diffHours == 0) {
                    return context.getResources().getQuantityString(minAgo, (int) diffMinutes, diffMinutes);
                }
                if (diffMinutes > 59 && diffHours < 24) {
                    return context.getResources().getQuantityString(hrAgo, (int) diffHours, diffHours);
                }
            }
            if (isYesterday(timestamp)) {
                return context.getString(yesterday);
            }
            return simpleDateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static String getDateAndTimeInDefaultFormat(Context context, long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(context.getString(is24hourFormat(context) ? R.string.DATE_TIME_24H_FULL_FORMAT
                : R.string.DATE_TIME_FULL_FORMAT), Locale.getDefault());
        return simpleDateFormat.format(date);
    }
}
