package com.sap.sailing.android.shared.data.database;

import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class GpsTable implements BaseTable, BaseColumns {

    public final static String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.sap.sailing.content.gpsdata";
    public final static String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/vnd.sap.sailing.content.gpsdata";

    public final static String TABLENAME = "data_gps";

    public final static String ACCURACY = "accuracy";
    public final static String ALTITUDE = "altitude";
    public final static String BEARING = "bearing";
    public final static String ELAPSEDREALTIME = "elapsedrealtime";
    public final static String LATITUDE = "latitude";
    public final static String LONGITUDE = "longitude";
    public final static String PROVIDER = "provider";
    public final static String SPEED = "speed";
    public final static String TIME = "time";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLENAME + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + _TIMESTAMP
                + " TEXT, " + ACCURACY + " REAL, " + ALTITUDE + " REAL, " + BEARING + " REAL, " + ELAPSEDREALTIME
                + " TEXT, " + LATITUDE + " REAL, " + LONGITUDE + " REAL, " + PROVIDER + " TEXT, " + SPEED + " REAL, "
                + TIME + " TEXT " + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLENAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLENAME);
        onCreate(db);
    }
}