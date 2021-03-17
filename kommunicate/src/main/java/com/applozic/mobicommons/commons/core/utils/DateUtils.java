package com.applozic.mobicommons.commons.core.utils;

import android.content.Context;
import android.os.SystemClock;

import com.applozic.mobicommons.ApplozicService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by devashish on 28/11/14.
 */
public class DateUtils {

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

    public static String getFormattedDate(Long timestamp) {
        // boolean sameDay = isSameDay(timestamp);
        Date date = new Date(timestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aa");
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd MMM");
        return simpleDateFormat.format(date);
    }

    public static String getDate(Long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd MMM yyyy");
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

    public static String getFormattedDateAndTime(Context context, Long timestamp, int justNow, int min, int hr) {
        boolean sameDay = isSameDay(timestamp);
        Date date = new Date(timestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aa");
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd MMM");
        Date newDate = new Date();

        try {
            if (sameDay) {
                long currentTime = newDate.getTime() - date.getTime();
                long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(currentTime);
                long diffHours = TimeUnit.MILLISECONDS.toHours(currentTime);
                if (diffMinutes <= 1 && diffHours == 0) {
                    return Utils.getString(context, justNow);
                }
                if (diffMinutes <= 59 && diffHours == 0) {
                    return ApplozicService.getContext(context).getResources().getQuantityString(min, (int) diffMinutes, diffMinutes);
                }

                if (diffMinutes > 59 && diffHours <= 2) {
                    return ApplozicService.getContext(context).getResources().getQuantityString(hr, (int) diffHours, diffHours);
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy");

        try {
            if (sameDay) {
                Date newDate = new Date();
                long currentTime = newDate.getTime() - date.getTime();
                long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(currentTime);
                long diffHours = TimeUnit.MILLISECONDS.toHours(currentTime);
                if (diffMinutes <= 1 && diffHours == 0) {
                    return ApplozicService.getContext(context).getString(justNow);
                }
                if (diffMinutes <= 59 && diffHours == 0) {
                    return ApplozicService.getContext(context).getResources().getQuantityString(minAgo, (int) diffMinutes, diffMinutes);
                }
                if (diffMinutes > 59 && diffHours < 24) {
                    return ApplozicService.getContext(context).getResources().getQuantityString(hrAgo, (int) diffHours, diffHours);
                }
            }
            if (isYesterday(timestamp)) {
                return ApplozicService.getContext(context).getString(yesterday);
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
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
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

    public static String getDateAndTimeInDefaultFormat(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy hh:mm aa");
        return simpleDateFormat.format(date);
    }


}
