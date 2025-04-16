package io.kommunicate.database;

import android.content.Context;
import net.sqlcipher.database.SQLiteDatabase;
import android.text.TextUtils;

import io.kommunicate.devkit.api.MobiComKitClientService;
import io.kommunicate.devkit.database.MobiComDatabaseHelper;
import io.kommunicate.commons.ALSpecificSettings;
import io.kommunicate.commons.AppContextService;
import io.kommunicate.commons.commons.core.utils.DBUtils;

public class KmDatabaseHelper extends MobiComDatabaseHelper {
    private static final int DB_VERSION = 1;
    private static KmDatabaseHelper sInstance;
    public static final String AUTO_SUGGESTION_TABLE = "auto_suggestion";
    public static final String ID = "id";
    public static final String CONTENT = "content";
    public static final String CATEGORY = "category";
    public static final String TYPE = "type";
    public static final String NAME = "name";
    public static final String USER_NAME = "user_name";
    public static final String UPDATED_AT = "updated_at";
    public static final String CREATED_AT = "created_at";
    public static final String DELETED = "deleted";

    private static final String CREATE_AUTO_SUGGESTION_TABLE = "create table " + AUTO_SUGGESTION_TABLE + "( "
            + ID + " integer, "
            + CATEGORY + " varchar(500), "
            + TYPE + " varchar(100), "
            + CONTENT + " varchar(2000), "
            + NAME + " varchar(500), "
            + USER_NAME + " varchar(200), "
            + UPDATED_AT + " integer, "
            + CREATED_AT + " integer, "
            + DELETED + " integer default 0);";

    private KmDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (!DBUtils.isTableExists(db, AUTO_SUGGESTION_TABLE)) {
            db.execSQL(CREATE_AUTO_SUGGESTION_TABLE);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {

        }
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase database = super.getReadableDatabase();
        database.enableWriteAheadLogging();
        return database;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase database = super.getWritableDatabase();
        database.enableWriteAheadLogging();
        return database;
    }

    @Override
    public synchronized void close() {
    }

    private KmDatabaseHelper(Context context) {
        this(context, "KM_" + (!TextUtils.isEmpty(ALSpecificSettings.getInstance(AppContextService.getContext(context)).getDatabaseName()) ? ALSpecificSettings.getInstance(AppContextService.getContext(context)).getDatabaseName() : MobiComKitClientService.getApplicationKey(AppContextService.getContext(context))), null, DB_VERSION);
    }

    public static KmDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new KmDatabaseHelper(AppContextService.getContext(context));
        }
        return sInstance;
    }

    public void deleteDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + AUTO_SUGGESTION_TABLE);
    }

}
