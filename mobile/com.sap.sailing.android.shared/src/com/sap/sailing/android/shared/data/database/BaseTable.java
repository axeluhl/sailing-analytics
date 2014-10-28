package com.sap.sailing.android.shared.data.database;

import android.database.sqlite.SQLiteDatabase;

public interface BaseTable {

    public final static String _TIMESTAMP = "_timestamp";

    public abstract void onCreate(SQLiteDatabase db);

    public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    public abstract void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion);
}
