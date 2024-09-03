package com.applozic.mobicommons.commons.core.utils;

import android.content.Context;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;

import android.text.TextUtils;
import android.util.Log;

import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicommons.ALSpecificSettings;
import com.applozic.mobicommons.ApplozicService;

import java.io.File;

/**
 * Created by devashish on 25/1/15.
 */
public class DBUtils {

    private static final String TAG = "DBUtils";
    private static final String SQLITE_MASTER = "sqlite_master";
    private static final String TABLE_NAME = "tbl_name";

    public static boolean isTableExists(SQLiteDatabase database, String tableName) {
        Cursor cursor = database.query(true, SQLITE_MASTER, new String[]{TABLE_NAME}, "tbl_name = ?", new String[]{String.valueOf(tableName)}, null, null, null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public static boolean existsColumnInTable(SQLiteDatabase inDatabase, String inTable, String columnToCheck) {
        Cursor mCursor = null;
        try {
            //query 1 row
            mCursor = inDatabase.query(inTable, null, "LIMIT 0", null, null, null, null);
            //getColumnIndex gives us the index (0 to ...) of the column - otherwise we get a -1
            return mCursor.getColumnIndex(columnToCheck) != -1;
        } catch (Exception exp) {
            //something went wrong. Missing the database? The table?
            exp.printStackTrace();
            Log.e(TAG, "... - existsColumnInTable, when checking whether a column exists in the table, an error occurred: " + exp.getMessage());
            return false;
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
        }
    }

    public static boolean isDatabaseEncrypted(Context context, String dbName) {
        String appId = MobiComKitClientService.getApplicationKey(ApplozicService.getContext(context));
        File dbFile = context.getDatabasePath(dbName);

        // Attempt to open the database with the given password
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(dbFile.getPath(), appId, null, SQLiteDatabase.OPEN_READONLY);
            db.close();
            return true;
        } catch (net.sqlcipher.database.SQLiteException e) {
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public static void migrateToSQLCypher(Context context, String dbName) {
        String appId = MobiComKitClientService.getApplicationKey(ApplozicService.getContext(context));
        // Open your existing unencrypted database
        File originalFile = context.getDatabasePath(dbName + ".db");

        // Open a new encrypted database
        SQLiteDatabase encryptedDb = SQLiteDatabase.openOrCreateDatabase(originalFile, appId, null);

        // Migrate your data by copying from the old unencrypted database to the new encrypted database
        try {
            originalFile.renameTo(context.getDatabasePath("unencrypted_temp.db"));

            // Open the original unencrypted database
            SQLiteDatabase unencryptedDb = SQLiteDatabase.openDatabase(originalFile.getPath(), "", null, SQLiteDatabase.OPEN_READWRITE);

            // Copy data from unencrypted to encrypted database
            unencryptedDb.execSQL("ATTACH DATABASE '" + encryptedDb.getPath() + "' AS encrypted_db KEY '" + appId + "';");
            unencryptedDb.execSQL("SELECT sqlcipher_export('encrypted_db');");
            unencryptedDb.execSQL("DETACH DATABASE encrypted_db;");

            // Close both databases
            unencryptedDb.close();
            encryptedDb.close();

            // Delete the unencrypted database
            originalFile.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isTableEmpty(SQLiteDatabase database, String table) {
        String sql = "SELECT COUNT(*) FROM " + table;
        SQLiteStatement statement = database.compileStatement(sql);
        long records = statement.simpleQueryForLong();
        statement.close();
        return records == 0;
    }
}
