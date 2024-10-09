package io.kommunicate.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class KmDateUtils {

    public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DEFAULT_TIME_FORMAT_24 = "HH:mm";
    public static final String DEFAULT_TIME_FORMAT_12 = "hh:mm aa";

    private static final String FORM_SERIALISED_DATE_FORMAT = "yyyy-MM-dd";
    private static final String FORM_SERIALISED_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm";


    public static String getLocalisedDateFormat() {
        return DEFAULT_DATE_FORMAT;
    }

    public static String getLocalisedDateTimeFormat(boolean isAmPm) {
        return getLocalisedDateFormat() + " " + getTimeFormat(isAmPm);
    }

    public static String getFormattedDate(Long timeInMillis) {
        return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(new Date(timeInMillis));
    }

    public static String getFormattedTime(Long timeInMillis, boolean isAmPm) {
        return new SimpleDateFormat(isAmPm ? DEFAULT_TIME_FORMAT_12 : DEFAULT_TIME_FORMAT_24, Locale.getDefault()).format(new Date(timeInMillis));
    }

    public static String getFormattedDateTime(Long timeInMillis, boolean isAmPm) {
        return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(new Date(timeInMillis)) + " " + getFormattedTime(timeInMillis, isAmPm);
    }

    public static String getTimeFormat(boolean isAmPm) {
        return isAmPm ? DEFAULT_TIME_FORMAT_12 : DEFAULT_TIME_FORMAT_24;
    }

    public static String getFormSerialisedDateFormat(Long timeStamp) {
        return new SimpleDateFormat(FORM_SERIALISED_DATE_FORMAT, Locale.getDefault()).format(new Date(timeStamp));
    }

    public static String getFormSerialisedTimeFormat(Long timeStamp) {
        return new SimpleDateFormat(DEFAULT_TIME_FORMAT_24, Locale.getDefault()).format(new Date(timeStamp));
    }

    public static String getFormSerialisedDateTimeFormat(Long timeStamp) {
        return new SimpleDateFormat(FORM_SERIALISED_DATE_TIME_FORMAT, Locale.getDefault()).format(new Date(timeStamp));
    }
}
