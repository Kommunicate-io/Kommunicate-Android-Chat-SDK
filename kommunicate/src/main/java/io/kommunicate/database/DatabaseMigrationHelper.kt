package io.kommunicate.database

import android.content.Context
import android.database.Cursor
import com.applozic.mobicomkit.api.MobiComKitClientService
import com.applozic.mobicommons.ApplozicService
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException

object DatabaseMigrationHelper {
    private const val TEMP_ENCRYPTED_DB_NAME = "temp_encrypted.db"

    @JvmStatic
    fun migrateDatabase(context: Context, dbName: String) {
        val databaseName = if(context.getDatabasePath(dbName).exists()) {
            dbName
        } else if(context.getDatabasePath(dbName.removeSuffix(".db")).exists()) {
            dbName.removeSuffix(".db")
        } else {
            return
        }

        val password =
            MobiComKitClientService.getApplicationKey(ApplozicService.getContext(context))

        // Load SQLCipher libraries
        SQLiteDatabase.loadLibs(context)

        // File paths for unencrypted and temporary encrypted databases
        val unencryptedDbFile = context.getDatabasePath(databaseName)
        val encryptedTempDbFile = context.getDatabasePath(TEMP_ENCRYPTED_DB_NAME)

        if (!unencryptedDbFile.exists()) {
            throw SQLiteException("Unencrypted database does not exist")
        }

        // Open the unencrypted database
        val unencryptedDb = SQLiteDatabase.openDatabase(
            unencryptedDbFile.path,
            "",  // Empty string since it's not encrypted
            null,
            SQLiteDatabase.OPEN_READWRITE
        )

        // Create the temporary encrypted database
        val encryptedDb = SQLiteDatabase.openOrCreateDatabase(
            encryptedTempDbFile.path,
            password,  // Password for encryption
            null
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
    private fun copyDataBetweenDatabases(sourceDb: SQLiteDatabase, destinationDb: SQLiteDatabase) {
        val cursor: Cursor =
            sourceDb.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
        if (cursor.moveToFirst()) {
            do {
                val tableName = cursor.getString(0)
                if (tableName == "android_metadata" || tableName == "sqlite_sequence") {
                    continue
                }

                // Copy table schema
                val createTableSql = getTableCreateSql(sourceDb, tableName)
                destinationDb.execSQL(createTableSql)

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
    private fun getTableCreateSql(db: SQLiteDatabase, tableName: String): String? {
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
