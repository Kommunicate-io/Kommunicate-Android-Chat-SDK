package io.kommunicate.commons.commons.core.utils;

import android.content.Context;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;

import android.util.Log;

import io.kommunicate.devkit.api.MobiComKitClientService;
import io.kommunicate.commons.AppContextService;

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
        String appId = MobiComKitClientService.getApplicationKey(AppContextService.getContext(context));
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

    public static boolean isTableEmpty(SQLiteDatabase database, String table) {
        String sql = "SELECT COUNT(*) FROM " + table;
        SQLiteStatement statement = database.compileStatement(sql);
        long records = statement.simpleQueryForLong();
        statement.close();
        return records == 0;
    }
}
