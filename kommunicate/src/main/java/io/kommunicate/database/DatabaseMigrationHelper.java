package io.kommunicate.database;

import android.content.Context;
import android.database.Cursor;

import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicommons.ApplozicService;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;

import java.io.File;

public class DatabaseMigrationHelper {
    private static final String TEMP_ENCRYPTED_DB_NAME = "temp_encrypted.db";

    public static void migrateDatabase(Context context, String dbName) {
        String databaseName;
        if (context.getDatabasePath(dbName).exists()) {
            databaseName = dbName;
        } else if (context.getDatabasePath(dbName.replace(".db", "")).exists()) {
            databaseName = dbName.replace(".db", "");
        } else {
            return;
        }

        String password = MobiComKitClientService.getApplicationKey(ApplozicService.getContext(context));

        // Load SQLCipher libraries
        SQLiteDatabase.loadLibs(context);

        // File paths for unencrypted and temporary encrypted databases
        File unencryptedDbFile = context.getDatabasePath(databaseName);
        File encryptedTempDbFile = context.getDatabasePath(TEMP_ENCRYPTED_DB_NAME);

        if (!unencryptedDbFile.exists()) {
            throw new SQLiteException("Unencrypted database does not exist");
        }

        // Open the unencrypted database
        SQLiteDatabase unencryptedDb = SQLiteDatabase.openDatabase(
                unencryptedDbFile.getPath(),
                "", // Empty string since it's not encrypted
                null,
                SQLiteDatabase.OPEN_READWRITE
        );

        // Create the temporary encrypted database
        SQLiteDatabase encryptedDb = SQLiteDatabase.openOrCreateDatabase(
                encryptedTempDbFile.getPath(),
                password, // Password for encryption
                null
        );

        // Copy data from unencrypted to encrypted database
        copyDataBetweenDatabases(unencryptedDb, encryptedDb);

        // Close the databases
        unencryptedDb.close();
        encryptedDb.close();

        // Replace the unencrypted database with the encrypted one
        if (unencryptedDbFile.delete()) {
            boolean renamed = encryptedTempDbFile.renameTo(unencryptedDbFile);
            if (renamed) {
                System.out.println("Migration completed and the encrypted database now has the original name.");
            } else {
                System.err.println("Failed to rename the encrypted database.");
            }
        } else {
            System.err.println("Failed to delete the original unencrypted database.");
        }
    }

    // Copy tables and data from one database to another
    private static void copyDataBetweenDatabases(SQLiteDatabase sourceDb, SQLiteDatabase destinationDb) {
        Cursor cursor = sourceDb.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (cursor.moveToFirst()) {
            do {
                String tableName = cursor.getString(0);
                if ("android_metadata".equals(tableName) || "sqlite_sequence".equals(tableName)) {
                    continue;
                }

                // Copy table schema
                String createTableSql = getTableCreateSql(sourceDb, tableName);
                destinationDb.execSQL(createTableSql);

                // Copy data from the table
                Cursor tableCursor = sourceDb.rawQuery("SELECT * FROM " + tableName, null);
                while (tableCursor.moveToNext()) {
                    // Build INSERT statement
                    StringBuilder insertSql = new StringBuilder("INSERT INTO " + tableName + " VALUES (");
                    for (int i = 0; i < tableCursor.getColumnCount(); i++) {
                        if (i > 0) insertSql.append(", ");
                        insertSql.append("?"); // Use placeholders for values
                    }
                    insertSql.append(")");

                    // Prepare the statement
                    Object[] bindArgs = new Object[tableCursor.getColumnCount()];
                    for (int i = 0; i < tableCursor.getColumnCount(); i++) {
                        bindArgs[i] = tableCursor.getString(i);
                    }
                    destinationDb.execSQL(insertSql.toString(), bindArgs);
                }
                tableCursor.close();
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    // Get the CREATE TABLE SQL statement for a specific table
    private static String getTableCreateSql(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery(
                "SELECT sql FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{tableName}
        );
        String createTableSql = null;
        if (cursor.moveToFirst()) {
            createTableSql = cursor.getString(0);
        }
        cursor.close();
        return createTableSql;
    }
}
