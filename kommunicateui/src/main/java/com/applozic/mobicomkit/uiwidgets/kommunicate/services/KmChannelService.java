package com.applozic.mobicomkit.uiwidgets.kommunicate.services;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.applozic.mobicomkit.database.MobiComDatabaseHelper;
import com.applozic.mobicommons.people.channel.ChannelUserMapper;

import java.util.ArrayList;
import java.util.List;

public class KmChannelService {

    private static final String CHANNEL_USER_X = "channel_User_X";
    private static KmChannelService kmChannelService;
    private MobiComDatabaseHelper dbHelper;

    private KmChannelService(Context context) {
        this.dbHelper = MobiComDatabaseHelper.getInstance(context);
    }

    public static KmChannelService getInstance(Context context) {
        if (kmChannelService == null) {
            kmChannelService = new KmChannelService(context.getApplicationContext());
        }
        return kmChannelService;
    }

    public String getUserInSupportGroup(Integer channelKey) {
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String query = "select * from " + CHANNEL_USER_X + " where channelKey = " + String.valueOf(channelKey) + " and role = " + String.valueOf(3);

            Cursor cursor = db.rawQuery(query, null);
            List<ChannelUserMapper> channelUserMappers = getListOfUsers(cursor);

            cursor.close();
            dbHelper.close();

            if (channelUserMappers == null || channelUserMappers.isEmpty()) {
                return "";
            }
            return channelUserMappers.get(0).getUserKey();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<ChannelUserMapper> getListOfUsers(Cursor cursor) {
        List<ChannelUserMapper> channelUserMapper = new ArrayList<ChannelUserMapper>();
        try {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    channelUserMapper.add(getChannelUser(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return channelUserMapper;
    }

    public static ChannelUserMapper getChannelUser(Cursor cursor) {
        ChannelUserMapper channelUserMapper = new ChannelUserMapper();
        channelUserMapper.setUserKey(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.USERID)));
        channelUserMapper.setKey(cursor.getInt(cursor.getColumnIndex(MobiComDatabaseHelper.CHANNEL_KEY)));
        channelUserMapper.setUnreadCount(cursor.getShort(cursor.getColumnIndex(MobiComDatabaseHelper.UNREAD_COUNT)));
        channelUserMapper.setRole(cursor.getInt(cursor.getColumnIndex(MobiComDatabaseHelper.ROLE)));
        channelUserMapper.setParentKey(cursor.getInt(cursor.getColumnIndex(MobiComDatabaseHelper.PARENT_GROUP_KEY)));
        return channelUserMapper;
    }
}
