package io.kommunicate.database

import android.content.Context
import android.database.Cursor
import io.kommunicate.devkit.api.MobiComKitClientService
import io.kommunicate.commons.AppContextService
import net.zetetic.database.sqlcipher.SQLiteDatabase
import android.database.sqlite.SQLiteException

object DatabaseMigrationHelper {
    private const val TEMP_ENCRYPTED_DB_NAME = "temp_encrypted.db"

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
