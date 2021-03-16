package com.applozic.mobicomkit.contact.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.database.MobiComDatabaseHelper;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by adarsh on 9/7/15.
 */
public class ContactDatabase {

    public static final String CONTACT = "contact";
    private static final String TAG = "ContactDatabaseService";
    private Context context = null;
    private MobiComUserPreference userPreferences;
    private MobiComDatabaseHelper dbHelper;

    public ContactDatabase(Context context) {
        this.context = ApplozicService.getContext(context);
        this.userPreferences = MobiComUserPreference.getInstance(ApplozicService.getContext(context));
        this.dbHelper = MobiComDatabaseHelper.getInstance(ApplozicService.getContext(context));
    }


    public Contact getContact(Cursor cursor) {
        return getContact(cursor, null);
    }

    /**
     * Form a single contact from cursor
     *
     * @param cursor
     * @return
     */
    public Contact getContact(Cursor cursor, String primaryKeyAliash) {
        Contact contact = new Contact();

        try {
            contact.setFullName(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.FULL_NAME)));
            contact.setUserId(cursor.getString(cursor.getColumnIndex(primaryKeyAliash == null ? MobiComDatabaseHelper.USERID : primaryKeyAliash)));
            contact.setLocalImageUrl(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.CONTACT_IMAGE_LOCAL_URI)));
            contact.setImageURL(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.CONTACT_IMAGE_URL)));
            contact.setContactNumber(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.CONTACT_NO)));
            contact.setApplicationId(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.APPLICATION_ID)));
            Long connected = cursor.getLong(cursor.getColumnIndex(MobiComDatabaseHelper.CONNECTED));
            contact.setContactType(cursor.getShort(cursor.getColumnIndex(MobiComDatabaseHelper.CONTACT_TYPE)));
            contact.setConnected(connected != 0 && connected.intValue() == 1);
            contact.setLastSeenAt(cursor.getLong(cursor.getColumnIndex(MobiComDatabaseHelper.LAST_SEEN_AT_TIME)));
            contact.setUnreadCount(cursor.getInt(cursor.getColumnIndex(MobiComDatabaseHelper.UNREAD_COUNT)));
            Boolean userBlocked = (cursor.getInt(cursor.getColumnIndex(MobiComDatabaseHelper.BLOCKED)) == 1);
            contact.setBlocked(userBlocked);
            Boolean userBlockedBy = (cursor.getInt(cursor.getColumnIndex(MobiComDatabaseHelper.BLOCKED_BY)) == 1);
            contact.setBlockedBy(userBlockedBy);
            contact.setEmailId(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.EMAIL)));
            contact.setStatus(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.STATUS)));
            contact.setUserTypeId(cursor.getShort(cursor.getColumnIndex(MobiComDatabaseHelper.USER_TYPE_ID)));
            contact.setDeletedAtTime(cursor.getLong(cursor.getColumnIndex(MobiComDatabaseHelper.DELETED_AT)));
            contact.setNotificationAfterTime(cursor.getLong(cursor.getColumnIndex(MobiComDatabaseHelper.NOTIFICATION_AFTER_TIME)));
            contact.setRoleType(cursor.getShort(cursor.getColumnIndex(MobiComDatabaseHelper.USER_ROLE_TYPE)));
            contact.setLastMessageAtTime(cursor.getLong(cursor.getColumnIndex(MobiComDatabaseHelper.LAST_MESSAGED_AT)));

            String metadata = cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.USER_METADATA));
            if (!TextUtils.isEmpty(metadata)) {
                contact.setMetadata((Map<String, String>) GsonUtils.getObjectFromJson(metadata, Map.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contact;
    }

    /**
     * Form a single contact details from cursor
     *
     * @param cursor
     * @return
     */
    public List<Contact> getContactList(Cursor cursor) {

        List<Contact> smsList = new ArrayList<Contact>();
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                smsList.add(getContact(cursor));
            } while (cursor.moveToNext());
        }
        return smsList;
    }

    public List<Contact> getAllContactListExcludingLoggedInUser() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getUserId())) {
            return new ArrayList<Contact>();
        }
        String structuredNameWhere = MobiComDatabaseHelper.USERID + " != ?";
        Cursor cursor = null;
        try {
            cursor = db.query(CONTACT, null, structuredNameWhere, new String[]{MobiComUserPreference.getInstance(context).getUserId()}, null, null, MobiComDatabaseHelper.FULL_NAME + " asc");
            return getContactList(cursor);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
    }

    public List<Contact> getAllContact() {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.query(CONTACT, null, null, null, null, null, MobiComDatabaseHelper.FULL_NAME + " asc");
            return getContactList(cursor);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
    }

    public Cursor getContactCursorByIdForLoader(String id) {
        Cursor cursor = null;
        try {
            if (TextUtils.isEmpty(id)) {
                return null;
            }
            String queryForLoaded = "SELECT c.userId AS _id,c.fullName,c.contactNO,c.displayName,c.contactImageURL,c.contactImageLocalURI,c.email,c.applicationId,c.connected,c.lastSeenAt,c.unreadCount,c.blocked,c.blockedBy,c.status,c.contactType,c.userTypeId,c.deletedAtTime,c.notificationAfterTime,c.userRoleType,c.lastMessagedAt,c.userMetadata FROM contact c WHERE userId = ?";

            SQLiteDatabase database = dbHelper.getReadableDatabase();
            cursor = database.rawQuery(queryForLoaded, new String[]{id});
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                }
            }
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
        return null;
    }

    public Contact getContactById(String id) {
        Cursor cursor = null;
        try {
            if (TextUtils.isEmpty(id)) {
                return null;
            }
            String structuredNameWhere = MobiComDatabaseHelper.USERID + " =?";
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.query(CONTACT, null, structuredNameWhere, new String[]{id}, null, null, null);
            Contact contact = null;
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    contact = getContact(cursor);
                }
            }
            return contact;
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

    public void updateContact(Contact contact) {
        ContentValues contentValues = prepareContactValues(contact, true);
        dbHelper.getWritableDatabase().update(CONTACT, contentValues, MobiComDatabaseHelper.USERID + "=?", new String[]{contact.getUserId()});
        dbHelper.close();
    }

    public void updateLocalImageUri(Contact contact) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MobiComDatabaseHelper.CONTACT_IMAGE_LOCAL_URI, contact.getLocalImageUrl());
        dbHelper.getWritableDatabase().update(CONTACT, contentValues, MobiComDatabaseHelper.USERID + "=?", new String[]{contact.getUserId()});
    }

    public void updateConnectedOrDisconnectedStatus(String userId, Date date, boolean connected) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MobiComDatabaseHelper.CONNECTED, connected ? 1 : 0);
        contentValues.put(MobiComDatabaseHelper.LAST_SEEN_AT_TIME, date.getTime());

        try {
            dbHelper.getWritableDatabase().update(CONTACT, contentValues, MobiComDatabaseHelper.USERID + "=?", new String[]{userId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public void updateLastSeenTimeAt(String userId, long lastSeenTime) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MobiComDatabaseHelper.LAST_SEEN_AT_TIME, lastSeenTime);
            dbHelper.getWritableDatabase().update(CONTACT, contentValues, MobiComDatabaseHelper.USERID + "=?", new String[]{userId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }


    public void updateUserBlockStatus(String userId, boolean userBlocked) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MobiComDatabaseHelper.BLOCKED, userBlocked ? 1 : 0);
            dbHelper.getWritableDatabase().update(CONTACT, contentValues, MobiComDatabaseHelper.USERID + "=?", new String[]{userId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public void updateUserBlockByStatus(String userId, boolean userBlockedBy) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MobiComDatabaseHelper.BLOCKED_BY, userBlockedBy ? 1 : 0);
            dbHelper.getWritableDatabase().update(CONTACT, contentValues, MobiComDatabaseHelper.USERID + "=?", new String[]{userId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public void addContact(Contact contact) {
        try {
            ContentValues contentValues = prepareContactValues(contact, false);
            dbHelper.getWritableDatabase().insert(CONTACT, null, contentValues);
        } catch (Exception e) {
            Utils.printLog(context, TAG, "Ignoring duplicate entry for contact");
        } finally {
            dbHelper.close();
        }
    }

    public ContentValues prepareContactValues(Contact contact, boolean isContactUpdated) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MobiComDatabaseHelper.FULL_NAME, getFullNameForUpdate(contact));

        if (!TextUtils.isEmpty(contact.getContactNumber())) {
            contentValues.put(MobiComDatabaseHelper.CONTACT_NO, contact.getContactNumber());
        }

        if (!TextUtils.isEmpty(contact.getImageURL())) {
            contentValues.put(MobiComDatabaseHelper.CONTACT_IMAGE_URL, contact.getImageURL());
        } else {
            contentValues.putNull(MobiComDatabaseHelper.CONTACT_IMAGE_LOCAL_URI);
            contentValues.putNull(MobiComDatabaseHelper.CONTACT_IMAGE_URL);
        }

        if (!TextUtils.isEmpty(contact.getLocalImageUrl())) {
            contentValues.put(MobiComDatabaseHelper.CONTACT_IMAGE_LOCAL_URI, contact.getLocalImageUrl());
        }
        contentValues.put(MobiComDatabaseHelper.USERID, contact.getUserId());
        if (!TextUtils.isEmpty(contact.getEmailId())) {
            contentValues.put(MobiComDatabaseHelper.EMAIL, contact.getEmailId());
        }
        if (!TextUtils.isEmpty(contact.getApplicationId())) {
            contentValues.put(MobiComDatabaseHelper.APPLICATION_ID, contact.getApplicationId());
        }

        contentValues.put(MobiComDatabaseHelper.CONNECTED, contact.isConnected() ? 1 : 0);
        if (contact.getLastSeenAt() != 0) {
            contentValues.put(MobiComDatabaseHelper.LAST_SEEN_AT_TIME, contact.getLastSeenAt());
        }
        if (contact.getUnreadCount() != null && contact.getUnreadCount() != 0) {
            contentValues.put(MobiComDatabaseHelper.UNREAD_COUNT, contact.getUnreadCount());
        }
        contentValues.put(MobiComDatabaseHelper.STATUS, contact.getStatus());
        if (contact.isBlocked()) {
            contentValues.put(MobiComDatabaseHelper.BLOCKED, contact.isBlocked());
        }
        if (contact.isBlockedBy()) {
            contentValues.put(MobiComDatabaseHelper.BLOCKED_BY, contact.isBlockedBy());
        }
        if (contact.getContactType() != 0) {
            contentValues.put(MobiComDatabaseHelper.CONTACT_TYPE, contact.getContactType());
        }
        if (contact.getNotificationAfterTime() != null && contact.getNotificationAfterTime() != 0) {
            contentValues.put(MobiComDatabaseHelper.NOTIFICATION_AFTER_TIME, contact.getNotificationAfterTime());
        }

        Map<String, String> metadata = getUpdatedMetadata(contact, isContactUpdated);

        if (metadata != null && !metadata.isEmpty()) {
            contentValues.put(MobiComDatabaseHelper.USER_METADATA, GsonUtils.getJsonFromObject(metadata, Map.class));
        }
        contentValues.put(MobiComDatabaseHelper.USER_ROLE_TYPE, contact.getRoleType());
        contentValues.put(MobiComDatabaseHelper.LAST_MESSAGED_AT, contact.getLastMessageAtTime());
        contentValues.put(MobiComDatabaseHelper.USER_TYPE_ID, contact.getUserTypeId());
        if (contact.getDeletedAtTime() != null && contact.getDeletedAtTime() != 0) {
            contentValues.put(MobiComDatabaseHelper.DELETED_AT, contact.getDeletedAtTime());
        }
        return contentValues;
    }

    private Map<String, String> getUpdatedMetadata(Contact contact, boolean isContactUpdate) {
        Map<String, String> metadata = contact.getMetadata();
        if (isContactUpdate) {
            if (metadata != null && !metadata.isEmpty() && metadata.containsKey(Contact.AL_DISPLAY_NAME_UPDATED)) {
                return metadata;
            }
            Contact existingContact = getContactById(contact.getUserId());
            Map<String, String> existingMetadata = existingContact.getMetadata();
            if (metadata != null && existingMetadata != null && !existingMetadata.isEmpty() && existingMetadata.containsKey(Contact.AL_DISPLAY_NAME_UPDATED)) {
                String flag = existingMetadata.get(Contact.AL_DISPLAY_NAME_UPDATED);
                if (!TextUtils.isEmpty(flag)) {
                    metadata.put(Contact.AL_DISPLAY_NAME_UPDATED, flag);
                }
            }
        }
        return metadata;
    }

    /**
     * This method will return full name of contact to be updated.
     * This is require to avoid updating full name back to userId in case fullname is not set while updating contact.
     *
     * @param contact
     * @return
     */
    private String getFullNameForUpdate(Contact contact) {

        String fullName = contact.getDisplayName();
        if (TextUtils.isEmpty(contact.getFullName())) {
            Contact contactFromDB = getContactById(contact.getUserId());
            if (contactFromDB != null) {
                fullName = contactFromDB.getFullName();
            }
        }
        return fullName;
    }

    public boolean isContactPresent(String userId) {
        Cursor cursor = null;
        try {
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            cursor = database.rawQuery(
                    "SELECT COUNT(*) FROM contact WHERE userId = ?",
                    new String[]{userId});
            cursor.moveToFirst();
            return cursor.getInt(0) > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
    }

    public void addAllContact(List<Contact> contactList) {
        for (Contact contact : contactList) {
            addContact(contact);
        }
    }

    public void deleteContact(Contact contact) {
        deleteContactById(contact.getUserId());
    }

    public void deleteContactById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(CONTACT, "userId=?", new String[]{id});
        dbHelper.close();
    }

    public void deleteAllContact(List<Contact> contacts) {
        for (Contact contact : contacts) {
            deleteContact(contact);
        }
    }

    public void updateNotificationAfterTime(String userId, Long notificationAfterTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MobiComDatabaseHelper.NOTIFICATION_AFTER_TIME, notificationAfterTime);
        dbHelper.getWritableDatabase().update(CONTACT, contentValues, MobiComDatabaseHelper.USERID + "=?", new String[]{userId});
    }

    public int getChatUnreadCount() {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery("SELECT COUNT(DISTINCT (userId)) FROM contact WHERE unreadCount > 0 ", null);
            cursor.moveToFirst();
            int chatCount = 0;
            if (cursor.getCount() > 0) {
                chatCount = cursor.getInt(0);
            }
            return chatCount;
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

    public int getGroupUnreadCount() {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery("SELECT COUNT(DISTINCT (channelKey)) FROM channel WHERE unreadCount > 0 ", null);
            cursor.moveToFirst();
            int groupCount = 0;
            if (cursor.getCount() > 0) {
                groupCount = cursor.getInt(0);
            }
            return groupCount;
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

    public Loader<Cursor> getSearchCursorLoader(final String searchString, final String[] userIdArray) {
        return getSearchCursorLoader(searchString, userIdArray, null, null);
    }

    public Loader<Cursor> getSearchCursorLoader(final String searchString, final String[] userIdArray, final Integer parentGroupKey) {
        return getSearchCursorLoader(searchString, userIdArray, parentGroupKey, null);
    }

    public Loader<Cursor> getSearchCursorLoader(final String searchString, final String[] userIdArray, final Integer parentGroupKey, final String pinnedContactUserId) {

        return new CursorLoader(context, null, null, null, null, MobiComDatabaseHelper.DISPLAY_NAME + " asc") {
            @Override
            public Cursor loadInBackground() {

                if (TextUtils.isEmpty(userPreferences.getUserId())) {
                    return null;
                }
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor;


                String query = null;
                if (parentGroupKey != null && parentGroupKey != 0) {
                    query = "Select DISTINCT(c.userId) as _id,c.fullName,c.contactNO,c.displayName,c.contactImageURL,c.contactImageLocalURI,c.email,c.applicationId,c.connected,c.lastSeenAt,c.unreadCount,c.blocked,c.blockedBy,c.status,c.contactType,c.userTypeId,c.deletedAtTime,c.notificationAfterTime,c.userRoleType,c.lastMessagedAt,c.userMetadata from contact c join channel_User_X cux on cux.userId = c.userId where ( cux.channelKey = '" + parentGroupKey + "' OR cux.parentGroupKey = '" + parentGroupKey + "' ) AND c.userId NOT IN ('" + userPreferences.getUserId().replaceAll("'", "''") + "')";
                    if (!TextUtils.isEmpty(searchString)) {
                        query = query + " AND c.fullName like '%" + searchString.replaceAll("'", "''") + "%'";
                    } else { //if searching, then no need ignore pinned contact
                        if(!TextUtils.isEmpty(pinnedContactUserId)) {
                            query = query + " AND c.userId NOT IN ('" + pinnedContactUserId.replaceAll("'", "''") + "')";
                        }
                    }
                    query = query + " order by c.fullName,c.userId asc ";
                    cursor = db.rawQuery(query, null);
                } else {
                    query = "select userId as _id, fullName, contactNO, " +
                            "displayName,contactImageURL,contactImageLocalURI,email," +
                            "applicationId,connected,lastSeenAt,unreadCount,blocked," +
                            "blockedBy,status,contactType,userTypeId,deletedAtTime,notificationAfterTime,userRoleType,userMetadata,lastMessagedAt from " + CONTACT + " where deletedAtTime=0 ";

                    if (userIdArray != null && userIdArray.length > 0) {
                        String placeHolderString = Utils.makePlaceHolders(userIdArray.length);
                        if (!TextUtils.isEmpty(searchString)) {
                            query = query + " and fullName like '%" + searchString.replaceAll("'", "''") + "%' and  userId  IN (" + placeHolderString + ")";
                        } else {
                            query = query + " and userId IN (" + placeHolderString + ")";
                        }
                        query = query + " order by connected desc,lastSeenAt desc ";

                        cursor = db.rawQuery(query, userIdArray);
                    } else {
                        if (ApplozicClient.getInstance(context).isShowMyContacts()) {
                            if (!TextUtils.isEmpty(searchString)) {
                                query = query + " and fullName like '%" + searchString.replaceAll("'", "''") + "%' AND contactType != 0 AND userId NOT IN ('" + userPreferences.getUserId().replaceAll("'", "''") + "')";
                            } else {
                                query = query + " and contactType != 0 AND userId != '" + userPreferences.getUserId() + "'";
                            }
                        } else {
                            if (!TextUtils.isEmpty(searchString)) {
                                query = query + " and fullName like '%" + searchString.replaceAll("'", "''") + "%' AND userId NOT IN ('" + userPreferences.getUserId().replaceAll("'", "''") + "')";
                            } else {
                                query = query + " and userId != '" + userPreferences.getUserId() + "'";
                            }
                        }
                        if(TextUtils.isEmpty(searchString) && !TextUtils.isEmpty(pinnedContactUserId)) { //ignore pinned contact only if search input is empty
                            query = query + " AND userId NOT IN ('" + pinnedContactUserId.replaceAll("'", "''") + "')";
                        }
                        query = query + " order by fullName COLLATE NOCASE,userId COLLATE NOCASE asc ";
                        cursor = db.rawQuery(query, null);
                    }

                }
                return cursor;
            }
        };
    }

    public boolean isContactPresent(String contactNumber, Contact.ContactType contactType) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery("SELECT COUNT(*) FROM contact where  " + MobiComDatabaseHelper.CONTACT_NO + " = ?  AND " + MobiComDatabaseHelper.DEVICE_CONTACT_TYPE + " = ? ", new String[]{contactNumber, String.valueOf(contactType.getValue())});
            cursor.moveToFirst();
            return cursor.getInt(0) > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
        return false;
    }

    public void saveOrUpdate(Contact contact) {
        Contact existingContact = getContactById(contact.getUserId());
        if (existingContact == null) {
            addContact(contact);
        } else {
            updateContact(contact);
        }
    }

    public List<Contact> getContacts(Contact.ContactType contactType) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String structuredNameWhere = MobiComDatabaseHelper.CONTACT_TYPE + " = ?";
            cursor = db.query(CONTACT, null, structuredNameWhere, new String[]{String.valueOf(contactType.getValue())}, null, null, MobiComDatabaseHelper.FULL_NAME + " asc");
            return getContactList(cursor);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
    }

    public void updateMetadataKeyValueForUserId(String userId, String key, String value) {
        Contact contact = getContactById(userId);
        if (contact != null) {
            Map<String, String> metadata = contact.getMetadata();
            if (metadata != null && !metadata.isEmpty()) {
                metadata.put(key, value);
                ContentValues contentValues = new ContentValues();
                contentValues.put(MobiComDatabaseHelper.USER_METADATA, GsonUtils.getJsonFromObject(metadata, Map.class));
                dbHelper.getWritableDatabase().update(CONTACT, contentValues, MobiComDatabaseHelper.USERID + "=?", new String[]{contact.getUserId()});
            }
        }
    }
}
