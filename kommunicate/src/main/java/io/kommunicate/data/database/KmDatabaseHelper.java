package io.kommunicate.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import io.kommunicate.data.preference.ALSpecificSettings;
import io.kommunicate.KommunicateService;
import io.kommunicate.data.api.MobiComKitClientService;
import io.kommunicate.utils.DBUtils;

public class KmDatabaseHelper extends MobiComDatabaseHelper {
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
    private static final int DB_VERSION = 1;
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
    private static KmDatabaseHelper sInstance;

    private KmDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private KmDatabaseHelper(Context context) {
        this(context, "KM_" + (!TextUtils.isEmpty(ALSpecificSettings.getInstance(KommunicateService.getContext(context)).getDatabaseName()) ? ALSpecificSettings.getInstance(KommunicateService.getContext(context)).getDatabaseName() : MobiComKitClientService.getApplicationKey(KommunicateService.getContext(context))), null, DB_VERSION);
    }

    public static KmDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new KmDatabaseHelper(KommunicateService.getContext(context));
        }
        return sInstance;
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

    public void deleteDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + AUTO_SUGGESTION_TABLE);
    }

}
