package com.applozic.mobicomkit.api.conversation.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.database.MobiComDatabaseHelper;

/**
 * Created with IntelliJ IDEA.
 * User: anshul
 * Date: 1/26/14
 * Time: 12:32 AM
 */
public class ScheduledMessageUtil {
    Context context = null;
    private Class intentClass;

    public ScheduledMessageUtil(Context ctxt, Class intentClass) {
        this.context = ctxt;
        this.intentClass = intentClass;
    }

    public void createScheduleMessage(Message message, Context ctx) {
        MobiComDatabaseHelper dbHelper = MobiComDatabaseHelper.getInstance(ctx);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MobiComDatabaseHelper.TO_FIELD, message.getTo());
        values.put(MobiComDatabaseHelper.SMS, message.getMessage());
        values.put(MobiComDatabaseHelper.TIMESTAMP, message.getScheduledAt());
        values.put(MobiComDatabaseHelper.SMS_TYPE, message.getType());
        values.put(MobiComDatabaseHelper.CONTACTID, message.getContactIds());
        values.put(MobiComDatabaseHelper.SMS_KEY_STRING, message.getKeyString());
        values.put(MobiComDatabaseHelper.TIME_TO_LIVE, message.getTimeToLive());
        database.insert(MobiComDatabaseHelper.SCHEDULE_SMS_TABLE_NAME, null, values);
        AlarmManager alarm;
        PendingIntent intent;
        Intent otherIntent = new Intent();
        otherIntent.setClass(ctx, intentClass);
        alarm = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        intent = PendingIntent.getService(ctx,
                (int) System.currentTimeMillis(), otherIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.set(AlarmManager.RTC_WAKEUP, message.getScheduledAt(), intent);
        dbHelper.close();
    }

}
