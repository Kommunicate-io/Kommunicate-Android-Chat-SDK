package io.kommunicate.devkit.api.conversation.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;
import android.text.TextUtils;

import io.kommunicate.devkit.SettingsSharedPreference;
import io.kommunicate.devkit.api.account.user.MobiComUserPreference;
import io.kommunicate.devkit.api.attachment.FileMeta;
import io.kommunicate.devkit.api.conversation.Message;
import io.kommunicate.devkit.broadcast.BroadcastService;
import io.kommunicate.devkit.channel.service.ChannelService;
import io.kommunicate.devkit.database.MobiComDatabaseHelper;
import io.kommunicate.commons.AppContextService;
import io.kommunicate.commons.commons.core.utils.DBUtils;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.json.GsonUtils;
import io.kommunicate.commons.people.channel.Channel;
import io.kommunicate.commons.people.contact.Contact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Manish
 * Date: 6/9/12
 * Time: 8:40 PM
 */
public class MessageDatabaseService {

    private static final String TAG = "MessageDatabaseService";
    private static final String size = "size";
    private static final String name = "name";
    public static List<Message> recentlyAddedMessage = new ArrayList<Message>();
    private Context context = null;
    private MobiComDatabaseHelper dbHelper;
    private boolean hideActionMessages = false;
    private boolean skipDeletedGroups;
    private static final String id = "id";
    private static final String keyString = "keyString";
    private static final String type = "type";
    private static final String source = "source";
    private static final String storeOnDevice = "storeOnDevice";
    private static final String contactNumbers = "contactNumbers";
    private static final String createdAt = "createdAt";
    private static final String blobKeyString = "blobKeyString";
    private static final String metaFileKeyString = "metaFileKeyString";
    private static final String thumbnailBlobKey = "thumbnailBlobKey";
    private static final String channelKey_AND= "channelKey = ? AND ";
    private static final String contactNumbers_AND = "contactNumbers = ? AND ";
    private static final String createdAt_AND = "createdAt >= ? AND ";
    private static final String createdAt_AND_No_null = "createdAt < ? AND ";
    private static final String conversationID_AND = "conversationId = ? AND ";
    private static final String deleted_AND = "deleted = ? AND ";
    private static final String reply_AND = "replyMessage != ? AND ";
    private static final String hidden_AND = "hidden = ? AND ";
    private static final String typeAnd = "type != ? AND type != ? AND ";
    private static final String status_03 = "status in (0,3) AND ";
    private static final String serverOps = "sentToServer = ? and canceled = ? and deleted = ?";
    private static final String channelKey_like = "channelKey = ? and metadata like ?";
    private static final String contactAnd_Message = "contactNumbers = ? AND message = ?";
    private static final String toNumbers = "toNumbers";
    private static final String applicationId = "applicationId";
    private static final String sentToServer = "sentToServer";
    private static final String thumbnailUrl = "thumbnailUrl";
    private static final String status_notin_5 = "status not in (5)";
    private static final String status_notin_4_5 = "status not in (4,5)";
    private static final String clientgroup_AND = "clientGroupId = ? AND ";
    private static final String messageContentType_10 = "messageContentType in (10) ";
    private static final String m_message = " and (m.message like ? ";
    private static final String c_channel = " or c.channelName like ?)";
    private static final String deleted_messageContent__replyMessage = "deleted = 0 AND messageContentType NOT IN (10,11,102,103) AND replyMessage NOT IN (2) AND type NOT IN (6,7) AND hidden = 0 AND message like ?";
    private static final String hidden_deleted_messageContent = "m.hidden = 0 AND m.deleted = 0 AND m.messageContentType not in (11,102) AND m.type not in (6, 7) AND ch.type = 10 AND ch.deletedAtTime is NULL AND";
    private static final String kmStatus_1_2 = " ch.kmStatus in (1, 2)";
    private static final String km_status = " ch.kmStatus = ?";
    private static final String m_createdAt = " AND m.createdAt < ?";
    private static final String contactNo_And_channelKey = "contactNumbers=? AND channelKey = 0";

    public MessageDatabaseService(Context context) {
        this.context = AppContextService.getContext(context);
        this.dbHelper = MobiComDatabaseHelper.getInstance(context);
        hideActionMessages = SettingsSharedPreference.getInstance(context).isActionMessagesHidden();
        skipDeletedGroups = SettingsSharedPreference.getInstance(context).isSkipDeletedGroups();
    }

    @SuppressLint("Range")
    public static Message getMessage(Cursor cursor) {
        Message message = new Message();
        message.setMessageId(cursor.getLong(cursor.getColumnIndex(id)));
        message.setKeyString(cursor.getString(cursor.getColumnIndex(keyString)));
        message.setType(cursor.getShort(cursor.getColumnIndex(type)));
        message.setSource(cursor.getShort(cursor.getColumnIndex(source)));
        Long storeOnDevice = cursor.getLong(cursor.getColumnIndex("storeOnDevice"));
        message.setStoreOnDevice(storeOnDevice != null && storeOnDevice.intValue() == 1);
        String contactNumbers = cursor.getString(cursor.getColumnIndex("contactNumbers"));
        message.setContactIds(contactNumbers);
        message.setCreatedAtTime(cursor.getLong(cursor.getColumnIndex(createdAt)));
        Long delivered = cursor.getLong(cursor.getColumnIndex("delivered"));
        message.setDelivered(delivered != null && delivered.intValue() == 1);

        Long canceled = cursor.getLong(cursor.getColumnIndex("canceled"));
        message.setCanceled(canceled != null && canceled.intValue() == 1);

        Long read = cursor.getLong(cursor.getColumnIndex("read"));
        message.setRead(read != null && read.intValue() == 1);

        message.setStatus(cursor.getShort(cursor.getColumnIndex(MobiComDatabaseHelper.STATUS)));
        message.setClientGroupId(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.CLIENT_GROUP_ID)));

        Long scheduledAt = cursor.getLong(cursor.getColumnIndex("scheduledAt"));
        message.setScheduledAt(scheduledAt == null || scheduledAt.intValue() == 0 ? null : scheduledAt);
        message.setMessage(cursor.getString(cursor.getColumnIndex("message")));
        Long sentToServer = cursor.getLong(cursor.getColumnIndex("sentToServer"));
        message.setSentToServer(sentToServer != null && sentToServer.intValue() == 1);
        message.setTo(cursor.getString(cursor.getColumnIndex("toNumbers")));
        int timeToLive = cursor.getInt(cursor.getColumnIndex("timeToLive"));
        message.setReplyMessage(cursor.getInt(cursor.getColumnIndex("replyMessage")));
        message.setTimeToLive(timeToLive != 0 ? timeToLive : null);
        String fileMetaKeyStrings = cursor.getString(cursor.getColumnIndex("fileMetaKeyStrings"));
        if (!TextUtils.isEmpty(fileMetaKeyStrings)) {
            message.setFileMetaKeyStrings(fileMetaKeyStrings);
        }
        String filePaths = cursor.getString(cursor.getColumnIndex("filePaths"));
        if (!TextUtils.isEmpty(filePaths)) {
            message.setFilePaths(Arrays.asList(filePaths.split(",")));
        }
        message.setHidden(cursor.getInt(cursor.getColumnIndex(MobiComDatabaseHelper.HIDDEN)) == 1);
        String metadata = cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.MESSAGE_METADATA));
        if (!TextUtils.isEmpty(metadata)) {
            message.setMetadata(((Map<String, String>) GsonUtils.getObjectFromJson(metadata, Map.class)));
        }
        message.setApplicationId(cursor.getString(cursor.getColumnIndex("applicationId")));
        message.setContentType(cursor.getShort(cursor.getColumnIndex(MobiComDatabaseHelper.MESSAGE_CONTENT_TYPE)));
        int conversationId = cursor.getInt(cursor.getColumnIndex(MobiComDatabaseHelper.CONVERSATION_ID));
        if (conversationId == 0) {
            message.setConversationId(null);
        } else {
            message.setConversationId(conversationId);
        }
        message.setTopicId(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.TOPIC_ID)));
        int channelKey = cursor.getInt(cursor.getColumnIndex(MobiComDatabaseHelper.CHANNEL_KEY));
        if (channelKey == 0) {
            message.setGroupId(null);
        } else {
            message.setGroupId(channelKey);
        }

        if (cursor.getString(cursor.getColumnIndex(blobKeyString)) == null) {
            //file is not present...  Don't set anything ...
        } else {
            FileMeta fileMeta = new FileMeta();
            fileMeta.setKeyString(cursor.getString(cursor.getColumnIndex(metaFileKeyString)));
            fileMeta.setBlobKeyString(cursor.getString(cursor.getColumnIndex(blobKeyString)));
            fileMeta.setThumbnailBlobKey(cursor.getString(cursor.getColumnIndex(thumbnailBlobKey)));
            fileMeta.setThumbnailUrl(cursor.getString(cursor.getColumnIndex("thumbnailUrl")));
            fileMeta.setSize(cursor.getInt(cursor.getColumnIndex("size")));
            fileMeta.setName(cursor.getString(cursor.getColumnIndex("name")));
            fileMeta.setContentType(cursor.getString(cursor.getColumnIndex("contentType")));
            fileMeta.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            message.setFileMetas(fileMeta);
        }
        return message;
    }

    public static List<Message> getMessageList(Cursor cursor) {
        List<Message> messageList = new ArrayList<Message>();
        try {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    Message message = getMessage(cursor);
                    if (Message.ContentType.CHANNEL_CUSTOM_MESSAGE.getValue().equals(message.getContentType())) {
                        if (!Message.GroupMessageMetaData.TRUE.getValue().equals(message.getMetaDataValueForKey(Message.GroupMessageMetaData.HIDE_KEY.getValue()))) {
                            messageList.add(message);
                        }
                    } else {
                        messageList.add(message);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return messageList;
    }

    public static List<Message> getLatestMessageList(Cursor cursor) {
        List<Message> messageList = new ArrayList<Message>();
        try {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    Message message = getMessage(cursor);
                    if (message != null) {
                        if (!Message.MetaDataType.ARCHIVE.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || !message.isHidden()) {
                            messageList.add(message);
                        }
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return messageList;
    }

    public static List<Message> getLatestMessageListForNotification(Cursor cursor) {
        List<Message> messageList = new ArrayList<Message>();
        try {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    Message message = getMessage(cursor);
                    if (message != null) {
                        if (!Message.GroupMessageMetaData.FALSE.getValue().equals(message.getMetaDataValueForKey(Message.GroupMessageMetaData.KEY.getValue()))) {
                            messageList.add(message);
                        }
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return messageList;
    }

    public List<Message> getMessages(Long startTime, Long endTime, Contact contact, Channel channel, Integer conversationId) {
        String structuredNameWhere = "";
        List<String> structuredNameParamsList = new ArrayList<String>();

        if (channel != null && channel.getKey() != null) {
            structuredNameWhere += channelKey_AND;
            structuredNameParamsList.add(String.valueOf(channel.getKey()));
        } else {
            structuredNameWhere += channelKey_AND;
            structuredNameParamsList.add("0");
        }
        if (contact != null && !TextUtils.isEmpty(contact.getContactIds())) {
            structuredNameWhere += contactNumbers_AND;
            structuredNameParamsList.add(contact.getContactIds());
        }
        if (startTime != null) {
            structuredNameWhere += createdAt_AND;
            structuredNameParamsList.add(String.valueOf(startTime));
        }
        if (endTime != null) {
            structuredNameWhere += createdAt_AND_No_null;
            structuredNameParamsList.add(String.valueOf(endTime));
        }
        if (BroadcastService.isContextBasedChatEnabled() && conversationId != null && conversationId != 0) {
            structuredNameWhere += conversationID_AND;
            structuredNameParamsList.add(String.valueOf(conversationId));
        }
        structuredNameWhere += "messageContentType not in ( ?,? ) AND ";
        structuredNameParamsList.add(String.valueOf(Message.ContentType.HIDDEN.getValue()));
        structuredNameParamsList.add(String.valueOf(Message.ContentType.VIDEO_CALL_NOTIFICATION_MSG.getValue()));
        structuredNameWhere += deleted_AND;
        structuredNameParamsList.add("0");
        structuredNameWhere += hidden_AND;
        structuredNameParamsList.add("0");
        structuredNameWhere += reply_AND;
        structuredNameParamsList.add(String.valueOf(Message.ReplyMessage.HIDE_MESSAGE.getValue()));

        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        if (!userPreferences.isDisplayCallRecordEnable()) {
            structuredNameWhere += typeAnd;
            structuredNameParamsList.add(String.valueOf(Message.MessageType.CALL_INCOMING.getValue()));
            structuredNameParamsList.add(String.valueOf(Message.MessageType.CALL_OUTGOING.getValue()));
        }

        if (!TextUtils.isEmpty(structuredNameWhere)) {
            structuredNameWhere = structuredNameWhere.substring(0, structuredNameWhere.length() - 5);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            Cursor cursor = db.query("sms", null, structuredNameWhere, structuredNameParamsList.toArray(new String[structuredNameParamsList.size()]), null, null, "createdAt asc");
            return MessageDatabaseService.getMessageList(cursor);
        } finally {
            dbHelper.close();
        }
    }

    public List<Message> getUnreadMessages() {
        String structuredNameWhere = "";
        List<String> structuredNameParamsList = new ArrayList<String>();
        structuredNameWhere += "messageContentType not in (11) AND ";
        structuredNameWhere += status_03;
        structuredNameWhere += "type = ? ";
        structuredNameParamsList.add(String.valueOf(Message.MessageType.MT_INBOX.getValue()));
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("sms", null, structuredNameWhere, structuredNameParamsList.toArray(new String[structuredNameParamsList.size()]), null, null, "createdAt desc limit 10");
        return MessageDatabaseService.getLatestMessageListForNotification(cursor);
    }

    public List<Message> getPendingMessages() {
        String structuredNameWhere = "";
        List<String> structuredNameParamsList = new ArrayList<String>();
        structuredNameWhere += serverOps;
        structuredNameParamsList.add("0");
        structuredNameParamsList.add("0");
        structuredNameParamsList.add("0");
        Cursor cursor = dbHelper.getReadableDatabase().query("sms", null, structuredNameWhere, structuredNameParamsList.toArray(new String[structuredNameParamsList.size()]), null, null, "createdAt asc");
        List<Message> messageList = getMessageList(cursor);
        dbHelper.close();
        return messageList;
    }

    public Message getLatestStatusMessage(Integer channelKey) {
        if (channelKey == null) {
            return null;
        }
        try {
            Cursor cursor = dbHelper.getReadableDatabase().query("sms", null, channelKey_like, new String[]{String.valueOf(channelKey), "%" + Message.CONVERSATION_STATUS + "%"}, null, null, "createdAt DESC", "1");
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                return getMessage(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
        return null;
    }

    public List<Message> getPendingDeleteMessages() {
        String structuredNameWhere = "";
        List<String> structuredNameParamsList = new ArrayList<String>();
        structuredNameWhere += "sentToServer = ? and deleted = ?";
        structuredNameParamsList.add("1");
        structuredNameParamsList.add("1");
        Cursor cursor = dbHelper.getReadableDatabase().query("sms", null, structuredNameWhere, structuredNameParamsList.toArray(new String[structuredNameParamsList.size()]), null, null, "createdAt asc");
        List<Message> messageList = getMessageList(cursor);
        return messageList;
    }

    public long getMinCreatedAtFromMessageTable() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        final Cursor cursor = db.query("sms", new String[]{"min(createdAt)"}, null, null, null, null, null);
        try {
            long createdAt = 0;
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                createdAt = cursor.getLong(0);
            }
            return createdAt;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
    }

    public Message getMessage(String contactNumber, String message) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String structuredNameWhere = "";
        List<String> structuredNameParamsList = new ArrayList<String>();

        structuredNameWhere += contactAnd_Message;
        structuredNameParamsList.add(contactNumber);
        structuredNameParamsList.add(message);

        Cursor cursor = db.query("sms", null, structuredNameWhere, structuredNameParamsList.toArray(new String[structuredNameParamsList.size()]), null, null, null);

        try {
            Message message1 = null;
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                message1 = getMessage(cursor);
            }
            return message1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
    }

    public boolean isMessagePresent(String key) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM sms WHERE keyString = ?";
        SQLiteStatement sqLiteStatement = database.compileStatement(sql);
        sqLiteStatement.bindString(1, key);
        try {
            return sqLiteStatement.simpleQueryForLong() > 0;
        } finally {
            dbHelper.close();
        }
    }

    public Message getMessage(String keyString) {
        if (TextUtils.isEmpty(keyString)) {
            return null;
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String structuredNameWhere = "";
        List<String> structuredNameParamsList = new ArrayList<String>();

        structuredNameWhere += "keyString = ?";
        structuredNameParamsList.add(keyString);

        Cursor cursor = db.query("sms", null, structuredNameWhere, structuredNameParamsList.toArray(new String[structuredNameParamsList.size()]), null, null, null);

        try {
            Message message = null;
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                message = getMessage(cursor);
            }
            return message;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
    }

    public List<Message> getScheduledMessages(Long time) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (!DBUtils.isTableExists(db, MobiComDatabaseHelper.SCHEDULE_SMS_TABLE_NAME)) {
            dbHelper.close();
            return new ArrayList<Message>();
        }

        List<Message> messages = new ArrayList<Message>();
        Cursor cursor = null;
        try {
            if (time != null) {
                cursor = db.query(MobiComDatabaseHelper.SCHEDULE_SMS_TABLE_NAME, null, MobiComDatabaseHelper.TIMESTAMP + " <= ?", new String[]{
                        time + ""}, null, null, null);
            } else {
                cursor = db.query(MobiComDatabaseHelper.SCHEDULE_SMS_TABLE_NAME, null, null, null, null, null, null);
            }

            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    String createdTime = cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.TIMESTAMP));
                    //SMS Creation From DB......
                    Message message = new Message();
                    message.setCreatedAtTime(Long.valueOf(createdTime));

                    message.setScheduledAt(cursor.getLong(cursor.getColumnIndex(MobiComDatabaseHelper.TIMESTAMP)));

                    message.setMessage(cursor
                            .getString(cursor.getColumnIndex(MobiComDatabaseHelper.SMS)));
                    message.setType(cursor
                            .getShort(cursor.getColumnIndex(MobiComDatabaseHelper.SMS_TYPE)));
                    message.setSource(cursor.getShort(cursor.getColumnIndex("source")));
                    message.setContactIds(cursor
                            .getString(cursor.getColumnIndex(MobiComDatabaseHelper.CONTACTID)));
                    message.setTo(cursor
                            .getString(cursor.getColumnIndex(MobiComDatabaseHelper.TO_FIELD)));
                    message.setKeyString(cursor
                            .getString(cursor.getColumnIndex(MobiComDatabaseHelper.SMS_KEY_STRING)));
                    message.setStoreOnDevice("1".equals(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.STORE_ON_DEVICE_COLUMN))));

                    if (cursor.getColumnIndex(MobiComDatabaseHelper.TIME_TO_LIVE) != -1) {
                        int timeToLive = cursor.getInt(cursor.getColumnIndex(MobiComDatabaseHelper.TIME_TO_LIVE));
                        message.setTimeToLive(timeToLive == 0 ? null : timeToLive);
                    }

                    messages.add(message);
                } while (cursor.moveToNext());
            }
            return messages;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
    }

    public void deleteScheduledMessages(long time) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(MobiComDatabaseHelper.SCHEDULE_SMS_TABLE_NAME, MobiComDatabaseHelper.TIMESTAMP + " <= ? ", new String[]{time + ""});
        dbHelper.close();
    }

    public boolean deleteScheduledMessage(String messageKeyString) {
        SQLiteDatabase db = dbHelper.getInstance(context).getWritableDatabase();
        boolean deleted = db.delete(MobiComDatabaseHelper.SCHEDULE_SMS_TABLE_NAME, MobiComDatabaseHelper.SMS_KEY_STRING + "='" + messageKeyString + "'", null) > 0;
        dbHelper.close();
        return deleted;
    }

    public boolean isMessageTableEmpty() {
        dbHelper = MobiComDatabaseHelper.getInstance(context);
        boolean empty = DBUtils.isTableEmpty(dbHelper.getReadableDatabase(), "sms");
        dbHelper.close();
        return empty;
    }

    public void updateMessageFileMetas(long messageId, final Message message) {
        ContentValues values = new ContentValues();
        values.put("keyString", message.getKeyString());
        if (message.getFileMetaKeyStrings() != null) {
            values.put("fileMetaKeyStrings", message.getFileMetaKeyStrings());
        }
        if (message.getFileMetas() != null) {
            FileMeta fileMeta = message.getFileMetas();
            if (fileMeta != null) {
                values.put("thumbnailUrl", fileMeta.getThumbnailUrl());
                values.put(size, fileMeta.getSize());
                values.put(name, fileMeta.getName());
                values.put("contentType", fileMeta.getContentType());
                values.put("metaFileKeyString", fileMeta.getKeyString());
                values.put("blobKeyString", fileMeta.getBlobKeyString());
                values.put("thumbnailBlobKey", fileMeta.getThumbnailBlobKey());
                values.put("url", fileMeta.getUrl());
            }
        }
        dbHelper.getWritableDatabase().update("sms", values, "id=" + messageId, null);
        dbHelper.close();
    }

    public long createMessage(final Message message) {
        long id = -1;
        if (message.getMessageId() != null) {
            return message.getMessageId();
        }
        if (isMessagePresent(message.getKeyString())) {
            id = getMessage(message.getKeyString()).getMessageId();
        } else {
            id = createSingleMessage(message);
        }
        message.setMessageId(id);
        if (message.isSentToMany()) {
            String[] toList = message.getTo().trim().replace("undefined,", "").split(",");
            for (String tofield : toList) {
                Message singleMessage = new Message(message);
                singleMessage.setKeyString(message.getKeyString());
                //  singleMessage.setBroadcastGroupId(null);
                singleMessage.setTo(tofield);
                singleMessage.processContactIds(context);
                singleMessage.setMessageId(createSingleMessage(singleMessage));
            }
        }
        return id;
    }

    public long createSingleMessage(final Message message) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        SettingsSharedPreference settingsSharedPreference = SettingsSharedPreference.getInstance(context);
        long id = -1;
        boolean duplicateCheck = true;
        long minCreatedAt = settingsSharedPreference.getMinCreatedAtTime();
        long maxCreatedAt = settingsSharedPreference.getMaxCreatedAtTime();

        if (message.getCreatedAtTime() < minCreatedAt) {
            duplicateCheck = false;
            settingsSharedPreference.setMinCreatedAtTime(message.getCreatedAtTime());
        }
        if (message.getCreatedAtTime() > maxCreatedAt) {
            duplicateCheck = false;
            settingsSharedPreference.setMaxCreatedAtTime(message.getCreatedAtTime());
        }

        if (duplicateCheck) {
            SQLiteStatement statement = null;
            try {
                String queryClause = "";

                if (message.getGroupId() != null) {
                    queryClause = "channelKey ='" + String.valueOf(message.getGroupId()) + "'";
                } else {
                    queryClause = "contactNumbers ='" + message.getContactIds() + "'";
                }

                if (message.isSentToServer() && !TextUtils.isEmpty(message.getKeyString())) {
                    statement = database.compileStatement("SELECT COUNT(*) FROM sms WHERE keyString = ? and " + queryClause);
                    statement.bindString(1, message.getKeyString());
                } else {
                    statement = database.compileStatement("SELECT COUNT(*) FROM sms WHERE sentToServer=0 and " + queryClause + " and message = ? and createdAt = ?");
                    statement.bindString(1, message.getMessage());
                    statement.bindLong(2, message.getCreatedAtTime());
                }
                if (statement.simpleQueryForLong() > 0) {
                    return -1;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                dbHelper.close();
            }
        }

        try {
            ContentValues values = new ContentValues();
            values.put(toNumbers, message.getTo());
            values.put("message", message.getMessage());
            values.put(createdAt, message.getCreatedAtTime());
            values.put(storeOnDevice, message.isStoreOnDevice());
            values.put("delivered", message.getDelivered());
            values.put("scheduledAt", message.getScheduledAt());
            values.put("type", message.getType());
            values.put(contactNumbers, message.getContactIds());
            values.put(sentToServer, message.isSentToServer());
            values.put("keyString", message.getKeyString());
            values.put("source", message.getSource());
            values.put("timeToLive", message.getTimeToLive());
            values.put("canceled", message.isCanceled());
            values.put("read", message.isRead() ? 1 : 0);
            values.put(applicationId, message.getApplicationId());
            values.put(MobiComDatabaseHelper.MESSAGE_CONTENT_TYPE, message.getContentType());
            values.put(MobiComDatabaseHelper.STATUS, message.getStatus());
            values.put(MobiComDatabaseHelper.CONVERSATION_ID, message.getConversationId());
            values.put(MobiComDatabaseHelper.TOPIC_ID, message.getTopicId());
            values.put(MobiComDatabaseHelper.HIDDEN, message.hasHideKey());
            boolean hidden = ((hideActionMessages && message.isActionMessage()) || (message.isActionMessage() && TextUtils.isEmpty(message.getMessage()))) || message.hasHideKey();
            values.put(MobiComDatabaseHelper.HIDDEN, hidden);
            if (message.getGroupId() != null) {
                values.put(MobiComDatabaseHelper.CHANNEL_KEY, message.getGroupId());
            }
            if (!TextUtils.isEmpty(message.getClientGroupId())) {
                values.put(MobiComDatabaseHelper.CLIENT_GROUP_ID, message.getClientGroupId());
            }
            if (message.getFileMetaKeyStrings() != null) {
                values.put("fileMetaKeyStrings", message.getFileMetaKeyStrings());
            }
            if (message.getFilePaths() != null && !message.getFilePaths().isEmpty()) {
                values.put("filePaths", TextUtils.join(",", message.getFilePaths()));
            }
            if (message.getMetadata() != null && !message.getMetadata().isEmpty()) {
                values.put(MobiComDatabaseHelper.MESSAGE_METADATA, GsonUtils.getJsonFromObject(message.getMetadata(), Map.class));
            }
            values.put(MobiComDatabaseHelper.REPLY_MESSAGE, message.isReplyMessage());
            //TODO:Right now we are supporting single image attachment...making entry in same table
            if (message.getFileMetas() != null) {
                FileMeta fileMeta = message.getFileMetas();
                if (fileMeta != null) {
                    values.put(thumbnailUrl, fileMeta.getThumbnailUrl());
                    values.put(size, fileMeta.getSize());
                    values.put(name, fileMeta.getName());
                    values.put("contentType", fileMeta.getContentType());
                    values.put(metaFileKeyString, fileMeta.getKeyString());
                    values.put(blobKeyString, fileMeta.getBlobKeyString());
                    values.put(thumbnailBlobKey, fileMeta.getThumbnailBlobKey());
                    values.put("url", fileMeta.getUrl());
                }
            }
            id = database.insertOrThrow("sms", null, values);
        } catch (Throwable ex) {
            ex.printStackTrace();
            Utils.printLog(context, TAG, " Ignore Duplicate entry in sms table, sms: " + message);
        } finally {
            dbHelper.close();
        }

        return id;
    }

    public void updateSmsType(String smsKeyString, Message.MessageType messageType) {
        ContentValues values = new ContentValues();
        values.put("type", messageType.getValue());
        dbHelper.getWritableDatabase().update("sms", values, "keyString='" + smsKeyString + "'", null);
        dbHelper.close();
    }

    public int updateMessageDeliveryReportForContact(String contactId, boolean markRead) {
        try {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            String whereClause = "contactNumbers= " + contactId + " and ";
            values.put("delivered", "1");
            if (markRead) {
                whereClause = whereClause + status_notin_5;
                values.put("status", String.valueOf(Message.Status.DELIVERED_AND_READ.getValue()));
            } else {
                whereClause = whereClause + status_notin_4_5;
                values.put("status", String.valueOf(Message.Status.DELIVERED.getValue()));
            }
            whereClause = whereClause + " and type=5 ";
            int rows = database.update("sms", values, whereClause, null);
            dbHelper.close();
            return rows;
        } catch (Throwable t) {

        }
        return 0;
    }

    public void updateMessageDeliveryReportForContact(String messageKeyString, String contactNumber, boolean markRead) {
        try {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            if (markRead) {
                values.put("status", String.valueOf(Message.Status.DELIVERED_AND_READ.getValue()));
            } else {
                values.put("status", String.valueOf(Message.Status.DELIVERED.getValue()));
            }
            values.put("delivered", "1");
            if (TextUtils.isEmpty(contactNumber)) {
                database.update("sms", values, "keyString='" + messageKeyString + "' and type = 5", null);
            } else {
                database.update("sms", values, "keyString='" + messageKeyString + "' and contactNumbers='" + contactNumber + "' and type = 5", null);
            }
            dbHelper.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void updateMessageSyncStatus(Message message, String keyString) {
        try {
            ContentValues values = new ContentValues();
            values.put("keyString", keyString);
            values.put(sentToServer, "1");
            values.put(createdAt, message.getSentMessageTimeAtServer());
            dbHelper.getWritableDatabase().update("sms", values, "id=" + message.getMessageId(), null);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public void updateDeleteSyncStatus(Message message, String deleteStatus) {
        try {
            ContentValues values = new ContentValues();
            values.put("deleted", deleteStatus);
            dbHelper.getWritableDatabase().update("sms", values, "id=" + message.getMessageId(), null);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public void updateInternalFilePath(String keyString, String filePath) {
        ContentValues values = new ContentValues();
        values.put("filePaths", filePath);
        dbHelper.getWritableDatabase().update("sms", values, "keyString='" + keyString + "'", null);
        dbHelper.close();

    }

    public void updateMessage(Long id, Long createdAt, String KeyString, boolean isSentToServer) {
        ContentValues values = new ContentValues();
        values.put("createdAt", createdAt);
        values.put(keyString, KeyString);
        values.put(sentToServer, isSentToServer);
        dbHelper.getWritableDatabase().update("sms", values, "id=" + id, null);
        dbHelper.close();
    }

    public void updateCanceledFlag(long smsId, int value) {
        ContentValues values = new ContentValues();
        values.put("canceled", value);
        dbHelper.getWritableDatabase().update("sms", values, "id=" + smsId, null);
        dbHelper.close();
    }

    public void updateMessageReadFlag(long smsId, boolean read) {
        ContentValues values = new ContentValues();
        values.put("read", read ? 1 : 0);
        values.put("status", 1);
        dbHelper.getWritableDatabase().update("sms", values, "id=" + smsId, null);
        dbHelper.close();
    }

    public int getUnreadMessageCountForContact(String userId) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.query("contact", new String[]{"unreadCount"}, "userId = ?", new String[]{userId}, null, null, null);
            cursor.moveToFirst();
            int unreadMessageCount = 0;
            if (cursor.getCount() > 0) {
                unreadMessageCount = cursor.getInt(0);
            }
            return unreadMessageCount;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
        return 0;
    }

    public int getUnreadMessageCountForChannel(Integer channelKey) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.query("channel", new String[]{"unreadCount"}, "channelKey = ?", new String[]{String.valueOf(channelKey)}, null, null, null);
            cursor.moveToFirst();
            int unreadMessage = 0;
            if (cursor.getCount() > 0) {
                unreadMessage = cursor.getInt(0);
            }
            return unreadMessage;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
        return 0;
    }

    public int getUnreadConversationCount() {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.query("sms", new String[]{"COUNT(DISTINCT contactNumbers)"}, "read = ?", new String[]{"0"}, null, null, null);
            cursor.moveToFirst();
            int conversationCount = 0;
            if (cursor.getCount() > 0) {
                conversationCount = cursor.getInt(0);
            }
            return conversationCount;
        } catch (Exception ex) {
            Utils.printLog(context, TAG, "Exception while fetching unread conversation count");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
        return 0;
    }

    public int getUnreadMessageCount() {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.query("sms", new String[]{"COUNT(1)"}, "read = ?", new String[]{"0"}, null, null, null);
            cursor.moveToFirst();
            int unreadMessageCount = 0;
            if (cursor.getCount() > 0) {
                unreadMessageCount = cursor.getInt(0);
            }
            return unreadMessageCount;
        } catch (Exception ex) {
            Utils.printLog(context, TAG, "Exception while fetching unread message count");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
        return 0;
    }

    public List<Message> getLatestMessage(String contactNumbers) {
        List<Message> messages = new ArrayList<Message>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("sms", null, "contactNumbers = ? ", new String[]{contactNumbers}, null, null, "createdAt desc", "1");
        if (cursor.moveToFirst()) {
            messages = MessageDatabaseService.getMessageList(cursor);
        }
        dbHelper.close();
        return messages;
    }


    public List<Message> getLatestMessageByClientGroupId(String clientGroupId) {
        return getLatestMessageForChannel(null, clientGroupId);
    }

    public List<Message> getLatestMessageByChannelKey(Integer channelKey) {
        return getLatestMessageForChannel(channelKey, null);
    }

    private List<Message> getLatestMessageForChannel(Integer channelKey, String clientGroupId) {
        String clauseString = null;

        if (channelKey != null && channelKey != 0) {
            clauseString = " channelKey = " + "'" + channelKey + "'";
        } else if (!TextUtils.isEmpty(clientGroupId)) {
            clauseString = " clientGroupId = " + "'" + clientGroupId + "'";
        }
        List<Message> messages = new ArrayList<Message>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("sms", null, clauseString, null, null, null, "createdAt desc", "1");
        try {
            if (cursor.moveToFirst()) {
                messages = MessageDatabaseService.getMessageList(cursor);
            }
            return messages;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
    }


    public boolean isMessagePresent(String key, Integer replyMessageType) {
        Cursor cursor = null;
        boolean present = false;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        try {
            cursor = database.query("sms", new String[]{"COUNT(*)"}, "keyString = ? AND replyMessage = ?", new String[]{key, String.valueOf(replyMessageType)}, null, null, null);
            cursor.moveToFirst();
            present = cursor.getInt(0) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
        return present;
    }


    public List<Message> getChannelCustomMessagesByClientGroupId(String clientGroupId) {
        return getChannelCustomMessageList(null, clientGroupId);
    }

    public List<Message> getChannelCustomMessagesByChannelKey(Integer channelKey) {
        return getChannelCustomMessageList(channelKey, null);
    }

    private List<Message> getChannelCustomMessageList(Integer channelKey, String clientGroupId) {
        String structuredNameWhere = "";
        List<String> structuredNameParamsList = new ArrayList<String>();
        if (channelKey != null && channelKey != 0) {
            structuredNameWhere = channelKey_AND;
            structuredNameParamsList.add(String.valueOf(channelKey));
        } else if (!TextUtils.isEmpty(clientGroupId)) {
            structuredNameWhere = clientgroup_AND;
            structuredNameParamsList.add(clientGroupId);
        }

        structuredNameWhere += messageContentType_10;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("sms", null, structuredNameWhere, structuredNameParamsList.toArray(new String[structuredNameParamsList.size()]), null, null, "createdAt desc");
        return getMessageList(cursor);
    }

    public int updateReadStatus(String contactNumbers) {
        ContentValues values = new ContentValues();
        values.put("read", 1);
        int read = dbHelper.getWritableDatabase().update("sms", values, " contactNumbers = " + "'" + contactNumbers + "'" + " and read = 0", null);
        dbHelper.close();
        return read;
    }

    public int updateReadStatusForKeyString(String keyString) {
        ContentValues values = new ContentValues();
        values.put("read", 1);
        values.put("status", 1);
        int read = dbHelper.getWritableDatabase().update("sms", values, " keyString = '" + keyString + "'", null);
        dbHelper.close();
        return read;
    }

    public int updateReadStatusForContact(String userId) {
        ContentValues values = new ContentValues();
        values.put(MobiComDatabaseHelper.UNREAD_COUNT, 0);
        int read = dbHelper.getWritableDatabase().update("contact", values, "userId = '" + userId + "'", null);
        dbHelper.close();
        return read;
    }

    public int updateReadStatusForChannel(String channelKey) {
        ContentValues values = new ContentValues();
        values.put(MobiComDatabaseHelper.UNREAD_COUNT, 0);
        int read = dbHelper.getWritableDatabase().update("channel", values, "channelKey = " + "'" + channelKey + "'", null);
        dbHelper.close();
        return read;
    }

    private List<Message> getLatestGroupMessages(Long createdAt, String searchText, Integer parentGroupKey) {
        if (parentGroupKey != null && parentGroupKey != 0) {
            List<String> channelKeysArray = ChannelService.getInstance(context).getChildGroupKeys(parentGroupKey);
            if (channelKeysArray == null || channelKeysArray.isEmpty()) {
                return new ArrayList<>();
            }
            channelKeysArray.add(String.valueOf(parentGroupKey));
            String placeHolderString = Utils.makePlaceHolders(channelKeysArray.size());
            String createdAtClause = "";
            String searchCaluse = "";

            if (!TextUtils.isEmpty(searchText)) {
                searchCaluse += m_message + c_channel;
                channelKeysArray.add("%" + searchText.replaceAll("'", "''") + "%");
                channelKeysArray.add("%" + searchText.replaceAll("'", "''") + "%");
            }

            if (createdAt != null && createdAt > 0) {
                createdAtClause += " and  m.createdAt < ?";
                channelKeysArray.add(String.valueOf(createdAt));
            }

            createdAtClause += " and m.deleted = 0 ";

            String messageTypeClause = "";
            MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
            if (!userPreferences.isDisplayCallRecordEnable()) {
                messageTypeClause = " and m.type != " + Message.MessageType.CALL_INCOMING.getValue() + " and m.type != " + Message.MessageType.CALL_OUTGOING.getValue();
            }

            String hiddenType = " and m.messageContentType != " + Message.ContentType.HIDDEN.getValue() + " and m.hidden = 0 and  m.replyMessage != " + Message.ReplyMessage.HIDE_MESSAGE.getValue();

            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String str = "select m1.* from sms m1, (SELECT  " +
                    "        m.channelKey as channelKey1, MAX(createdAt) as createdAt1" +
                    "    FROM" +
                    "        sms m join channel c on m.channelKey IN (" + placeHolderString + ")" +
                    "    WHERE 1=1 "
                    + searchCaluse
                    + messageTypeClause
                    + hiddenType
                    + createdAtClause
                    + " GROUP BY m.channelKey) m2" + " Where  m1.createdAt = m2.createdAt1 " +
                    "        AND m1.channelKey = m2.channelKey1 "
                    + " order by m1.createdAt desc";


            final Cursor cursor = db.query(str, channelKeysArray.toArray(new String[0]));
            try {
                return getMessageList(cursor);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                dbHelper.close();
            }
        }
        return new ArrayList<>();
    }


    public List<Message> getMessages(Long createdAt) {
        return getMessages(createdAt, null, null);
    }

    List<Message> getMessages(Long createdAt, String searchText) {
        return getMessages(createdAt, searchText, null);
    }


    public List<Message> getMessages(Long createdAt, String searchText, Integer parentGroupKey) {

        if (parentGroupKey != null && parentGroupKey != 0) {
            return getLatestGroupMessages(createdAt, searchText, parentGroupKey);
        } else {
            Cursor cursor = null;
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            if (!TextUtils.isEmpty(searchText)) {
                cursor = db.query("sms", null, deleted_messageContent__replyMessage, new String[]{"%" + searchText.replaceAll("'", "''") + "%"}, null, null, "createdAt DESC");
            } else {
                String messageTypeClause = "";
                String messageTypeJoinClause = "";
                String categoryClause = " left join channel ch on ch.channelKey = m1.channelKey ";

                MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
                String categoryName = userPreferences.getCategoryName();

                if (!userPreferences.isDisplayCallRecordEnable()) {
                    messageTypeClause = " and m1.type != " + Message.MessageType.CALL_INCOMING.getValue() + " and m1.type != " + Message.MessageType.CALL_OUTGOING.getValue();
                    messageTypeJoinClause = " and m1.type = m2.type";
                }

                String hiddenType = " and m1.messageContentType not in (" + Message.ContentType.HIDDEN.getValue()
                        + "," + Message.ContentType.VIDEO_CALL_NOTIFICATION_MSG.getValue() + ") AND m1.hidden = 0 AND m1.replyMessage not in (" + Message.ReplyMessage.HIDE_MESSAGE.getValue() + ")";

                String rowQuery = "select m1.* from sms m1 left outer join sms m2 on (m1.createdAt < m2.createdAt"
                        + " and m1.channelKey = m2.channelKey and m1.contactNumbers = m2.contactNumbers and m1.deleted = m2.deleted and  m1.messageContentType = m2.messageContentType and m1.hidden = m2.hidden " + messageTypeJoinClause + " ) ";

                if (!TextUtils.isEmpty(categoryName) || skipDeletedGroups) {
                    rowQuery = rowQuery + categoryClause;
                }

                rowQuery = rowQuery + "where m2.createdAt is null ";
                List<String> selectionArgs = new ArrayList<>();

                if (!TextUtils.isEmpty(categoryName)) {
                    rowQuery = rowQuery + "and ch.AL_CATEGORY = ?";
                    selectionArgs.add(categoryName);
                }

                if (skipDeletedGroups) {
                    rowQuery = rowQuery + " and ch.deletedAtTime is null";
                }

                String createdAtClause = "";
                if (createdAt != null && createdAt > 0) {
                    createdAtClause = " and m1.createdAt < ? ";
                    selectionArgs.add(String.valueOf(createdAt));
                }
                createdAtClause += " and m1.deleted = 0 ";

                String searchCaluse = "";
                if (!TextUtils.isEmpty(searchText)) {
                    searchCaluse += " and m1.message like ? ";
                    selectionArgs.add("%" + searchText.replaceAll("'", "''") + "%");
                }

                rowQuery = rowQuery + createdAtClause + searchCaluse + hiddenType + messageTypeClause + " order by m1.createdAt desc";
                cursor = db.query(rowQuery, selectionArgs.toArray(new String[0]));
            }

            List<Message> messageList = getLatestMessageList(cursor);
            dbHelper.close();
            return messageList;
        }
    }

    public List<Message> getAlConversationList(int status, Long lastFetchTime) {
        Cursor cursor = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String statusQuery = status == 2 ? "ch.kmStatus in (1, 2)" : "ch.kmStatus = ? ";

        try {
            if (status == 1) {
                List<String> selectionArgs = new ArrayList<>();
                String rowQuery = "SELECT * FROM (" +
                        "select max(createdAt) as maxCreatedAt , m.* from sms m inner join channel ch on m.channelKey = ch.channelKey " +
                        "where m.hidden = 0 " +
                        "AND m.deleted = 0 " +
                        "AND m.messageContentType not in (11,102) " +
                        "AND m.type not in (6, 7) " +
                        "AND ch.deletedAtTime is NULL " +
                        "AND " + statusQuery + " group by m.channelKey " +
                        "UNION ALL " +
                        "select max(createdAt) as maxCreatedAt , m.* from sms m " +
                        "where m.hidden = 0 " +
                        "AND m.deleted = 0 " +
                        "AND m.messageContentType not in (11,102) " +
                        "AND m.type not in (6, 7) AND m.channelKey = 0 " +
                        "group by m.contactNumbers " +
                        ") temp " +
                        (lastFetchTime != null && lastFetchTime > 0 ? " where temp.maxCreatedAt < " + lastFetchTime : "") +
                        " ORDER BY temp.maxCreatedAt DESC";
                selectionArgs.add(String.valueOf(status));
                if (lastFetchTime != null && lastFetchTime > 0) {
                    selectionArgs.add(String.valueOf(lastFetchTime));
                }
                cursor = db.query(rowQuery, selectionArgs.toArray(new String[0]));
            } else {

                List<String> selectionArgs = new ArrayList<>();
                String selection = hidden_deleted_messageContent;
                if (status == 2) {
                    selection += kmStatus_1_2;
                } else {
                    selection += km_status;
                    selectionArgs.add(String.valueOf(status));
                }

                if (lastFetchTime != null && lastFetchTime > 0) {
                    selection += m_createdAt;
                    selectionArgs.add(String.valueOf(lastFetchTime));
                }

                cursor = db.query("sms m inner join channel ch on m.channelKey = ch.channelKey", new String[]{"max(createdAt) , m.*"}, selection, selectionArgs.toArray(new String[0]), "m.channelKey", null, "createdAt desc");
            }
            List<Message> messageList = getLatestMessageList(cursor);
            return messageList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
    }


    public int getTotalUnreadCountForSupportGroup(int status) {
        int count = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            String statusQuery = status == 2 ? "kmStatus in (1, 2)" : "kmStatus = ?";
            String sql = "select sum(" + MobiComDatabaseHelper.UNREAD_COUNT + ") from channel where " + statusQuery;

            SQLiteStatement statement = db.compileStatement(sql);
            if (status != 2) {
                statement.bindString(1, String.valueOf(status));
            }
            long records = statement.simpleQueryForLong();
            count = (int) records;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
        return count;
    }

    public String deleteMessage(Message message, String contactNumber) {
        if (!message.isSentToServer()) {
            deleteMessageFromDb(message);
        } else if (isMessagePresent(message.getKeyString(), Message.ReplyMessage.REPLY_MESSAGE.getValue())) {
            updateReplyFlag(message.getKeyString(), Message.ReplyMessage.HIDE_MESSAGE.getValue());
        } else if (!isMessagePresent(message.getKeyString(), Message.ReplyMessage.HIDE_MESSAGE.getValue())) {
            deleteMessageFromDb(message);
        }
        return null;
    }

    public void deleteMessageFromDb(Message message) {
        try {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            database.delete("sms", "keyString" + "='" + message.getKeyString() + "'", null);
            dbHelper.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteConversation(String contactNumber) {
        Utils.printLog(context, TAG, "Deleting conversation for contactNumber: " + contactNumber);
        int deletedRows = dbHelper.getWritableDatabase().delete("sms", contactNo_And_channelKey, new String[]{contactNumber});
        updateContactUnreadCountToZero(contactNumber);
        dbHelper.close();
        Utils.printLog(context, TAG, "Delete " + deletedRows + " messages.");
    }

    public void deleteChannelConversation(Integer channelKey) {
        Utils.printLog(context, TAG, "Deleting  Conversation for channel: " + channelKey);
        int deletedRows = dbHelper.getWritableDatabase().delete("sms", "channelKey=?", new String[]{String.valueOf(channelKey)});
        updateChannelUnreadCountToZero(channelKey);
        dbHelper.close();
        Utils.printLog(context, TAG, "Delete " + deletedRows + " messages.");
    }

    public void updateContactUnreadCount(String userId) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int unreadCount = getUnreadMessageCountForContact(userId);
            ContentValues values = new ContentValues();
            values.put("unreadCount", unreadCount + 1);
            db.update("contact", values, "userId = ?", new String[]{userId});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void updateChannelUnreadCount(Integer channelKey) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int unreadCount = getUnreadMessageCountForChannel(channelKey);
            ContentValues values = new ContentValues();
            values.put("unreadCount", unreadCount + 1);
            db.update("channel", values, "channelKey = ?", new String[]{String.valueOf(channelKey)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void decreaseChannelUnreadCount(Integer channelKey) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int unreadCount = getUnreadMessageCountForChannel(channelKey);
            ContentValues values = new ContentValues();
            values.put("unreadCount", unreadCount - 1);
            db.update("channel", values, "channelKey = ? AND unreadCount > 0", new String[]{String.valueOf(channelKey)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateChannelUnreadCountToZero(Integer channelKey) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("unreadCount", 0);
            db.update("channel", values, "channelKey = ?", new String[]{String.valueOf(channelKey)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void replaceExistingMessage(Message message) {
        deleteMessageFromDb(message);
        createMessage(message);
    }

    public synchronized void updateContactUnreadCountToZero(String userId) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("unreadCount", 0);
            db.update("contact", values, "userId = ?", new String[]{userId});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateReplyFlag(String messageKey, int isReplyMessage) {
        ContentValues values = new ContentValues();
        values.put("replyMessage", isReplyMessage);
        int updatedMessage = dbHelper.getWritableDatabase().update("sms", values, " keyString = '" + messageKey + "'", null);
    }

    public void updateMessageReplyType(String messageKey, Integer replyMessage) {
        try {
            ContentValues values = new ContentValues();
            values.put("replyMessage", replyMessage);
            dbHelper.getWritableDatabase().update("sms", values, "keyString = ?", new String[]{messageKey});
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public int getTotalUnreadCount() {
        Cursor channelCursor = null;
        Cursor contactCursor = null;
        int totalCount = 0;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            contactCursor = db.query("contact", null, "unreadCount > 0", null, null, null, null);
            channelCursor = db.query("channel", null, "unreadCount > 0", null, null, null, null);

            if (contactCursor.moveToFirst()) {
                do {
                    totalCount = totalCount + contactCursor.getInt(contactCursor.getColumnIndex(MobiComDatabaseHelper.UNREAD_COUNT));
                } while (contactCursor.moveToNext());
            }

            if (channelCursor.moveToFirst()) {
                do {
                    totalCount = totalCount + channelCursor.getInt(channelCursor.getColumnIndex(MobiComDatabaseHelper.UNREAD_COUNT));
                } while (channelCursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (channelCursor != null) {
                channelCursor.close();
            }
            if (contactCursor != null) {
                contactCursor.close();
            }
        }
        return totalCount;
    }

    public List<Message> getAttachmentMessages(String contactId, Integer groupId, boolean downloadedOnly) {

        if (contactId == null && (groupId == null || groupId == 0)) {
            return new ArrayList<>();
        }
        String selection = "";
        List<String> selectionArgs = new ArrayList<>();
        if (groupId != null && groupId != 0) {
            selection = MobiComDatabaseHelper.CHANNEL_KEY + " = ? AND ";
            selectionArgs.add(String.valueOf(groupId));
        } else if (contactId != null) {
            selection = "contactNumbers = ? AND ";
            selectionArgs.add(contactId);
        }
        selection += (downloadedOnly ? " filePaths" : blobKeyString) + " IS NOT NULL";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(MobiComDatabaseHelper.SMS_TABLE_NAME, null, selection, selectionArgs.toArray(new String[0]), null, null, "createdAt DESC");
        return getMessageList(cursor);

    }

    public void updateMessageMetadata(String keyString, Map<String, String> metadata) {
        ContentValues values = new ContentValues();

        if (isMessagePresent(keyString)) {
            values.put(MobiComDatabaseHelper.MESSAGE_METADATA, GsonUtils.getJsonFromObject(metadata, Map.class));
            dbHelper.getWritableDatabase().update("sms", values, "keyString='" + keyString + "'", null);
        }

        dbHelper.close();
    }

}
