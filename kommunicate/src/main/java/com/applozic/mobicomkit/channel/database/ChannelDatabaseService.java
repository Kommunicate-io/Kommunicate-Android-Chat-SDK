package com.applozic.mobicomkit.channel.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.database.MobiComDatabaseHelper;
import com.applozic.mobicomkit.feed.GroupInfoUpdate;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.ChannelUserMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunil on 28/12/15.
 */
public class ChannelDatabaseService {

    private static final String TAG = "ChannelDatabaseService";
    private static final String CHANNEL = "channel";
    private static final String CHANNEL_USER_X = "channel_User_X";
    private static ChannelDatabaseService channelDatabaseService;
    private Context context;
    private MobiComUserPreference mobiComUserPreference;
    private MobiComDatabaseHelper dbHelper;
    private static final String channelKey_userID = "channelKey=? AND userId= ?";
    private static final String channelName = "channelName";
    private static final String AND = " =? AND ";
    private static final String channelUser_channelKey = "channel c JOIN channel_User_X cu on c.channelKey = cu.channelKey";
    private static final String channelImageURL = "channelImageURL";
    private static final String channelImageLocalURI = "channelImageLocalURI";

    private ChannelDatabaseService(Context context) {
        this.context = ApplozicService.getContext(context);
        this.mobiComUserPreference = MobiComUserPreference.getInstance(ApplozicService.getContext(context));
        this.dbHelper = MobiComDatabaseHelper.getInstance(ApplozicService.getContext(context));
    }

    public synchronized static ChannelDatabaseService getInstance(Context context) {
        if (channelDatabaseService == null) {
            channelDatabaseService = new ChannelDatabaseService(ApplozicService.getContext(context));
        }
        return channelDatabaseService;
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

    public void addChannel(Channel channel) {
        try {
            ContentValues contentValues = prepareChannelValues(channel);
            dbHelper.getWritableDatabase().insertWithOnConflict(CHANNEL, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public ContentValues prepareChannelValues(Channel channel) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MobiComDatabaseHelper.CHANNEL_DISPLAY_NAME, channel.getName());
        contentValues.put(MobiComDatabaseHelper.CHANNEL_KEY, channel.getKey());
        contentValues.put(MobiComDatabaseHelper.CLIENT_GROUP_ID, channel.getClientGroupId());
        contentValues.put(MobiComDatabaseHelper.TYPE, channel.getType());
        contentValues.put(MobiComDatabaseHelper.NOTIFICATION_AFTER_TIME, channel.getNotificationAfterTime());
        contentValues.put(MobiComDatabaseHelper.DELETED_AT, channel.getDeletedAtTime());
        contentValues.put(MobiComDatabaseHelper.ADMIN_ID, channel.getAdminKey());
        Channel oldChannel = null;
        contentValues.put(MobiComDatabaseHelper.CHANNEL_IMAGE_URL, channel.getImageUrl());
        oldChannel = ChannelDatabaseService.getInstance(context).getChannelByChannelKey(channel.getKey());

        if (channel.getKmStatus() != 0) {
            contentValues.put(MobiComDatabaseHelper.CONVERSATION_STATUS, channel.getKmStatus());
        }
        if (oldChannel != null && !TextUtils.isEmpty(oldChannel.getImageUrl()) && !channel.getImageUrl().equals(oldChannel.getImageUrl())) {
            updateChannelLocalImageURI(channel.getKey(), null);
        }
        if (!TextUtils.isEmpty(channel.getLocalImageUri())) {
            contentValues.put(MobiComDatabaseHelper.CHANNEL_IMAGE_LOCAL_URI, channel.getLocalImageUri());
        }
        if (channel.getUserCount() != 0) {
            contentValues.put(MobiComDatabaseHelper.USER_COUNT, channel.getUserCount());
        }
        if (channel.getUnreadCount() != 0) {
            contentValues.put(MobiComDatabaseHelper.UNREAD_COUNT, channel.getUnreadCount());
        }
        if (channel.getMetadata() != null) {
            contentValues.put(MobiComDatabaseHelper.CHANNEL_META_DATA, GsonUtils.getJsonFromObject(channel.getMetadata(), Map.class));
            if (channel.getMetadata().containsKey(Channel.AL_CATEGORY)) {
                contentValues.put(MobiComDatabaseHelper.AL_CATEGORY, channel.getMetadata().get(Channel.AL_CATEGORY));
            }
        }
        contentValues.put(MobiComDatabaseHelper.PARENT_GROUP_KEY, channel.getParentKey());
        contentValues.put(MobiComDatabaseHelper.PARENT_CLIENT_GROUP_ID, channel.getParentClientGroupId());
        return contentValues;
    }

    public void addChannelUserMapper(ChannelUserMapper channelUserMapper) {
        try {
            ContentValues contentValues = prepareChannelUserMapperValues(channelUserMapper);
            dbHelper.getWritableDatabase().insertWithOnConflict(CHANNEL_USER_X, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public ContentValues prepareChannelUserMapperValues(ChannelUserMapper channelUserMapper) {
        ContentValues contentValues = new ContentValues();
        if (channelUserMapper != null) {
            if (channelUserMapper.getKey() != null) {
                contentValues.put(MobiComDatabaseHelper.CHANNEL_KEY, channelUserMapper.getKey());
            }
            if (channelUserMapper.getUserKey() != null) {
                contentValues.put(MobiComDatabaseHelper.USERID, channelUserMapper.getUserKey());
            }
            if (channelUserMapper.getUserKey() != null) {
                contentValues.put(MobiComDatabaseHelper.UNREAD_COUNT, channelUserMapper.getUnreadCount());
            }
            if (channelUserMapper.getStatus() != 0) {
                contentValues.put(MobiComDatabaseHelper.STATUS, channelUserMapper.getStatus());
            }
            contentValues.put(MobiComDatabaseHelper.ROLE, channelUserMapper.getRole());

            if (channelUserMapper.getParentKey() != null) {
                contentValues.put(MobiComDatabaseHelper.PARENT_GROUP_KEY, channelUserMapper.getParentKey());
            }
        }
        return contentValues;
    }

    public Channel getChannelByClientGroupId(String clientGroupId) {
        Channel channel = null;
        try {
            String structuredNameWhere = MobiComDatabaseHelper.CLIENT_GROUP_ID + " =?";
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(CHANNEL, null, structuredNameWhere, new String[]{String.valueOf(clientGroupId)}, null, null, null);
            try {
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        channel = getChannel(cursor);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                dbHelper.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return channel;
    }

    public Channel getChannelByChannelKey(final Integer channelKey) {
        Channel channel = null;
        try {
            String structuredNameWhere = MobiComDatabaseHelper.CHANNEL_KEY + " =?";
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(CHANNEL, null, structuredNameWhere, new String[]{String.valueOf(channelKey)}, null, null, null);
            try {
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        channel = getChannel(cursor);
                    }
                }

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                dbHelper.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channel;
    }

    public List<ChannelUserMapper> getChannelUserList(Integer channelKey) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String structuredNameWhere = "";

            structuredNameWhere += "channelKey = ?";
            cursor = db.query(CHANNEL_USER_X, null, structuredNameWhere, new String[]{String.valueOf(channelKey)}, null, null, null);
            return getListOfUsers(cursor);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
        return null;
    }

    public Map<Channel, Long> getChannelAndUserLastContactedByChannelKey(Integer channelKey) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String table = "channel c JOIN channel_User_X cu ON c.channelKey = cu.channelKey JOIN sms s ON c.channelKey = s.channelKey";
        String[] columns = {"c.channelKey", "MAX(s.createdAt) AS latestMessageTime"};
        String selection = "cu.userId = (SELECT userId FROM channel_User_X WHERE channelKey = ? AND role = ?)";
        String[] selectionArgs = {String.valueOf(channelKey), String.valueOf(User.RoleType.USER_ROLE.getValue())};
        String groupBy = "c.channelKey";
        String orderBy = "latestMessageTime DESC";
        Map<Channel, Long> resultMap = new LinkedHashMap<>();
        Cursor cursor = null;
        try{
            cursor = db.query(table,columns,selection,selectionArgs,groupBy,null,orderBy);
            if (cursor.moveToFirst()) {
                do {
                    Channel channel = getChannelByChannelKey(cursor.getInt(0));
                    long createdAt = cursor.getLong(1);
                    resultMap.put(channel, createdAt);
                } while (cursor.moveToNext());
            }
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            if(cursor != null){
                cursor.close();
            }
            dbHelper.close();
        }
        return resultMap;
    }

    public Channel getChannel(Cursor cursor) {
        Channel channel = new Channel();
        channel.setKey(cursor.getInt(cursor.getColumnIndex(MobiComDatabaseHelper.CHANNEL_KEY)));
        channel.setParentClientGroupId(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.PARENT_CLIENT_GROUP_ID)));
        channel.setClientGroupId(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.CLIENT_GROUP_ID)));
        channel.setName(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.CHANNEL_DISPLAY_NAME)));
        channel.setAdminKey(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.ADMIN_ID)));
        channel.setType(cursor.getShort(cursor.getColumnIndex(MobiComDatabaseHelper.TYPE)));
        channel.setImageUrl(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.CHANNEL_IMAGE_URL)));
        channel.setLocalImageUri(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.CHANNEL_IMAGE_LOCAL_URI)));
        int count = cursor.getInt(cursor.getColumnIndex(MobiComDatabaseHelper.UNREAD_COUNT));
        channel.setNotificationAfterTime(cursor.getLong(cursor.getColumnIndex(MobiComDatabaseHelper.NOTIFICATION_AFTER_TIME)));
        channel.setDeletedAtTime(cursor.getLong(cursor.getColumnIndex(MobiComDatabaseHelper.DELETED_AT)));
        channel.setParentKey(cursor.getInt(cursor.getColumnIndex(MobiComDatabaseHelper.PARENT_GROUP_KEY)));
        channel.setKmStatus(cursor.getInt(cursor.getColumnIndex(MobiComDatabaseHelper.CONVERSATION_STATUS)));
        String metadata = cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.CHANNEL_META_DATA));
        channel.setMetadata(((Map<String, String>) GsonUtils.getObjectFromJson(metadata, Map.class)));
        if (count > 0) {
            channel.setUnreadCount(count);
        }
        return channel;
    }

    public List<Channel> getAllChannels() {
        List<Channel> contactList = null;
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            cursor = db.query(CHANNEL, null, null, null, null, null, MobiComDatabaseHelper.CHANNEL_DISPLAY_NAME + " asc");
            contactList = getChannelList(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
        return contactList;
    }

    public List<Channel> getChannelList(Cursor cursor) {
        try {
            List<Channel> channelList = new ArrayList<Channel>();
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    channelList.add(getChannel(cursor));
                } while (cursor.moveToNext());
            }
            return channelList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void updateChannel(Channel channel) {
        try {
            ContentValues contentValues = prepareChannelValues(channel);
            dbHelper.getWritableDatabase().update(CHANNEL, contentValues, MobiComDatabaseHelper.CHANNEL_KEY + "=?", new String[]{String.valueOf(channel.getKey())});
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public void updateNotificationAfterTime(Integer id, Long notificationAfterTime) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MobiComDatabaseHelper.NOTIFICATION_AFTER_TIME, notificationAfterTime);
            dbHelper.getWritableDatabase().update(CHANNEL, contentValues, MobiComDatabaseHelper.CHANNEL_KEY + "=?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public void updateChannelUserMapper(ChannelUserMapper channelUserMapper) {
        try {
            ContentValues contentValues = prepareChannelUserMapperValues(channelUserMapper);
            dbHelper.getWritableDatabase().update(CHANNEL_USER_X, contentValues, MobiComDatabaseHelper.CHANNEL_KEY + "=?  and " + MobiComDatabaseHelper.USERID + "=?", new String[]{String.valueOf(channelUserMapper.getKey()), String.valueOf(channelUserMapper.getUserKey())});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public boolean isChannelPresent(Integer channelKey) {
        try {
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            String sql = "SELECT COUNT(*) FROM channel WHERE channelKey = ?";
            SQLiteStatement statement = database.compileStatement(sql);
            statement.bindString(1,String.valueOf(channelKey));
            return statement.simpleQueryForLong() > 0;
        } finally {
            dbHelper.close();
        }
    }

    public void updateChannelLocalImageURI(Integer channelKey, String channelLocalURI) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MobiComDatabaseHelper.CHANNEL_IMAGE_LOCAL_URI, channelLocalURI);
            dbHelper.getWritableDatabase().update(CHANNEL, contentValues, MobiComDatabaseHelper.CHANNEL_KEY + "=?", new String[]{String.valueOf(channelKey)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public boolean isChannelUserPresent(Integer channelKey, String userId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        boolean present = false;
        try {
            String sql = "SELECT COUNT(*) FROM channel_User_X WHERE " + MobiComDatabaseHelper.CHANNEL_KEY + "= ? and " + MobiComDatabaseHelper.USERID + "= ?";
            SQLiteStatement statement = database.compileStatement(sql);
            statement.bindString(1,String.valueOf(channelKey));
            statement.bindString(2,String.valueOf(userId));
            present = statement.simpleQueryForLong() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
        return present;
    }

    public int removeMemberFromChannel(String clientGroupId, String userId) {
        Channel channel = getChannelByClientGroupId(clientGroupId);
        return removeMemberFromChannel(channel.getKey(), userId);
    }

    public int removeMemberFromChannel(Integer channelKey, String userId) {
        int deleteUser = 0;
        try {
            deleteUser = dbHelper.getWritableDatabase().delete(MobiComDatabaseHelper.CHANNEL_USER_X, channelKey_userID, new String[]{String.valueOf(channelKey), userId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
        return deleteUser;
    }

    public int leaveMemberFromChannel(String clientGroupId, String userId) {
        Channel channel = getChannelByClientGroupId(clientGroupId);
        return leaveMemberFromChannel(channel.getKey(), userId);
    }

    public int leaveMemberFromChannel(Integer channelKey, String userId) {
        int deletedRows = 0;
        try {
            deletedRows = dbHelper.getWritableDatabase().delete(MobiComDatabaseHelper.CHANNEL_USER_X, channelKey_userID, new String[]{String.valueOf(channelKey), userId});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deletedRows;
    }

    public int updateChannel(GroupInfoUpdate groupInfoUpdate) {
        if (groupInfoUpdate.getImageUrl() == null && groupInfoUpdate.getNewName() == null) {
            return 0;
        }

        int rowUpdated = 0;
        try {
            ContentValues values = new ContentValues();
            if (groupInfoUpdate != null) {
                if (!TextUtils.isEmpty(groupInfoUpdate.getClientGroupId())) {
                    Channel channel = getChannelByClientGroupId(groupInfoUpdate.getClientGroupId());
                    groupInfoUpdate.setGroupId(channel.getKey());
                }
                if (groupInfoUpdate.getNewName() != null) {
                    values.put(channelName, groupInfoUpdate.getNewName());
                }
                if (groupInfoUpdate.getImageUrl() != null) {
                    values.put(channelImageURL, groupInfoUpdate.getImageUrl());
                    values.putNull(channelImageLocalURI);
                }
            }
            rowUpdated = dbHelper.getWritableDatabase().update("channel", values, "channelKey=" + groupInfoUpdate.getGroupId(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowUpdated;
    }

    public int deleteChannel(Integer channelKey) {
        int deletedRows = 0;
        try {
            deletedRows = dbHelper.getWritableDatabase().delete(MobiComDatabaseHelper.CHANNEL, "channelKey=?", new String[]{String.valueOf(channelKey)});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deletedRows;
    }

    public int deleteChannelUserMappers(Integer channelKey) {
        int deletedRows = 0;
        try {
            deletedRows = dbHelper.getWritableDatabase().delete(MobiComDatabaseHelper.CHANNEL_USER_X, "channelKey=?", new String[]{String.valueOf(channelKey)});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deletedRows;
    }

    public Loader<Cursor> getSearchCursorForGroupsLoader(final String searchString) {

        return new CursorLoader(context, null, null, null, null, MobiComDatabaseHelper.CHANNEL_DISPLAY_NAME + " asc") {
            @Override
            public Cursor loadInBackground() {

                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor;
                List<String> selectionArgs = new ArrayList<>();
                String selection = MobiComDatabaseHelper.TYPE + " NOT IN ('" + Channel.GroupType.CONTACT_GROUP.getValue() + "')";
                if(!TextUtils.isEmpty(searchString)){
                    selection += " AND " + MobiComDatabaseHelper.CHANNEL_DISPLAY_NAME + " like ? ";
                    selectionArgs.add("%" + searchString.replaceAll("'", "''") + "%");
                }
                cursor = db.query(MobiComDatabaseHelper.CHANNEL, null, selection, selectionArgs.toArray(new String[0]), null, null, MobiComDatabaseHelper.CHANNEL_DISPLAY_NAME + " COLLATE NOCASE asc ");
                return cursor;

            }
        };
    }


    public String getGroupOfTwoReceiverId(Integer channelKey) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String structuredNameWhere = "";

            structuredNameWhere += "channelKey = ? AND userId NOT IN ('" + MobiComUserPreference.getInstance(context).getUserId().replaceAll("'", "''") + "')";
            cursor = db.query(CHANNEL_USER_X, null, structuredNameWhere, new String[]{String.valueOf(channelKey)}, null, null, null);

            List<ChannelUserMapper> channelUserMappers = getListOfUsers(cursor);
            if (channelUserMappers != null && channelUserMappers.size() > 0) {
                ChannelUserMapper channelUserMapper = channelUserMappers.get(0);
                if (channelUserMapper != null) {
                    return channelUserMapper.getUserKey();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
        return null;
    }


    public String[] getChannelMemberByName(String name, String type) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        List<String> userIds = new ArrayList<String>();
        Cursor cursor = database.query(channelUser_channelKey,new String[]{"cu.userId"},"c.channelName = ? AND c.type = ?",new String[]{name,type},null,null,null);
        try {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    userIds.add(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.USERID)));

                } while (cursor.moveToNext());
            }
            if (userIds.contains(MobiComUserPreference.getInstance(context).getUserId())) {
                userIds.remove(MobiComUserPreference.getInstance(context).getUserId());
            }
            if (userIds != null && userIds.size() > 0) {
                return userIds.toArray(new String[userIds.size()]);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
    }

    public void updateRoleInChannelUserMapper(Integer channelKey, String userId, Integer role) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MobiComDatabaseHelper.ROLE, role);
            dbHelper.getWritableDatabase().update(CHANNEL_USER_X, contentValues, MobiComDatabaseHelper.CHANNEL_KEY + AND + MobiComDatabaseHelper.USERID + "=?", new String[]{String.valueOf(channelKey), userId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public ChannelUserMapper getChannelUserByChannelKey(final Integer channelKey) {
        ChannelUserMapper channelUserMapper = null;
        Cursor cursor = null;
        try {
            String structuredNameWhere = MobiComDatabaseHelper.CHANNEL_KEY + AND + MobiComDatabaseHelper.USERID + "=" + MobiComUserPreference.getInstance(context).getUserId();

            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.query(CHANNEL_USER_X, null, structuredNameWhere, new String[]{String.valueOf(channelKey)}, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    channelUserMapper = getChannelUser(cursor);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
        return channelUserMapper;
    }

    public ChannelUserMapper getChannelUserByChannelKeyAndUserId(final Integer channelKey, final String userId) {
        ChannelUserMapper channelUserMapper = null;
        Cursor cursor = null;
        try {
            String structuredNameWhere = MobiComDatabaseHelper.CHANNEL_KEY + AND + MobiComDatabaseHelper.USERID + " =?";

            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.query(CHANNEL_USER_X, null, structuredNameWhere, new String[]{String.valueOf(channelKey), userId}, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    channelUserMapper = getChannelUser(cursor);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
        return channelUserMapper;
    }

    public List<String> getChildGroupIds(Integer parentGroupKey) {
        if (parentGroupKey == null || parentGroupKey == 0) {
            return new ArrayList<>();
        }
        Cursor cursor = null;
        try {
            List<String> childGroupIds = new ArrayList<>();

            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String structuredNameWhere = "";
            structuredNameWhere += "parentGroupKey = ?";
            cursor = db.query(CHANNEL, null, structuredNameWhere, new String[]{String.valueOf(parentGroupKey)}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    childGroupIds.add(String.valueOf(cursor.getInt(cursor.getColumnIndex(MobiComDatabaseHelper.CHANNEL_KEY))));

                } while (cursor.moveToNext());
            }
            return childGroupIds;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
        return null;
    }

    public void updateParentGroupKeyInUserMapper(Integer channelKey, Integer parentGroupKey) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MobiComDatabaseHelper.PARENT_GROUP_KEY, parentGroupKey);
            dbHelper.getWritableDatabase().update(CHANNEL_USER_X, contentValues, MobiComDatabaseHelper.CHANNEL_KEY + "=?", new String[]{String.valueOf(channelKey)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }


    public Integer getParentGroupKey(String parentClientGroupId) {
        if (TextUtils.isEmpty(parentClientGroupId)) {
            return null;
        }
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String structuredNameWhere = "";
            structuredNameWhere += "parentClientGroupId = ?";
            cursor = db.query(CHANNEL, null, structuredNameWhere, new String[]{String.valueOf(parentClientGroupId)}, null, null, null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex("parentGroupKey"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
        return null;
    }


}
