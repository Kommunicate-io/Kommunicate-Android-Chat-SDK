package io.kommunicate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;

import com.applozic.mobicommons.ApplozicService;

import java.util.ArrayList;
import java.util.List;

import io.kommunicate.models.KmAutoSuggestionModel;

public class KmAutoSuggestionDatabase {
    private Context context;
    private KmDatabaseHelper dbHelper;
    private static KmAutoSuggestionDatabase kmAutoSuggestionDatabase;

    private KmAutoSuggestionDatabase(Context context) {
        this.context = ApplozicService.getContext(context);
        this.dbHelper = KmDatabaseHelper.getInstance(context);
    }

    public static KmAutoSuggestionDatabase getInstance(Context context) {
        if (kmAutoSuggestionDatabase == null) {
            kmAutoSuggestionDatabase = new KmAutoSuggestionDatabase(context);
        }
        return kmAutoSuggestionDatabase;
    }

    public static KmAutoSuggestionModel getAutoSuggestion(Cursor cursor) {
        KmAutoSuggestionModel autoSuggestionModel = new KmAutoSuggestionModel();
        autoSuggestionModel.setId(cursor.getLong(cursor.getColumnIndex(KmDatabaseHelper.ID)));
        autoSuggestionModel.setCategory(cursor.getString(cursor.getColumnIndex(KmDatabaseHelper.CATEGORY)));
        autoSuggestionModel.setContent(cursor.getString(cursor.getColumnIndex(KmDatabaseHelper.CONTENT)));
        autoSuggestionModel.setCreatedAt(cursor.getLong(cursor.getColumnIndex(KmDatabaseHelper.CREATED_AT)));
        autoSuggestionModel.setUpdatedAt(cursor.getLong(cursor.getColumnIndex(KmDatabaseHelper.UPDATED_AT)));
        autoSuggestionModel.setDeleted(cursor.getInt(cursor.getColumnIndex(KmDatabaseHelper.DELETED)) == 1);
        autoSuggestionModel.setName(cursor.getString(cursor.getColumnIndex(KmDatabaseHelper.NAME)));
        autoSuggestionModel.setType(cursor.getString(cursor.getColumnIndex(KmDatabaseHelper.TYPE)));
        autoSuggestionModel.setUserName(cursor.getString(cursor.getColumnIndex(KmDatabaseHelper.USER_NAME)));

        return autoSuggestionModel;
    }

    public ContentValues getContentValues(KmAutoSuggestionModel autoSuggestionModel) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KmDatabaseHelper.ID, autoSuggestionModel.getId());
        contentValues.put(KmDatabaseHelper.CATEGORY, autoSuggestionModel.getCategory());
        contentValues.put(KmDatabaseHelper.CONTENT, autoSuggestionModel.getContent());
        contentValues.put(KmDatabaseHelper.CREATED_AT, autoSuggestionModel.getCreatedAt());
        contentValues.put(KmDatabaseHelper.UPDATED_AT, autoSuggestionModel.getUpdatedAt());
        contentValues.put(KmDatabaseHelper.NAME, autoSuggestionModel.getName());
        contentValues.put(KmDatabaseHelper.TYPE, autoSuggestionModel.getType());
        contentValues.put(KmDatabaseHelper.USER_NAME, autoSuggestionModel.getUserName());
        if (autoSuggestionModel.isDeleted()) {
            contentValues.put(KmDatabaseHelper.DELETED, 1);
        }

        return contentValues;
    }

    public synchronized void addAutoSuggestion(KmAutoSuggestionModel autoSuggestion) {
        try {
            dbHelper.getWritableDatabase().insertOrThrow(KmDatabaseHelper.AUTO_SUGGESTION_TABLE, null, getContentValues(autoSuggestion));
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public synchronized void updateAutoSuggestion(KmAutoSuggestionModel autoSuggestion) {
        try {
            dbHelper.getWritableDatabase().update(KmDatabaseHelper.AUTO_SUGGESTION_TABLE, getContentValues(autoSuggestion), "id='" + autoSuggestion.getId() + "'", null);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public void upsertAutoSuggestion(KmAutoSuggestionModel autoSuggestion) {
        if (autoSuggestion == null) {
            return;
        }
        if (isAutoSuggestionPresent(autoSuggestion)) {
            updateAutoSuggestion(autoSuggestion);
        } else {
            addAutoSuggestion(autoSuggestion);
        }
    }

    public boolean isAutoSuggestionPresent(KmAutoSuggestionModel autoSuggestion) {
        if (autoSuggestion == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            cursor = database.rawQuery(
                    "SELECT COUNT(*) FROM " + KmDatabaseHelper.AUTO_SUGGESTION_TABLE + " WHERE " + KmDatabaseHelper.ID + " = " + autoSuggestion.getId(), null);
            cursor.moveToFirst();
            return cursor.getInt(0) > 0;
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            dbHelper.close();
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public static List<KmAutoSuggestionModel> getAutoSuggestionList(Cursor cursor) {
        List<KmAutoSuggestionModel> kmAutoSuggestionList = new ArrayList<>();
        try {
            cursor.moveToFirst();
            while (cursor.getCount() > 0) {
                do {
                    kmAutoSuggestionList.add(getAutoSuggestion(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return kmAutoSuggestionList;
    }

    public Loader<Cursor> getAutoSuggestionCursorLoader(String typedText) {
        return new AutoSuggestionLoader(context, null, null, null, null, KmDatabaseHelper.CATEGORY + " asc").setArgs(dbHelper, typedText);
    }

    private static class AutoSuggestionLoader extends CursorLoader {
        private KmDatabaseHelper dbHelper;
        private String typedText;

        public AutoSuggestionLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            super(context, uri, projection, selection, selectionArgs, sortOrder);
        }

        public AutoSuggestionLoader setArgs(KmDatabaseHelper dbHelper, String typedText) {
            this.dbHelper = dbHelper;
            this.typedText = typedText;
            return this;
        }

        @Override
        protected Cursor onLoadInBackground() {
            return dbHelper.getReadableDatabase().rawQuery("select * from " + KmDatabaseHelper.AUTO_SUGGESTION_TABLE
                    + " where " + KmDatabaseHelper.DELETED + " = 0"
                    + (!TextUtils.isEmpty(typedText) ? (" AND " + KmDatabaseHelper.CATEGORY + " like '%" + typedText.replaceAll("'", "''") + "%'") : "")
                    + " ORDER BY " + KmDatabaseHelper.CATEGORY + " COLLATE NOCASE asc", null);
        }
    }

}
