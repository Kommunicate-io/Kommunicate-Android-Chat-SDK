package com.applozic.mobicomkit.uiwidgets.schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScheduledTimeHolder {
    String date = null;
    String time = null;
    Long timestamp = null;

    public ScheduledTimeHolder() {

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Long getTimestamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        if (this.getDate() == null || this.getTime() == null) {
            return timestamp;
        }
        Date d;
        try {
            d = formatter.parse(this.getDate() + " " + this.getTime());
            timestamp = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void resetScheduledTimeHolder() {
        this.date = null;
        this.time = null;
        this.timestamp = null;
    }
}
