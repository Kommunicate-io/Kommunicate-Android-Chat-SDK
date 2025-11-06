package io.kommunicate.database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.util.Log
import io.kommunicate.commons.AppContextService
import io.kommunicate.commons.AppSpecificSettings
import io.kommunicate.devkit.api.MobiComKitClientService
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File

object DatabaseMigrationHelper {
    private const val TEMP_ENCRYPTED_DB_NAME = "temp_encrypted.db"
    private const val TAG = "DatabaseMigrationHelper"


    // Check if table exists in destination DB
    private fun tableExists(db: SQLiteDatabase, tableName: String): Boolean {
        return db.rawQuery(
            "SELECT 1 FROM sqlite_master WHERE type='table' AND name=? LIMIT 1",
            arrayOf(tableName)
        ).use { cursor -> cursor.moveToFirst() }
    }

    @JvmStatic
    @Throws(Exception::class)
    fun migrateDatabase(context: Context, dbName: String) {
        val databaseName = if(context.getDatabasePath(dbName).exists()) {
            dbName
        } else if(context.getDatabasePath(dbName.removeSuffix(".db")).exists()) {
            dbName.removeSuffix(".db")
        } else {
            return
        }

        val password =
            MobiComKitClientService.getApplicationKey(AppContextService.getContext(context))

        // Check added to prevent crash in case of null or empty application key
        if (password.isNullOrEmpty()) {
            System.err.println("Migration failed: Application Key is missing. Is the SDK initialized?")
            return
        }

        // Load SQLCipher libraries
        System.loadLibrary("sqlcipher")

        // File paths for unencrypted and temporary encrypted databases
        val unencryptedDbFile = context.getDatabasePath(databaseName)
        val encryptedTempDbFile = context.getDatabasePath(TEMP_ENCRYPTED_DB_NAME)

        if (!unencryptedDbFile.exists()) {
            throw SQLiteException("Unencrypted database does not exist")
        }

        // Open the unencrypted database
        val unencryptedDb = android.database.sqlite.SQLiteDatabase.openDatabase(
            unencryptedDbFile.path,
            null,
            android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
        )


        // Create the temporary encrypted database
        val encryptedDb = SQLiteDatabase.openOrCreateDatabase(
            encryptedTempDbFile.path,
            password.toByteArray(Charsets.UTF_8), // Convert the password String to a byte array
            null, // CursorFactory
            null  // SQLiteDatabaseHook
        )

        // Copy data from unencrypted to encrypted database
        copyDataBetweenDatabases(unencryptedDb, encryptedDb)

        // Close the databases
        unencryptedDb.close()
        encryptedDb.close()

        // Replace the unencrypted database with the encrypted one
        if (unencryptedDbFile.delete()) {
            val renamed = encryptedTempDbFile.renameTo(unencryptedDbFile)
            if (renamed) {
                println("Migration completed and the encrypted database now has the original name.")
            } else {
                System.err.println("Failed to rename the encrypted database.")
            }
        } else {
            System.err.println("Failed to delete the original unencrypted database.")
        }
    }

    // Copy tables and data from one database to another
    @Throws(Exception::class)
    private fun copyDataBetweenDatabases(sourceDb: android.database.sqlite.SQLiteDatabase, destinationDb: SQLiteDatabase) {
        val cursor: Cursor =
            sourceDb.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
        if (cursor.moveToFirst()) {
            do {
                val tableName = cursor.getString(0)
                if (tableName == "android_metadata" || tableName == "sqlite_sequence") {
                    continue
                }

                // Verifies if table exist
                if (tableExists(destinationDb, tableName)) {
                    println("Table $tableName already exists in destination DB, skipping creation.")
                    continue
                }

                // Copy table schema
                val createTableSql = getTableCreateSql(sourceDb, tableName)
                if (createTableSql != null) {
                    destinationDb.execSQL(createTableSql)
                }

                // Copy data from the table
                val tableCursor: Cursor = sourceDb.query(tableName, null, null, null, null, null, null)
                while (tableCursor.moveToNext()) {
                    // Build INSERT statement
                    val insertSql = StringBuilder("INSERT INTO $tableName VALUES (")
                    for (i in 0 until tableCursor.columnCount) {
                        if (i > 0) insertSql.append(", ")
                        insertSql.append("?") // Use placeholders for values
                    }
                    insertSql.append(")")

                    // Prepare the statement
                    val bindArgs = arrayOfNulls<Any>(tableCursor.columnCount)
                    for (i in 0 until tableCursor.columnCount) {
                        bindArgs[i] = tableCursor.getString(i)
                    }
                    destinationDb.execSQL(insertSql.toString(), bindArgs)
                }
                tableCursor.close()
            } while (cursor.moveToNext())
        }
        cursor.close()
    }


    /**
     * Re-encrypts the database from the old key (applicationId) to the new Keystore-based key.
     * This is a one-time migration for users upgrading the app.
     *
     * @return true if re-keying was successful or not needed, false if it failed and requires intervention.
     */
    @JvmStatic
    fun rekeyDatabaseToKeystore(context: Context, dbName: String): Boolean {
        val appSettings = AppSpecificSettings.getInstance(context)
        if (appSettings.isDbRekeyedToKeystore) {
            // Already migrated, nothing to do.
            Log.i(TAG, "Database re-keying already completed. Skipping.")
            return true
        }

        val dbFile: File = context.getDatabasePath(dbName)
        if (!dbFile.exists()) {
            // New installation, no database to migrate. Mark as complete.
            appSettings.isDbRekeyedToKeystore = true
            Log.i(TAG, "New installation detected. Marking re-keying as complete.")
            return true
        }

        // 1. Get the old and new keys
        val oldKey = MobiComKitClientService.getApplicationKey(context)
        val newKey = DatabaseKeyProvider.getDatabaseKey(context)
        if (oldKey.isNullOrEmpty()) {
            // This happens if the service starts before the SDK is initialized on an upgraded app.
            // We cannot migrate without the old key. Return false to signal a critical failure.
            Log.e(TAG, "CRITICAL: Cannot re-key database because the old application key is missing. A logout is required to recover.")
            return false
        }
        try {
            System.loadLibrary("sqlcipher")
            // 3. Open the database with the OLD key and re-key it within a `use` block.
            SQLiteDatabase.openDatabase(
                dbFile.path,
                oldKey,
                null,
                SQLiteDatabase.OPEN_READWRITE,
                null
            ).use { database ->
                // 4. Re-key the database to the NEW key using PRAGMA rekey
                val newKeyHex = bytesToHex(newKey.toByteArray())
                database.execSQL("PRAGMA rekey = '$newKeyHex'")
            }

            // 5. Mark migration as complete
            appSettings.isDbRekeyedToKeystore = true
            Log.i(TAG, "Database successfully re-keyed to use Android Keystore.")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to re-key database. The database might be corrupt or the old key is incorrect.", e)
            // The re-key failed. Return false to signal this failure.
            return false
        }

    }
    private fun bytesToHex(bytes: ByteArray): String {
        val hexString = StringBuilder(2 * bytes.size)
        for (b in bytes) {
            val hex = Integer.toHexString(0xff and b.toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }

    // Get the CREATE TABLE SQL statement for a specific table
    private fun getTableCreateSql(db: android.database.sqlite.SQLiteDatabase, tableName: String): String? {
        val cursor: Cursor = db.rawQuery(
            "SELECT sql FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(tableName)
        )
        var createTableSql: String? = null
        if (cursor.moveToFirst()) {
            createTableSql = cursor.getString(0)
        }
        cursor.close()
        return createTableSql
    }
}
