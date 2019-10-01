package io.kommunicate.services;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.applozic.mobicomkit.database.MobiComDatabaseHelper;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.people.channel.ChannelUserMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KmChannelService {

    private static final String CHANNEL_USER_X = "channel_User_X";
    private static KmChannelService kmChannelService;
    private MobiComDatabaseHelper dbHelper;

    private KmChannelService(Context context) {
        this.dbHelper = MobiComDatabaseHelper.getInstance(context);
    }

    public static KmChannelService getInstance(Context context) {
        if (kmChannelService == null) {
            kmChannelService = new KmChannelService(ApplozicService.getContext(context));
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

    public Set<String> getListOfUsersByRole(Integer channelKey, int role) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String query = "select * from " + CHANNEL_USER_X + " where channelKey = " + String.valueOf(channelKey) + " and role = " + String.valueOf(role);
            cursor = db.rawQuery(query, null);
            return getListOfUserIds(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }

    public static Set<String> getListOfUserIds(Cursor cursor) {
        Set<String> userIdList = new HashSet<>();
        try {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    userIdList.add(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.USERID)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userIdList;
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
