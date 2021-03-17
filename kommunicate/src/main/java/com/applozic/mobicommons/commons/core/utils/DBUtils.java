package com.applozic.mobicommons.commons.core.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * Created by devashish on 25/1/15.
 */
public class DBUtils {

    private static final String TAG = "DBUtils";

    public static boolean isTableExists(SQLiteDatabase database, String tableName) {
        Cursor cursor = database.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
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
            mCursor = inDatabase.rawQuery("SELECT * FROM " + inTable + " LIMIT 0", null);

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

    public static boolean isTableEmpty(SQLiteDatabase database, String table) {
        String sql = "SELECT COUNT(*) FROM " + table;
        SQLiteStatement statement = database.compileStatement(sql);
        long records = statement.simpleQueryForLong();
        statement.close();
        return records == 0;
    }
}
